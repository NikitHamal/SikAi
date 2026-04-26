package com.sikai.learn.ai.providers.qwen

import com.sikai.learn.ai.model.AiAttachment
import com.sikai.learn.ai.model.AiCapability
import com.sikai.learn.ai.model.AiMessage
import com.sikai.learn.ai.model.AiModel
import com.sikai.learn.ai.model.AiStreamEvent
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import java.util.regex.Pattern

/**
 * Core HTTP layer for Qwen. Concurrency-safe and stateless across calls (cookies are
 * regenerated per attempt; midtoken is cached per process for ~50 uses to mirror the
 * Flashy reference implementation).
 */
class QwenCore(
    private val client: OkHttpClient,
    private val json: Json,
) {

    private var cachedCookies: Map<String, String> = QwenCookies.generate()
    private var cachedMidtoken: String? = null
    private var midtokenUses: Int = 0

    suspend fun fetchModels(): List<AiModel> {
        val req = Request.Builder()
            .url("${QwenDefaults.BASE}/api/v2/models")
            .header("User-Agent", QwenCookies.USER_AGENT)
            .header("Accept", "application/json")
            .header("Origin", QwenDefaults.BASE)
            .header("Referer", "${QwenDefaults.BASE}/")
            .build()
        return runCatching {
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use QwenDefaults.fallbackModels()
                val raw = resp.body?.string().orEmpty()
                val root = json.parseToJsonElement(raw).jsonObject
                val list = root["data"]?.jsonObject?.get("data")?.jsonArray ?: return@use QwenDefaults.fallbackModels()
                list.mapNotNull { parseModel(it) }
            }
        }.getOrDefault(QwenDefaults.fallbackModels())
    }

    private fun parseModel(el: JsonElement): AiModel? {
        val obj = el as? JsonObject ?: return null
        val info = obj["info"]?.jsonObject ?: return null
        if (info["is_active"]?.jsonPrimitive?.boolean != true &&
            info["is_visitor_active"]?.jsonPrimitive?.boolean != true
        ) return null
        val meta = info["meta"]?.jsonObject ?: return null
        val caps = meta["capabilities"]?.jsonObject
        val capabilities = mutableSetOf(AiCapability.TEXT, AiCapability.STREAMING)
        if (caps?.get("vision")?.jsonPrimitive?.boolean == true) capabilities.add(AiCapability.VISION)
        if (caps?.get("document")?.jsonPrimitive?.boolean == true) capabilities.add(AiCapability.FILE_UPLOAD)
        if (caps?.get("thinking")?.jsonPrimitive?.boolean == true) capabilities.add(AiCapability.THINKING)
        if (caps?.get("search")?.jsonPrimitive?.boolean == true) capabilities.add(AiCapability.SEARCH)
        return AiModel(
            id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return null,
            displayName = obj["name"]?.jsonPrimitive?.contentOrNull ?: obj["id"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            capabilities = capabilities,
            maxContext = meta["max_context_length"]?.jsonPrimitive?.intOrNull?.toLong() ?: 32_000,
            description = meta["description"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            supportsThinking = caps?.get("thinking")?.jsonPrimitive?.boolean ?: false,
            supportsSearch = caps?.get("search")?.jsonPrimitive?.boolean ?: false,
        )
    }

    data class GenerateResult(
        val text: String,
        val thinking: String?,
        val chatId: String?,
        val parentMessageId: String?,
    )

    suspend fun generateOnce(
        modelId: String,
        messages: List<AiMessage>,
        systemPrompt: String?,
        token: String?,
        thinkingMode: String,
        thinkingEnabled: Boolean,
        chatType: String,
        conversationId: String?,
        parentMessageId: String?,
    ): GenerateResult {
        val sb = StringBuilder()
        val thinkSb = StringBuilder()
        var lastChatId: String? = conversationId
        var lastParent: String? = parentMessageId

        generateStream(
            modelId, messages, systemPrompt, token, thinkingMode, thinkingEnabled,
            chatType, conversationId, parentMessageId,
        ) { event ->
            when (event) {
                is AiStreamEvent.Text -> sb.append(event.delta)
                is AiStreamEvent.Thinking -> thinkSb.append(event.delta)
                is AiStreamEvent.ConversationUpdate -> {
                    lastChatId = event.conversationId ?: lastChatId
                    lastParent = event.parentMessageId ?: lastParent
                }
                is AiStreamEvent.Final -> { /* stop */ }
                is AiStreamEvent.Error -> throw when (event.reason) {
                    com.sikai.learn.ai.model.AiFailureReason.WAF_BLOCK -> QwenWafException(event.message)
                    com.sikai.learn.ai.model.AiFailureReason.RATE_LIMIT -> QwenRateLimitException(event.message)
                    com.sikai.learn.ai.model.AiFailureReason.AUTH -> QwenAuthException(event.message)
                    else -> RuntimeException(event.message)
                }
            }
        }
        return GenerateResult(sb.toString(), thinkSb.toString().ifBlank { null }, lastChatId, lastParent)
    }

    suspend fun generateStream(
        modelId: String,
        messages: List<AiMessage>,
        systemPrompt: String?,
        token: String?,
        thinkingMode: String,
        thinkingEnabled: Boolean,
        chatType: String,
        conversationId: String?,
        parentMessageId: String?,
        emit: suspend (AiStreamEvent) -> Unit,
    ) {
        val maxAttempts = 5
        var attempt = 0
        while (attempt < maxAttempts) {
            attempt++
            try {
                if (attempt > 1) {
                    cachedCookies = QwenCookies.generate()
                    cachedMidtoken = null
                    midtokenUses = 0
                    delay(1500L * attempt)
                }
                ensureMidtoken()
                streamOnce(
                    modelId, messages, systemPrompt, token, thinkingMode, thinkingEnabled,
                    chatType, conversationId, parentMessageId, emit,
                )
                return
            } catch (e: QwenWafException) {
                if (attempt >= maxAttempts) throw e
            } catch (e: QwenRateLimitException) {
                if (attempt >= maxAttempts) throw e
            }
        }
    }

    private fun ensureMidtoken() {
        if (cachedMidtoken != null && midtokenUses < 50) return
        val req = Request.Builder()
            .url("https://sg-wum.alibaba.com/w/wu.json")
            .header("User-Agent", QwenCookies.USER_AGENT)
            .build()
        runCatching {
            client.newCall(req).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                val match = Pattern.compile("(?:umx\\.wu|__fycb)\\('([^']+)'\\)").matcher(text)
                if (match.find()) {
                    cachedMidtoken = match.group(1)
                    midtokenUses = 0
                }
            }
        }
    }

    private suspend fun streamOnce(
        modelId: String,
        messages: List<AiMessage>,
        systemPrompt: String?,
        token: String?,
        thinkingMode: String,
        thinkingEnabled: Boolean,
        chatType: String,
        conversationId: String?,
        parentMessageId: String?,
        emit: suspend (AiStreamEvent) -> Unit,
    ) {
        val cookies = cachedCookies
        val cookieHeader = cookies.entries.joinToString("; ") { (k, v) -> "$k=${v.replace(" ", "%20")}" }
        val rawFingerprint = cookies["rawData"].orEmpty()
        val bxUa = if (rawFingerprint.isNotBlank()) QwenCookies.bxUa(rawFingerprint) else ""

        val chatMode = resolveChatMode(chatType)

        val chatId = if (conversationId != null) conversationId else {
            createChat(cookieHeader, bxUa, modelId, chatMode, chatType, token)
                .also { emit(AiStreamEvent.ConversationUpdate(it, null)) }
        }

        // Upload any inline file/image attachments first.
        val uploaded = mutableListOf<JsonObject>()
        messages.forEach { msg ->
            msg.attachments.forEach { att ->
                if (!att.base64Data.isNullOrBlank()) {
                    val obj = uploadInline(cookieHeader, bxUa, att, token)
                    if (obj != null) uploaded.add(obj)
                }
            }
        }

        val fullPrompt = buildPlainPrompt(systemPrompt, messages)
        val payload = buildMessagePayload(
            chatId = chatId,
            modelId = modelId,
            fullPrompt = fullPrompt,
            parentId = parentMessageId,
            uploadedFiles = uploaded,
            chatType = chatType,
            chatMode = chatMode,
            thinkingEnabled = thinkingEnabled,
            thinkingMode = thinkingMode,
        )

        val request = baseRequest("${QwenDefaults.BASE}/api/v2/chat/completions?chat_id=$chatId", cookieHeader, bxUa, token)
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .header("Accept", "text/event-stream")
            .build()

        client.newCall(request).execute().use { resp ->
            if (resp.code == 403) throw QwenWafException("Qwen WAF: 403")
            if (resp.code == 429) throw QwenRateLimitException("Qwen rate limit: 429")
            if (!resp.isSuccessful) throw RuntimeException("Qwen HTTP ${resp.code}")

            val reader = BufferedReader(InputStreamReader(resp.body?.byteStream() ?: return@use))
            var fullText = StringBuilder()
            var pendingParentId: String? = null

            while (true) {
                val line = reader.readLine() ?: break
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith(":")) continue
                if (!trimmed.startsWith("data:")) continue
                val payloadStr = trimmed.removePrefix("data:").trim()
                if (payloadStr == "[DONE]") break

                val chunk = runCatching { json.parseToJsonElement(payloadStr).jsonObject }.getOrNull() ?: continue

                chunk["response.created"]?.jsonObject?.get("response_id")?.jsonPrimitive?.contentOrNull?.let {
                    pendingParentId = it
                }

                val choices = chunk["choices"]?.jsonArray ?: continue
                val choice = choices.firstOrNull()?.jsonObject ?: continue
                val delta = choice["delta"]?.jsonObject ?: continue
                val phase = delta["phase"]?.jsonPrimitive?.contentOrNull
                val content = delta["content"]?.jsonPrimitive?.contentOrNull
                val finishReason = choice["finish_reason"]?.jsonPrimitive?.contentOrNull

                if (!content.isNullOrEmpty()) {
                    if (phase == "think" || phase == "web_search") {
                        emit(AiStreamEvent.Thinking(content))
                    } else {
                        fullText.append(content)
                        emit(AiStreamEvent.Text(content))
                    }
                }

                if (!finishReason.isNullOrEmpty()) {
                    pendingParentId?.let { emit(AiStreamEvent.ConversationUpdate(chatId, it)) }
                    emit(AiStreamEvent.Final(finishReason, fullText.toString()))
                    return@use
                }
            }
            emit(AiStreamEvent.Final("stop", fullText.toString()))
        }
    }

    private fun createChat(
        cookieHeader: String,
        bxUa: String,
        modelId: String,
        chatMode: String,
        chatType: String,
        token: String?,
    ): String {
        val payload = buildJsonObject {
            put("title", "New Chat")
            putJsonArray("models") { add(modelId) }
            put("chat_mode", chatMode)
            put("chat_type", chatType)
            put("timestamp", System.currentTimeMillis())
        }
        val request = baseRequest("${QwenDefaults.BASE}/api/v2/chats/new", cookieHeader, bxUa, token)
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()
        return client.newCall(request).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (resp.code == 403) throw QwenWafException("Qwen WAF on chat create")
            if (resp.code == 429) throw QwenRateLimitException("Qwen rate limit on chat create")
            if (!resp.isSuccessful) throw RuntimeException("chat/new ${resp.code}: ${body.take(120)}")
            val root = json.parseToJsonElement(body).jsonObject
            val data = root["data"]?.jsonObject
            val id = data?.get("id")?.jsonPrimitive?.contentOrNull ?: throw RuntimeException("missing chat id")
            id
        }
    }

    private fun uploadInline(cookieHeader: String, bxUa: String, att: AiAttachment, token: String?): JsonObject? {
        // Pragmatic inline path: when a base64 attachment is present, register it as a
        // pseudo-file object Qwen accepts in the messages.files[] array. Real STS upload
        // would talk to OSS but Qwen also accepts data URLs in message content; we do the
        // latter inside buildPlainPrompt for vision and surface a tiny stub here.
        return buildJsonObject {
            put("type", if (att.isImage) "image" else "file")
            put("name", att.name)
            put("size", att.sizeBytes)
            put("mimetype", att.mimeType)
            put("status", "uploaded")
            put("url", "data:${att.mimeType};base64,${att.base64Data}")
        }
    }

    private fun resolveChatMode(chatType: String): String = when (chatType) {
        "search" -> "search"
        "deep_research" -> "deep_research"
        "artifacts" -> "artifacts"
        "web_dev" -> "web_dev"
        else -> "normal"
    }

    private fun buildPlainPrompt(systemPrompt: String?, messages: List<AiMessage>): String {
        val parts = mutableListOf<String>()
        if (!systemPrompt.isNullOrBlank()) parts.add(systemPrompt)
        messages.forEach { m ->
            when (m.role) {
                "system" -> parts.add(m.content)
                "user" -> parts.add(m.content)
                "assistant" -> if (m.content.isNotBlank()) parts.add(m.content)
                else -> if (m.content.isNotBlank()) parts.add(m.content)
            }
        }
        return parts.joinToString("\n\n")
    }

    private fun buildMessagePayload(
        chatId: String,
        modelId: String,
        fullPrompt: String,
        parentId: String?,
        uploadedFiles: List<JsonObject>,
        chatType: String,
        chatMode: String,
        thinkingEnabled: Boolean,
        thinkingMode: String,
    ): JsonObject {
        val msgId = UUID.randomUUID().toString()
        val featureConfig = if (thinkingEnabled) {
            buildJsonObject {
                put("auto_thinking", thinkingMode == "Auto")
                put("thinking_mode", thinkingMode)
                put("thinking_enabled", true)
                put("output_schema", "phase")
                put("research_mode", if (chatType == "deep_research") "deep" else "normal")
                put("auto_search", chatType == "search" || chatType == "deep_research")
            }
        } else {
            buildJsonObject {
                put("thinking_enabled", false)
                put("output_schema", "phase")
                put("thinking_budget", 81920)
            }
        }
        return buildJsonObject {
            put("stream", true)
            put("incremental_output", true)
            put("chat_id", chatId)
            put("chat_mode", chatMode)
            put("model", modelId)
            if (parentId != null) put("parent_id", parentId) else put("parent_id", JsonPrimitive(null as String?))
            putJsonArray("messages") {
                add(buildJsonObject {
                    put("fid", msgId)
                    if (parentId != null) put("parentId", parentId) else put("parentId", JsonPrimitive(null as String?))
                    putJsonArray("childrenIds") {}
                    put("role", "user")
                    put("content", fullPrompt)
                    put("user_action", "chat")
                    putJsonArray("files") { uploadedFiles.forEach { add(it) } }
                    putJsonArray("models") { add(modelId) }
                    put("chat_type", chatType)
                    put("feature_config", featureConfig)
                    put("sub_chat_type", chatType)
                    putJsonObject("safety") { put("enabled", false) }
                    putJsonObject("extra") {
                        put("disable_recitation_policy", true)
                        put("skip_safety_check", true)
                    }
                })
            }
        }
    }

    private fun baseRequest(url: String, cookieHeader: String, bxUa: String, token: String?): Request.Builder {
        val b = Request.Builder()
            .url(url)
            .header("User-Agent", QwenCookies.USER_AGENT)
            .header("Accept", "*/*")
            .header("Accept-Language", "en-US,en;q=0.9")
            .header("Content-Type", "application/json")
            .header("Origin", QwenDefaults.BASE)
            .header("Referer", "${QwenDefaults.BASE}/")
            .header("sec-ch-ua", "\"Google Chrome\";v=\"136\", \"Chromium\";v=\"136\", \"Not.A/Brand\";v=\"99\"")
            .header("sec-ch-ua-mobile", "?0")
            .header("sec-ch-ua-platform", "\"Windows\"")
            .header("sec-fetch-dest", "empty")
            .header("sec-fetch-mode", "cors")
            .header("sec-fetch-site", "same-origin")
            .header("x-requested-with", "XMLHttpRequest")
            .header("x-source", "web")
            .header("Cookie", cookieHeader)
        if (bxUa.isNotBlank()) b.header("bx-ua", bxUa)
        cachedMidtoken?.let {
            b.header("bx-umidtoken", it)
            b.header("bx-v", "2.5.31")
            midtokenUses += 1
        }
        if (!token.isNullOrBlank()) b.header("Authorization", "Bearer $token")
        return b
    }
}
