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
import kotlinx.serialization.json.putJsonObject
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
        val uploadedFiles = mutableListOf<JsonObject>()
        for (msg in request.messages) {
            for (att in msg.attachments) {
                val uploaded = QwenWorkerUpload.upload(context, client, backendUrl, att)
                if (uploaded != null) uploadedFiles += uploaded
            }
        }

        // 2) Build messages for the worker
        val messages = buildJsonArray {
            for (msg in request.messages) {
                addJsonObject {
                    put("role", msg.role.name.lowercase())
                    put("content", msg.content)
                    if (msg.attachments.isNotEmpty()) {
                        put("files", buildJsonArray {
                            for (att in msg.attachments) {
                                // files are pre-uploaded, we'll include the file objects
                            }
                        })
                    }
                }
            }
        }

        // 3) Build the flat prompt
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

        val thinkingEnabled = AiCapability.THINKING in capabilities
        val chatType = when (request.task) {
            AiTask.WEAKNESS_ANALYSIS -> "search"
            AiTask.GENERATE_QUIZ, AiTask.STUDY_PLAN -> "t2t"
            else -> "t2t"
        }
        val chatMode = when (request.task) {
            AiTask.WEAKNESS_ANALYSIS -> "search"
            else -> "normal"
        }

        val payload = buildJsonObject {
            put("model", model)
            put("chat_mode", chatMode)
            put("chat_type", chatType)
            put("thinking_enabled", thinkingEnabled)
            put("thinking_mode", "Auto")
            put("stream", false)
            put("messages", buildJsonArray {
                addJsonObject {
                    put("role", "user")
                    put("content", flatPrompt)
                    if (uploadedFiles.isNotEmpty()) put("files", buildJsonArray { uploadedFiles.forEach { add(it) } })
                }
            })
        }

        // 4) Call the worker
        val req = Request.Builder()
            .url("$backendUrl/v1/qwen/chat")
            .post(payload.toString().toRequestBody(JSON))
            .header("Content-Type", "application/json")
            .build()

        try {
            val resp = client.newCall(req).execute()
            val body = resp.body?.string().orEmpty()

            if (!resp.isSuccessful) {
                return@withContext AiProviderResult.Failure(
                    AiFailureReason.ServerError, id,
                    "Worker HTTP ${resp.code}: ${body.take(300)}"
                )
            }

            // Parse the response — the worker returns the full Qwen chat response
            val json = runCatching { Json.parseToJsonElement(body) as? JsonObject }.getOrNull()
                ?: return@withContext AiProviderResult.Failure(
                    AiFailureReason.Parsing, id, "Failed to parse worker response"
                )

            // If streaming was requested, we need to handle SSE
            // For now, non-streaming: extract the answer from the response
            val choices = json["choices"] as? kotlinx.serialization.json.JsonArray
            if (choices != null && choices.isNotEmpty()) {
                val first = choices[0] as? JsonObject
                val content = (first?.get("message") as? JsonObject)?.get("content") as? JsonPrimitive
                if (content != null) {
                    return@withContext AiProviderResult.Success(
                        AiResponse(
                            text = content.content ?: "",
                            providerId = id,
                            providerLabel = displayName,
                            modelId = model,
                        )
                    )
                }
            }

            // Try to extract from the data field (Qwen format)
            val data = json["data"] as? JsonObject
            val answer = data?.get("answer") as? JsonPrimitive
            if (answer != null) {
                return@withContext AiProviderResult.Success(
                    AiResponse(
                        text = answer.content ?: "",
                        providerId = id,
                        providerLabel = displayName,
                        modelId = model,
                    )
                )
            }

            AiProviderResult.Failure(AiFailureReason.Parsing, id, "No answer in worker response: ${body.take(200)}")
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

    private fun getBackendUrl(): String {
        return try {
            val clazz = Class.forName("com.sikai.learn.BuildConfig")
            val field = clazz.getDeclaredField("BACKEND_BASE_URL")
            field.get(null) as String
        } catch (_: Exception) {
            "https://sikai-content.nikithamalofficial.workers.dev"
        }
    }

    companion object {
        private val JSON = "application/json".toMediaType()
    }
}