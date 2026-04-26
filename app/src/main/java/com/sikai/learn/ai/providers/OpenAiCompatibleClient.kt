package com.sikai.learn.ai.providers

import com.sikai.learn.ai.model.AiAttachment
import com.sikai.learn.ai.model.AiFailureReason
import com.sikai.learn.ai.model.AiMessage
import com.sikai.learn.ai.model.AiProviderResult
import com.sikai.learn.ai.model.AiRequest
import com.sikai.learn.ai.model.AiResponse
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Minimal OpenAI-compatible chat completions client. Used by DeepInfra, OpenRouter,
 * NVIDIA, DeepSeek, and user-created custom providers. Concrete provider classes
 * supply the auth/host header tweaks they need.
 */
class OpenAiCompatibleClient(
    private val client: OkHttpClient,
    private val json: kotlinx.serialization.json.Json,
) {

    fun call(
        baseUrl: String,
        path: String = "/v1/chat/completions",
        modelId: String,
        request: AiRequest,
        apiKey: String?,
        extraHeaders: Map<String, String> = emptyMap(),
        providerId: String,
    ): AiProviderResult {
        val url = baseUrl.trimEnd('/') + path
        val body = buildBody(modelId, request).toString().toRequestBody("application/json".toMediaType())

        val builder = Request.Builder().url(url).post(body)
        if (!apiKey.isNullOrBlank()) builder.header("Authorization", "Bearer $apiKey")
        extraHeaders.forEach { (k, v) -> builder.header(k, v) }
        builder.header("Accept", "application/json")
        builder.header("Content-Type", "application/json")

        return try {
            client.newCall(builder.build()).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    val reason = when (resp.code) {
                        401, 403 -> AiFailureReason.AUTH
                        429 -> AiFailureReason.RATE_LIMIT
                        in 500..599 -> AiFailureReason.SERVER_ERROR
                        else -> AiFailureReason.UNKNOWN
                    }
                    return AiProviderResult.Failure(reason, "HTTP ${resp.code}: ${raw.take(200)}", providerId)
                }
                val text = parseChoiceText(raw) ?: return AiProviderResult.Failure(
                    AiFailureReason.PARSE_ERROR, "No choices in response", providerId,
                )
                AiProviderResult.Success(
                    AiResponse(text = text, providerId = providerId, modelId = modelId),
                )
            }
        } catch (t: Throwable) {
            AiProviderResult.Failure(AiFailureReason.NETWORK, t.message ?: t::class.java.simpleName, providerId)
        }
    }

    private fun buildBody(modelId: String, request: AiRequest): JsonObject = buildJsonObject {
        put("model", modelId)
        put("temperature", request.temperature)
        put("stream", false)
        if (request.maxTokens != null) put("max_tokens", request.maxTokens)
        putJsonArray("messages") {
            if (!request.systemPrompt.isNullOrBlank()) {
                add(buildJsonObject {
                    put("role", "system")
                    put("content", request.systemPrompt)
                })
            }
            request.messages.forEach { add(messageToJson(it)) }
        }
    }

    private fun messageToJson(msg: AiMessage): JsonElement {
        val hasAttachments = msg.attachments.isNotEmpty()
        if (!hasAttachments) {
            return buildJsonObject {
                put("role", msg.role)
                put("content", msg.content)
            }
        }
        // Multimodal payload uses content as an array of parts
        val parts = buildJsonArray {
            if (msg.content.isNotBlank()) {
                add(buildJsonObject {
                    put("type", "text")
                    put("text", msg.content)
                })
            }
            msg.attachments.forEach { att ->
                add(attachmentToPart(att))
            }
        }
        return buildJsonObject {
            put("role", msg.role)
            put("content", parts)
        }
    }

    private fun attachmentToPart(att: AiAttachment): JsonElement {
        if (att.isImage && !att.base64Data.isNullOrBlank()) {
            return buildJsonObject {
                put("type", "image_url")
                putJsonObject("image_url") {
                    put("url", "data:${att.mimeType};base64,${att.base64Data}")
                }
            }
        }
        return buildJsonObject {
            put("type", "text")
            put("text", "[attachment: ${att.name}]")
        }
    }

    private fun parseChoiceText(raw: String): String? = try {
        val root = json.parseToJsonElement(raw).jsonObject
        val choices = root["choices"]?.jsonArray ?: return null
        val first = choices.firstOrNull()?.jsonObject ?: return null
        val message = first["message"]?.jsonObject
        val content = message?.get("content")
        when {
            content is JsonPrimitive -> content.contentOrNull
            content is JsonArray -> content.joinToString("") { (it.jsonObject["text"] as? JsonPrimitive)?.contentOrNull.orEmpty() }
            else -> first["text"]?.let { (it as? JsonPrimitive)?.contentOrNull }
        }
    } catch (_: Throwable) {
        null
    }
}
