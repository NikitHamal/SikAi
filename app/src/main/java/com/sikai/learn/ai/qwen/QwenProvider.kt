package com.sikai.learn.ai.qwen

import android.content.Context
import com.sikai.learn.ai.AiProvider
import com.sikai.learn.ai.PromptBuilder
import com.sikai.learn.data.secure.EncryptedKeyStore
import dagger.hilt.android.qualifiers.ApplicationContext
import com.sikai.learn.domain.model.AiCapability
import com.sikai.learn.domain.model.AiFailureReason
import com.sikai.learn.domain.model.AiProviderConfig
import com.sikai.learn.domain.model.AiProviderResult
import com.sikai.learn.domain.model.AiRequest
import com.sikai.learn.domain.model.AiResponse
import com.sikai.learn.domain.model.AiTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

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
        val backendUrl = getBackendUrl()

        // 1) Upload files if any
        val hasAttachments = request.messages.any { it.attachments.isNotEmpty() }
        val uploadedFiles = mutableListOf<JsonObject>()
        for (msg in request.messages) {
            for (att in msg.attachments) {
                val uploaded = QwenWorkerUpload.upload(context, client, backendUrl, att)
                if (uploaded != null) {
                    uploadedFiles += uploaded
                } else if (hasAttachments) {
                    return@withContext AiProviderResult.Failure(
                        AiFailureReason.ServerError, id,
                        "Failed to upload file: ${att.displayName}"
                    )
                }
            }
        }

        // 2) Build the flat prompt
        val systemPrompt = PromptBuilder.systemPromptFor(request)
        val flatPrompt = buildString {
            append(systemPrompt)
            for (msg in request.messages) {
                if (msg.role == com.sikai.learn.domain.model.AiMessageRole.SYSTEM) continue
                append("\n\n")
                append(if (msg.role == com.sikai.learn.domain.model.AiMessageRole.USER) "USER:" else "ASSISTANT:")
                append("\n")
                append(msg.content)
            }
        }

        val hasImage = request.messages.any { msg ->
            msg.attachments.any { it.mimeType.startsWith("image/") }
        }
        val hasDocument = request.messages.any { msg ->
            msg.attachments.any { it.mimeType.startsWith("application/") || it.mimeType.startsWith("text/") }
        }
        val chatType = when {
            hasImage -> "image"
            hasDocument -> "document"
            request.task == AiTask.WEAKNESS_ANALYSIS -> "search"
            else -> "t2t"
        }
        val chatMode = when (request.task) {
            AiTask.WEAKNESS_ANALYSIS -> "search"
            else -> "normal"
        }
        val thinkingEnabled = AiCapability.THINKING in capabilities

        val messagesArray = buildJsonArray {
            addJsonObject {
                put("role", "user")
                put("content", flatPrompt)
                if (uploadedFiles.isNotEmpty()) put("files", buildJsonArray { uploadedFiles.forEach { add(it) } })
            }
        }

        val payload = buildJsonObject {
            put("model", model)
            put("chat_mode", chatMode)
            put("chat_type", chatType)
            put("thinking_enabled", thinkingEnabled)
            put("thinking_mode", "Auto")
            put("messages", messagesArray)
        }

        // 3) Call the worker
        val req = Request.Builder()
            .url("$backendUrl/v1/qwen/chat")
            .post(payload.toString().toRequestBody(JSON))
            .header("Content-Type", "application/json")
            .build()

        try {
            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) {
                val body = resp.body?.string()?.take(500) ?: "unknown"
                return@withContext AiProviderResult.Failure(
                    AiFailureReason.ServerError, id, "Worker HTTP ${resp.code}: $body"
                )
            }

            // Parse SSE stream from the worker
            val source = resp.body?.source() ?: return@withContext AiProviderResult.Failure(
                AiFailureReason.Parsing, id, "Empty response body"
            )

            val answer = StringBuilder()
            val reasoning = StringBuilder()

            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (!line.startsWith("data: ")) continue
                val data = line.substring(6).trim()
                if (data == "[DONE]") break

                val event = runCatching { Json.parseToJsonElement(data) as? JsonObject }.getOrNull() ?: continue
                val choices = event["choices"] as? kotlinx.serialization.json.JsonArray ?: continue
                val first = choices.firstOrNull() as? JsonObject ?: continue
                val delta = first["delta"] as? JsonObject ?: continue
                val content = (delta["content"] as? JsonPrimitive)?.content ?: continue
                if (content.isEmpty()) continue
                val phase = (delta["phase"] as? JsonPrimitive)?.content ?: ""
                if (phase == "think" || phase == "web_search") {
                    reasoning.append(content)
                } else {
                    answer.append(content)
                }
            }

            val finalAnswer = answer.toString().ifBlank { reasoning.toString() }
            if (finalAnswer.isBlank()) {
                return@withContext AiProviderResult.Failure(
                    AiFailureReason.ServerError, id, "Empty response from Qwen"
                )
            }

            AiProviderResult.Success(
                AiResponse(
                    text = finalAnswer,
                    providerId = id,
                    providerLabel = displayName,
                    modelId = model,
                    reasoning = reasoning.toString().ifBlank { null },
                )
            )
        } catch (e: IOException) {
            AiProviderResult.Failure(AiFailureReason.Network, id, e.message ?: "network error")
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

    private fun getBackendUrl(): String = try {
        val clazz = Class.forName("com.sikai.learn.BuildConfig")
        val field = clazz.getDeclaredField("BACKEND_BASE_URL")
        field.get(null) as String
    } catch (_: Exception) {
        "https://sikai-content.nikithamalofficial.workers.dev"
    }

    companion object {
        private val JSON = "application/json".toMediaType()
    }
}