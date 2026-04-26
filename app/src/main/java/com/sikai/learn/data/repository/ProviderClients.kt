package com.sikai.learn.data.repository

import android.util.Base64
import com.sikai.learn.domain.ai.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException

class OpenAiCompatibleProvider(
    override val config: AiProviderConfig,
    private val client: OkHttpClient
) : AiProvider {
    override suspend fun complete(request: AiRequest, apiKey: String?): AiProviderResult = withContext(Dispatchers.IO) {
        val model = request.modelOverride ?: if (request.messages.any { it.attachments.isNotEmpty() }) config.multimodalModel else config.textModel
        val payload = JSONObject().apply {
            put("model", model)
            put("stream", false)
            put("messages", JSONArray().apply {
                request.messages.forEach { message ->
                    put(JSONObject().apply {
                        put("role", message.role.name.lowercase())
                        if (message.attachments.isEmpty()) {
                            put("content", message.content)
                        } else {
                            put("content", JSONArray().apply {
                                put(JSONObject().put("type", "text").put("text", message.content))
                                message.attachments.forEach { attachment ->
                                    when {
                                        attachment.providerFileId != null -> put(JSONObject().put("type", "file").put("file", JSONObject().put("file_id", attachment.providerFileId)))
                                        attachment.bytesBase64 != null && attachment.mimeType.startsWith("image") -> put(JSONObject().put("type", "image_url").put("image_url", JSONObject().put("url", "data:${attachment.mimeType};base64,${attachment.bytesBase64}")))
                                        attachment.bytesBase64 != null -> put(JSONObject().put("type", "file").put("file", JSONObject().put("filename", attachment.fileName).put("file_data", "data:${attachment.mimeType};base64,${attachment.bytesBase64}")))
                                    }
                                }
                            })
                        }
                    })
                }
            })
            if (config.supportsThinking) put("enable_thinking", request.enableThinking)
            if (config.supportsSearch) put("enable_search", request.enableSearch)
        }
        val httpRequest = Request.Builder()
            .url(config.baseUrl.trimEnd('/') + "/chat/completions")
            .post(payload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .header("Content-Type", "application/json")
            .apply { if (!apiKey.isNullOrBlank()) header("Authorization", "Bearer $apiKey") }
            .build()
        executeCompletion(httpRequest, model, request)
    }

    override suspend fun uploadAttachment(attachment: AiAttachment, apiKey: String?): Result<AiAttachment> = withContext(Dispatchers.IO) {
        if (!config.capabilities.contains(AiCapability.FILE_UPLOAD) && attachment.mimeType != "application/pdf") return@withContext Result.success(attachment)
        val bytes = attachment.bytesBase64?.let { Base64.decode(it, Base64.DEFAULT) } ?: return@withContext Result.success(attachment)
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("purpose", "assistants")
            .addFormDataPart("file", attachment.fileName, bytes.toRequestBody(attachment.mimeType.toMediaTypeOrNull()))
            .build()
        val req = Request.Builder()
            .url(config.baseUrl.trimEnd('/') + "/files")
            .post(body)
            .apply { if (!apiKey.isNullOrBlank()) header("Authorization", "Bearer $apiKey") }
            .build()
        try {
            client.newCall(req).execute().use { response ->
                if (!response.isSuccessful) return@use Result.success(attachment)
                val id = JSONObject(response.body?.string().orEmpty()).optString("id")
                if (id.isBlank()) Result.success(attachment) else Result.success(attachment.copy(providerFileId = id, bytesBase64 = null))
            }
        } catch (e: Exception) {
            Result.success(attachment)
        }
    }

    override suspend fun listModels(apiKey: String?): Result<List<AiModel>> = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url(config.baseUrl.trimEnd('/') + "/models")
            .apply { if (!apiKey.isNullOrBlank()) header("Authorization", "Bearer $apiKey") }
            .build()
        try {
            client.newCall(req).execute().use { response ->
                if (!response.isSuccessful) return@use Result.failure(IOException("HTTP ${response.code}"))
                val data = JSONObject(response.body?.string().orEmpty()).optJSONArray("data") ?: JSONArray()
                Result.success((0 until data.length()).mapNotNull { index ->
                    data.optJSONObject(index)?.optString("id")?.takeIf { it.isNotBlank() }?.let { AiModel(it) }
                })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun health(apiKey: String?): ProviderHealthState = listModels(apiKey).fold(
        onSuccess = { ProviderHealthState.HEALTHY },
        onFailure = { ProviderHealthState.DEGRADED }
    )

    private fun executeCompletion(httpRequest: Request, model: String, aiRequest: AiRequest): AiProviderResult = try {
        client.newCall(httpRequest).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) return@use failureForStatus(response.code, body)
            val root = JSONObject(body)
            val choices = root.optJSONArray("choices")
            val text = choices?.optJSONObject(0)?.optJSONObject("message")?.optString("content").orEmpty()
            if (text.isBlank()) AiProviderResult.Failure(config.id, AiFailureReason.PARSING_ERROR, "Provider returned an empty response")
            else AiProviderResult.Success(AiResponse(text, config.id, config.name, model, aiRequest.preferredProviderId != null && aiRequest.preferredProviderId != config.id, root.optJSONObject("usage")?.optInt("total_tokens")))
        }
    } catch (e: SocketTimeoutException) {
        AiProviderResult.Failure(config.id, AiFailureReason.TIMEOUT, "Provider timed out")
    } catch (e: Exception) {
        AiProviderResult.Failure(config.id, AiFailureReason.NETWORK, e.message ?: "Network error")
    }

    private fun failureForStatus(code: Int, body: String): AiProviderResult.Failure {
        val reason = when (code) {
            401, 403 -> AiFailureReason.AUTH_REQUIRED
            408 -> AiFailureReason.TIMEOUT
            429 -> AiFailureReason.RATE_LIMIT
            in 500..599 -> AiFailureReason.SERVER_ERROR
            404 -> AiFailureReason.UNAVAILABLE_MODEL
            else -> AiFailureReason.UNKNOWN
        }
        val message = runCatching { JSONObject(body).optJSONObject("error")?.optString("message") }.getOrNull().orEmpty().ifBlank { "HTTP $code" }
        return AiProviderResult.Failure(config.id, reason, message, reason != AiFailureReason.AUTH_REQUIRED)
    }
}

class GeminiProvider(
    override val config: AiProviderConfig,
    private val client: OkHttpClient
) : AiProvider {
    override suspend fun complete(request: AiRequest, apiKey: String?): AiProviderResult = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrBlank()) return@withContext AiProviderResult.Failure(config.id, AiFailureReason.AUTH_REQUIRED, "Gemini API key is required", retryable = false)
        val model = request.modelOverride ?: if (request.messages.any { it.attachments.isNotEmpty() }) config.multimodalModel else config.textModel
        val contents = JSONArray().apply {
            request.messages.filter { it.role != AiRole.SYSTEM }.forEach { message ->
                put(JSONObject().put("role", if (message.role == AiRole.ASSISTANT) "model" else "user").put("parts", JSONArray().apply {
                    put(JSONObject().put("text", message.content))
                    message.attachments.forEach { attachment ->
                        attachment.bytesBase64?.let { put(JSONObject().put("inlineData", JSONObject().put("mimeType", attachment.mimeType).put("data", it))) }
                    }
                }))
            }
        }
        val system = request.messages.firstOrNull { it.role == AiRole.SYSTEM }?.content.orEmpty()
        val payload = JSONObject().put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system)))).put("contents", contents)
        val httpRequest = Request.Builder()
            .url(config.baseUrl.trimEnd('/') + "/models/$model:generateContent?key=$apiKey")
            .post(payload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .header("Content-Type", "application/json")
            .build()
        try {
            client.newCall(httpRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) return@use mapFailure(response.code, body)
                val text = JSONObject(body).optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text").orEmpty()
                if (text.isBlank()) AiProviderResult.Failure(config.id, AiFailureReason.PARSING_ERROR, "Gemini returned an empty response")
                else AiProviderResult.Success(AiResponse(text, config.id, config.name, model, request.preferredProviderId != null && request.preferredProviderId != config.id))
            }
        } catch (e: SocketTimeoutException) {
            AiProviderResult.Failure(config.id, AiFailureReason.TIMEOUT, "Provider timed out")
        } catch (e: Exception) {
            AiProviderResult.Failure(config.id, AiFailureReason.NETWORK, e.message ?: "Network error")
        }
    }

    override suspend fun health(apiKey: String?): ProviderHealthState = if (apiKey.isNullOrBlank()) ProviderHealthState.AUTH_REQUIRED else ProviderHealthState.HEALTHY

    private fun mapFailure(code: Int, body: String): AiProviderResult.Failure {
        val reason = when (code) {
            401, 403 -> AiFailureReason.AUTH_REQUIRED
            429 -> AiFailureReason.RATE_LIMIT
            in 500..599 -> AiFailureReason.SERVER_ERROR
            else -> AiFailureReason.UNKNOWN
        }
        val message = runCatching { JSONObject(body).optJSONObject("error")?.optString("message") }.getOrNull().orEmpty().ifBlank { "HTTP $code" }
        return AiProviderResult.Failure(config.id, reason, message, reason != AiFailureReason.AUTH_REQUIRED)
    }
}
