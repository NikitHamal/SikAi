package com.sikai.learn.ai.gemini

import android.content.Context
import android.util.Base64
import com.sikai.learn.ai.AiProvider
import com.sikai.learn.ai.PromptBuilder
import com.sikai.learn.data.secure.EncryptedKeyStore
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
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * Google Gemini Generative Language API. Supports inline image data, so we
 * can route SOLVE_FILE traffic here without uploading anything to a backend.
 */
class GeminiProvider(
    private val client: OkHttpClient,
    private val keyStore: EncryptedKeyStore,
    private val context: Context,
) : AiProvider {

    override val id: String = "gemini"
    override val displayName: String = "Google Gemini"
    override val capabilities: Set<AiCapability> = setOf(
        AiCapability.TEXT, AiCapability.VISION, AiCapability.PDF, AiCapability.STREAMING
    )

    override fun supports(request: AiRequest): Boolean {
        if (request.task == AiTask.SOLVE_FILE) return true
        return true
    }

    override suspend fun complete(
        config: AiProviderConfig,
        request: AiRequest,
    ): AiProviderResult = withContext(Dispatchers.IO) {
        val key = config.apiKeyAlias?.let { keyStore.get(it) }
            ?: return@withContext AiProviderResult.Failure(
                AiFailureReason.Auth, id, "No Gemini API key configured"
            )
        val model = request.preferredModelId
            ?: if (request.task == AiTask.SOLVE_FILE) (config.defaultMultimodalModel ?: "gemini-1.5-flash")
            else (config.defaultTextModel ?: "gemini-1.5-flash")

        val baseUrl = (config.baseUrl ?: "https://generativelanguage.googleapis.com/v1beta").trimEnd('/')
        val url = "$baseUrl/models/$model:generateContent?key=$key"

        val payload = buildPayload(request)
        val body = payload.toString().toRequestBody("application/json".toMediaType())

        val httpReq = Request.Builder()
            .url(url)
            .post(body)
            .header("Content-Type", "application/json")
            .build()

        try {
            client.newCall(httpReq).execute().use { resp ->
                if (!resp.isSuccessful) {
                    val reason = when (resp.code) {
                        401, 403 -> AiFailureReason.Auth
                        429 -> AiFailureReason.RateLimit
                        in 500..599 -> AiFailureReason.ServerError
                        else -> AiFailureReason.Unknown("HTTP ${resp.code}")
                    }
                    return@withContext AiProviderResult.Failure(reason, id, "HTTP ${resp.code}")
                }
                val raw = resp.body?.string().orEmpty()
                val parsed = runCatching {
                    Json.parseToJsonElement(raw) as? JsonObject
                }.getOrNull() ?: return@withContext AiProviderResult.Failure(
                    AiFailureReason.Parsing, id, "Could not parse Gemini JSON"
                )
                val text = extractText(parsed)
                if (text.isNullOrBlank()) {
                    return@withContext AiProviderResult.Failure(
                        AiFailureReason.Parsing, id, "Empty Gemini completion"
                    )
                }
                AiProviderResult.Success(
                    AiResponse(
                        text = text,
                        providerId = id,
                        providerLabel = displayName,
                        modelId = model,
                    )
                )
            }
        } catch (io: IOException) {
            AiProviderResult.Failure(AiFailureReason.Network, id, io.message ?: "network")
        } catch (t: Throwable) {
            AiProviderResult.Failure(
                AiFailureReason.Unknown(t.message ?: t::class.simpleName.orEmpty()),
                id,
                t.message ?: ""
            )
        }
    }

    private fun buildPayload(request: AiRequest): JsonObject {
        val systemText = PromptBuilder.systemPromptFor(request)
        val userMessages = request.messages

        return buildJsonObject {
            putJsonObject("systemInstruction") {
                putJsonArray("parts") {
                    addJsonObject { put("text", systemText) }
                }
            }
            putJsonArray("contents") {
                userMessages.forEach { add(serialiseMessage(it)) }
            }
            putJsonObject("generationConfig") {
                put("temperature", request.temperature)
                put("maxOutputTokens", request.maxOutputTokens)
            }
        }
    }

    private fun serialiseMessage(msg: AiMessage): JsonObject = buildJsonObject {
        put("role", roleOf(msg.role))
        putJsonArray("parts") {
            if (msg.content.isNotBlank()) addJsonObject { put("text", msg.content) }
            msg.attachments.forEach { att -> add(buildAttachment(att)) }
        }
    }

    private fun buildAttachment(att: AiAttachment): JsonObject = buildJsonObject {
        val bytes = runCatching {
            context.contentResolver.openInputStream(android.net.Uri.parse(att.uri))!!.use { it.readBytes() }
        }.getOrNull() ?: byteArrayOf()
        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        putJsonObject("inlineData") {
            put("mimeType", att.mimeType)
            put("data", b64)
        }
    }

    private fun roleOf(role: AiMessageRole): String = when (role) {
        AiMessageRole.SYSTEM -> "user" // Gemini uses systemInstruction; coerce SYSTEM into user safely
        AiMessageRole.USER -> "user"
        AiMessageRole.ASSISTANT -> "model"
        AiMessageRole.TOOL -> "user"
    }

    private fun extractText(root: JsonObject): String? {
        val candidates = root["candidates"] as? JsonArray ?: return null
        val sb = StringBuilder()
        for (c in candidates) {
            val obj = c as? JsonObject ?: continue
            val content = obj["content"] as? JsonObject ?: continue
            val parts = content["parts"] as? JsonArray ?: continue
            for (p in parts) {
                val pp = p as? JsonObject ?: continue
                val txt = (pp["text"] as? JsonPrimitive)?.content
                if (!txt.isNullOrEmpty()) sb.append(txt)
            }
        }
        return sb.toString().ifBlank { null }
    }
}
