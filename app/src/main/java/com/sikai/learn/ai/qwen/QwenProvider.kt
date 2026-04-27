package com.sikai.learn.ai.qwen

import android.content.Context
import com.sikai.learn.ai.AiProvider
import com.sikai.learn.ai.PromptBuilder
import com.sikai.learn.data.secure.EncryptedKeyStore
import dagger.hilt.android.qualifiers.ApplicationContext
import com.sikai.learn.domain.model.AiAttachment
import com.sikai.learn.domain.model.AiCapability
import com.sikai.learn.domain.model.AiFailureReason
import com.sikai.learn.domain.model.AiMessage
import com.sikai.learn.domain.model.AiMessageRole
import com.sikai.learn.domain.model.AiProviderConfig
import com.sikai.learn.domain.model.AiProviderResult
import com.sikai.learn.domain.model.AiRequest
import com.sikai.learn.domain.model.AiResponse
import com.sikai.learn.domain.model.AiTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Qwen public chat provider. Mirrors the integration surface in NikitHamal/Flashy:
 *  - identity/cookie rotation
 *  - midtoken refresh from sg-wum.alibaba.com
 *  - WAF/captcha/rate-limit retries (up to 5 attempts)
 *  - chat conversation creation, parent_id tracking
 *  - streaming SSE parsing for both "think" and "answer" phases
 *  - inline file upload via the Qwen STS endpoint when an attachment is present
 */
@Singleton
class QwenProvider @Inject constructor(
    private val client: OkHttpClient,
    private val keyStore: EncryptedKeyStore,
    @ApplicationContext private val context: Context,
) : AiProvider {

    override val id: String = "qwen"
    override val displayName: String = "Qwen"
    override val capabilities: Set<AiCapability> = setOf(
        AiCapability.TEXT,
        AiCapability.VISION,
        AiCapability.FILE_UPLOAD,
        AiCapability.PDF,
        AiCapability.STREAMING,
        AiCapability.THINKING,
        AiCapability.SEARCH,
    )

    override fun supports(request: AiRequest): Boolean = true

    override suspend fun complete(
        config: AiProviderConfig,
        request: AiRequest,
    ): AiProviderResult = withContext(Dispatchers.IO) {
        val model = pickModel(config, request)
        val token = config.apiKeyAlias?.let { keyStore.get(it) }

        val session = QwenSession(client).apply { seedSyntheticCookies() }
        val maxAttempts = 5
        var lastFailure: AiProviderResult.Failure? = null

        repeat(maxAttempts) { attempt ->
            session.refreshMidTokenSync()
            val attempted = runAttempt(session, config, request, model, token)
            when (attempted) {
                is AttemptOutcome.Success -> return@withContext attempted.result
                is AttemptOutcome.RetryWaf -> {
                    session.reset()
                    session.seedSyntheticCookies()
                    delay(BACKOFF_MS * (attempt + 1))
                }
                is AttemptOutcome.RetryRate -> delay(BACKOFF_MS * (attempt + 1))
                is AttemptOutcome.HardFail -> {
                    lastFailure = attempted.failure
                    return@withContext attempted.failure
                }
            }
        }
        lastFailure ?: AiProviderResult.Failure(
            AiFailureReason.Waf, id, "Qwen retried $maxAttempts times without success"
        )
    }

    private suspend fun runAttempt(
        session: QwenSession,
        config: AiProviderConfig,
        request: AiRequest,
        model: String,
        token: String?,
    ): AttemptOutcome {
        // 0) warm up session: visit chat.qwen.ai to collect real cookies
        session.warmUp(client)

        // 1) optional file upload
        val uploadedFiles = mutableListOf<JsonObject>()
        for (att in request.messages.flatMap { it.attachments }) {
            try {
                val uploaded = QwenFileUpload.upload(context, client, session, att)
                uploadedFiles += uploaded
            } catch (e: Exception) {
                return AttemptOutcome.HardFail(
                    AiProviderResult.Failure(
                        AiFailureReason.UnsupportedCapability,
                        id,
                        "Qwen file upload failed: ${e.message}"
                    )
                )
            }
        }

        // 2) create new chat (per request, simplest model — no resume across calls
        //    in this MVP; conversation memory rebuilt from messages)
        val createBody = buildJsonObject {
            put("title", "SikAi Chat")
            putJsonArray("models") { add(model) }
            put("chat_mode", chatModeFor(request))
            put("chat_type", chatTypeFor(request))
            put("timestamp", System.currentTimeMillis())
        }.toString().toRequestBody(JSON)

        val createReq = baseRequestBuilder(session, token)
            .url("$BASE/api/v2/chats/new")
            .post(createBody)
            .build()

        val chatId = try {
            client.newCall(createReq).execute().use { resp ->
                session.mergeFromSetCookies(resp.headers("Set-Cookie"))
                if (isWafBlocked(resp)) return AttemptOutcome.RetryWaf
                if (resp.code == 429) return AttemptOutcome.RetryRate
                if (!resp.isSuccessful) return AttemptOutcome.HardFail(
                    AiProviderResult.Failure(
                        AiFailureReason.ServerError,
                        id,
                        "Qwen chat create HTTP ${resp.code}"
                    )
                )
                val text = resp.body?.string().orEmpty()
                val obj = Json.parseToJsonElement(text) as? JsonObject
                val ok = (obj?.get("success") as? JsonPrimitive)?.content?.toBoolean() ?: false
                if (!ok) return AttemptOutcome.RetryWaf
                val data = obj["data"] as? JsonObject
                (data?.get("id") as? JsonPrimitive)?.content
                    ?: return AttemptOutcome.HardFail(
                        AiProviderResult.Failure(
                            AiFailureReason.Parsing,
                            id,
                            "No chat id from Qwen"
                        )
                    )
            }
        } catch (io: IOException) {
            return AttemptOutcome.HardFail(
                AiProviderResult.Failure(AiFailureReason.Network, id, io.message ?: "network")
            )
        }

        // 3) build the chat completion payload (single user turn, full prompt
        //    flattened — Flashy does the same when no parent_id is known)
        val systemPrompt = PromptBuilder.systemPromptFor(request)
        val flatPrompt = buildString {
            append(systemPrompt)
            request.messages.forEach { m ->
                if (m.role == AiMessageRole.SYSTEM) return@forEach
                append("\n\n")
                append(if (m.role == AiMessageRole.USER) "USER:" else "ASSISTANT:")
                append("\n")
                append(m.content)
            }
        }

        val msgId = UUID.randomUUID().toString()
        val featureConfig = buildJsonObject {
            val thinking = AiCapability.THINKING in capabilities
            put("thinking_enabled", thinking)
            if (thinking) {
                put("auto_thinking", true)
                put("thinking_mode", "Auto")
            }
            put("output_schema", "phase")
            put("auto_search", request.task == AiTask.WEAKNESS_ANALYSIS)
        }
        val msgPayload = buildJsonObject {
            put("stream", true)
            put("incremental_output", true)
            put("chat_id", chatId)
            put("chat_mode", chatModeFor(request))
            put("model", model)
            putJsonArray("messages") {
                addJsonObject {
                    put("fid", msgId)
                    put("parentId", JsonPrimitive(null as String?))
                    putJsonArray("childrenIds") { /* empty */ }
                    put("role", "user")
                    put("content", flatPrompt)
                    put("user_action", "chat")
                    putJsonArray("files") { uploadedFiles.forEach { add(it) } }
                    putJsonArray("models") { add(model) }
                    put("chat_type", chatTypeFor(request))
                    put("feature_config", featureConfig)
                    put("sub_chat_type", chatTypeFor(request))
                    putJsonObject("safety") { put("enabled", false) }
                    putJsonObject("extra") {
                        put("disable_recitation_policy", true)
                        put("skip_safety_check", true)
                    }
                }
            }
        }

        val streamReq = baseRequestBuilder(session, token)
            .url("$BASE/api/v2/chat/completions?chat_id=$chatId")
            .post(msgPayload.toString().toRequestBody(JSON))
            .header("Accept", "text/event-stream")
            .build()

        return try {
            client.newCall(streamReq).execute().use { resp ->
                if (isWafBlocked(resp)) return AttemptOutcome.RetryWaf
                if (resp.code == 429) return AttemptOutcome.RetryRate
                if (!resp.isSuccessful) return AttemptOutcome.HardFail(
                    AiProviderResult.Failure(
                        AiFailureReason.ServerError,
                        id,
                        "Qwen stream HTTP ${resp.code}"
                    )
                )

                val parser = QwenStreamParser()
                val source = resp.body?.source() ?: return AttemptOutcome.HardFail(
                    AiProviderResult.Failure(AiFailureReason.Parsing, id, "Empty stream body")
                )
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    val withNewline = line + "\n"
                    if (parser.feed(withNewline)) break
                }
                val out = parser.finalAnswer()
                if (out.isBlank()) {
                    return AttemptOutcome.RetryRate
                }
                AttemptOutcome.Success(
                    AiProviderResult.Success(
                        AiResponse(
                            text = out,
                            providerId = id,
                            providerLabel = displayName,
                            modelId = model,
                            reasoning = parser.finalReasoning(),
                        )
                    )
                )
            }
        } catch (io: IOException) {
            AttemptOutcome.HardFail(
                AiProviderResult.Failure(AiFailureReason.Network, id, io.message ?: "network")
            )
        }
    }

    private fun pickModel(config: AiProviderConfig, request: AiRequest): String {
        val explicit = request.preferredModelId
        if (!explicit.isNullOrBlank()) return explicit
        return when (request.task) {
            AiTask.SOLVE_FILE -> config.defaultMultimodalModel ?: "qwen3.6-plus"
            else -> config.defaultTextModel ?: "qwen3.6-plus"
        }
    }

    private fun chatTypeFor(request: AiRequest): String = when (request.task) {
        AiTask.WEAKNESS_ANALYSIS -> "search"
        AiTask.GENERATE_QUIZ, AiTask.STUDY_PLAN -> "t2t"
        else -> "t2t"
    }

    private fun chatModeFor(request: AiRequest): String = when (request.task) {
        AiTask.WEAKNESS_ANALYSIS -> "search"
        else -> "normal"
    }

    private fun baseRequestBuilder(session: QwenSession, token: String?): Request.Builder {
        val ident = session.identity
        val b = Request.Builder()
        b.header("Accept", "*/*")
        b.header("Accept-Language", "en-US,en;q=0.9")
        b.header("Content-Type", "application/json")
        b.header("Origin", BASE)
        b.header("Referer", "$BASE/")
        b.header("User-Agent", ident.userAgent)
        b.header("sec-ch-ua", ident.secChUa)
        b.header("sec-ch-ua-mobile", ident.secChUaMobile)
        b.header("sec-ch-ua-platform", ident.secChUaPlatform)
        b.header("sec-fetch-dest", "empty")
        b.header("sec-fetch-mode", "cors")
        b.header("sec-fetch-site", "same-origin")
        b.header("x-requested-with", "XMLHttpRequest")
        b.header("x-source", "web")
        val bxUa = session.bxUaHeader()
        if (bxUa.isNotEmpty()) b.header("bx-ua", bxUa)
        val cookies = session.cookieHeader()
        if (cookies.isNotEmpty()) b.header("Cookie", cookies)
        session.midToken.get()?.let {
            b.header("bx-umidtoken", it)
            b.header("bx-v", "2.5.31")
        }
        if (!token.isNullOrBlank()) b.header("Authorization", "Bearer $token")
        return b
    }

    private fun isWafBlocked(resp: Response): Boolean {
        if (resp.code == 403) return true
        if (resp.code in 520..530) return true
        if (resp.code == 200) {
            val ct = resp.header("Content-Type") ?: ""
            if (ct.contains("text/html", ignoreCase = true)) return true
        }
        return false
    }

    private sealed interface AttemptOutcome {
        data class Success(val result: AiProviderResult.Success) : AttemptOutcome
        data class HardFail(val failure: AiProviderResult.Failure) : AttemptOutcome
        data object RetryWaf : AttemptOutcome
        data object RetryRate : AttemptOutcome
    }

    companion object {
        const val BASE = "https://chat.qwen.ai"
        private const val BACKOFF_MS = 1500L
        private val JSON = "application/json".toMediaType()
    }
}

private fun JsonArray.firstObjectOrNull(): JsonObject? = firstOrNull() as? JsonObject
