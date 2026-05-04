package com.sikai.learn.ai.openai

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
import com.sikai.learn.domain.model.AiUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonArray
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
import java.io.IOException

/**
 * Generic OpenAI Chat Completions implementation. Used as-is for any provider
 * speaking the OpenAI wire format.
 * and any user-supplied custom server.
 */
open class OpenAiCompatibleProvider(
    final override val id: String,
    final override val displayName: String,
    final override val capabilities: Set<AiCapability>,
    private val defaultBaseUrl: String,
    private val client: OkHttpClient,
    private val keyStore: EncryptedKeyStore,
    private val context: Context,
    private val extraHeaders: Map<String, String> = emptyMap(),
    private val authScheme: AuthScheme = AuthScheme.Bearer,
) : AiProvider {

    enum class AuthScheme { Bearer, ApiKeyHeader, None }

    override fun supports(request: AiRequest): Boolean {
        if (request.task == AiTask.SOLVE_FILE) {
            return AiCapability.VISION in capabilities
        }
        return AiCapability.TEXT in capabilities
    }

    override suspend fun complete(
        config: AiProviderConfig,
        request: AiRequest,
    ): AiProviderResult = withContext(Dispatchers.IO) {
        val baseUrl = (config.baseUrl ?: defaultBaseUrl).trimEnd('/')
        val url = "$baseUrl/chat/completions"
        val model = pickModel(config, request) ?: return@withContext AiProviderResult.Failure(
            AiFailureReason.UnsupportedCapability, id, "No model configured for $displayName"
        )
        val key = config.apiKeyAlias?.let { keyStore.get(it) }
        if (authScheme != AuthScheme.None && key.isNullOrBlank()) {
            return@withContext AiProviderResult.Failure(
                AiFailureReason.Auth, id, "No API key configured for $displayName"
            )
        }

        val payload = buildPayload(model, request)
        val body = payload.toString().toRequestBody(JSON_MEDIA)

        val builder = Request.Builder().url(url).post(body)
        builder.header("Content-Type", "application/json")
        builder.header("Accept", "application/json")
        when (authScheme) {
            AuthScheme.Bearer -> builder.header("Authorization", "Bearer $key")
            AuthScheme.ApiKeyHeader -> builder.header("api-key", key ?: "")
            AuthScheme.None -> Unit
        }
        extraHeaders.forEach { (k, v) -> builder.header(k, v) }

        try {
            client.newCall(builder.build()).execute().use { resp ->
                if (!resp.isSuccessful) {
                    val reason = when (resp.code) {
                        401, 403 -> AiFailureReason.Auth
                        429 -> AiFailureReason.RateLimit
                        in 500..599 -> AiFailureReason.ServerError
                        else -> AiFailureReason.Unknown("HTTP ${resp.code}")
                    }
                    return@withContext AiProviderResult.Failure(reason, id, "HTTP ${resp.code}")
                }
                val text = resp.body?.string().orEmpty()
                val parsed = runCatching { Json.parseToJsonElement(text).jsonObjectOrNull() }
                    .getOrNull()
                    ?: return@withContext AiProviderResult.Failure(
                        AiFailureReason.Parsing, id, "Could not parse JSON response"
                    )
                val choices = parsed["choices"] as? JsonArray
                val first = choices?.firstOrNull() as? JsonObject
                val message = first?.get("message") as? JsonObject
                val content = (message?.get("content") as? kotlinx.serialization.json.JsonPrimitive)
                    ?.content
                if (content.isNullOrBlank()) {
                    return@withContext AiProviderResult.Failure(
                        AiFailureReason.Parsing, id, "Empty completion"
                    )
                }
                val usage = parseUsage(parsed["usage"] as? JsonObject)
                AiProviderResult.Success(
                    AiResponse(
                        text = content,
                        providerId = id,
                        providerLabel = displayName,
                        modelId = model,
                        usage = usage,
                    )
                )
            }
        } catch (io: IOException) {
            AiProviderResult.Failure(AiFailureReason.Network, id, io.message ?: "network error")
        } catch (t: Throwable) {
            AiProviderResult.Failure(AiFailureReason.Unknown(t.message ?: t::class.simpleName.orEmpty()), id, t.message ?: "")
        }
    }

    protected open fun pickModel(config: AiProviderConfig, request: AiRequest): String? {
        val explicit = request.preferredModelId
            ?: if (request.task == AiTask.SOLVE_FILE) config.defaultMultimodalModel
            else null
        return explicit ?: config.defaultTextModel ?: config.defaultMultimodalModel
    }

    protected open fun buildPayload(model: String, request: AiRequest): JsonObject {
        val messages = PromptBuilder.buildMessages(request)
        return buildJsonObject {
            put("model", model)
            put("temperature", request.temperature)
            put("max_tokens", request.maxOutputTokens)
            put("stream", false) // streaming on background path is non-MVP for first cut
            putJsonArray("messages") {
                messages.forEach { add(serialiseMessage(it)) }
            }
        }
    }

    private fun serialiseMessage(msg: AiMessage): JsonObject = buildJsonObject {
        put("role", roleOf(msg.role))
        if (msg.attachments.isEmpty()) {
            put("content", msg.content)
        } else {
            putJsonArray("content") {
                addJsonObject {
                    put("type", "text")
                    put("text", msg.content)
                }
                msg.attachments.forEach { att -> add(buildAttachment(att)) }
            }
        }
    }

    private fun buildAttachment(att: AiAttachment): JsonObject = buildJsonObject {
        put("type", "image_url")
        putJsonObject("image_url") {
            put("url", inlineDataUri(att))
        }
    }

    private fun inlineDataUri(att: AiAttachment): String {
        return runCatching {
            val resolver = context.contentResolver
            val bytes = resolver.openInputStream(android.net.Uri.parse(att.uri))!!.use { it.readBytes() }
            val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:${att.mimeType};base64,$b64"
        }.getOrElse { att.uri }
    }

    private fun roleOf(role: AiMessageRole): String = when (role) {
        AiMessageRole.SYSTEM -> "system"
        AiMessageRole.USER -> "user"
        AiMessageRole.ASSISTANT -> "assistant"
        AiMessageRole.TOOL -> "tool"
    }

    private fun parseUsage(o: JsonObject?): AiUsage? {
        if (o == null) return null
        fun int(k: String) = (o[k] as? kotlinx.serialization.json.JsonPrimitive)?.content?.toIntOrNull()
        return AiUsage(
            promptTokens = int("prompt_tokens"),
            completionTokens = int("completion_tokens"),
            totalTokens = int("total_tokens"),
        )
    }

    companion object {
        val JSON_MEDIA = "application/json".toMediaType()
    }
}

private fun kotlinx.serialization.json.JsonElement.jsonObjectOrNull(): JsonObject? =
    this as? JsonObject
