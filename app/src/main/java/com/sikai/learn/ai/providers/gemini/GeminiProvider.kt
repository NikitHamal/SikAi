package com.sikai.learn.ai.providers.gemini

import com.sikai.learn.ai.model.AiCapability
import com.sikai.learn.ai.model.AiFailureReason
import com.sikai.learn.ai.model.AiMessage
import com.sikai.learn.ai.model.AiModel
import com.sikai.learn.ai.model.AiProviderConfig
import com.sikai.learn.ai.model.AiProviderResult
import com.sikai.learn.ai.model.AiProviderType
import com.sikai.learn.ai.model.AiRequest
import com.sikai.learn.ai.model.AiRequestFormat
import com.sikai.learn.ai.model.AiResponse
import com.sikai.learn.ai.model.AiStreamEvent
import com.sikai.learn.ai.provider.AiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
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
import javax.inject.Inject

/**
 * Google Gemini provider — uses the native generativelanguage.googleapis.com endpoint
 * with API-key auth (?key=...). Supports text, vision (inline base64 images), and PDFs
 * via inline_data parts.
 */
class GeminiProvider @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
) : AiProvider {

    private var _config: AiProviderConfig = defaultConfig()
    private var _apiKey: String? = null

    fun bind(config: AiProviderConfig, apiKey: String?): GeminiProvider {
        _config = config; _apiKey = apiKey; return this
    }

    override val config: AiProviderConfig get() = _config

    override fun capabilities(): Set<AiCapability> =
        setOf(AiCapability.TEXT, AiCapability.VISION, AiCapability.PDF, AiCapability.STREAMING)

    override suspend fun listModels(): List<AiModel> = withContext(Dispatchers.IO) { fallbackModels() }

    override suspend fun generate(request: AiRequest): AiProviderResult = withContext(Dispatchers.IO) {
        val key = _apiKey
        if (key.isNullOrBlank()) {
            return@withContext AiProviderResult.Failure(
                AiFailureReason.AUTH, "Gemini key not configured", config.id,
            )
        }
        val modelId = pickModel(request)
        val url = "${config.baseUrl}/v1beta/models/$modelId:generateContent?key=$key"
        val body = buildJsonObject {
            request.systemPrompt?.let {
                putJsonObject("systemInstruction") {
                    putJsonArray("parts") { add(buildJsonObject { put("text", it) }) }
                }
            }
            putJsonArray("contents") {
                request.messages.forEach { msg ->
                    add(buildJsonObject {
                        put("role", if (msg.role == "assistant") "model" else "user")
                        putJsonArray("parts") {
                            if (msg.content.isNotBlank()) add(buildJsonObject { put("text", msg.content) })
                            msg.attachments.forEach { att ->
                                if (!att.base64Data.isNullOrBlank()) {
                                    add(buildJsonObject {
                                        putJsonObject("inline_data") {
                                            put("mime_type", att.mimeType)
                                            put("data", att.base64Data)
                                        }
                                    })
                                }
                            }
                        }
                    })
                }
            }
            putJsonObject("generationConfig") {
                put("temperature", request.temperature)
                request.maxTokens?.let { put("maxOutputTokens", it) }
            }
        }

        val req = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .header("Content-Type", "application/json")
            .build()

        try {
            client.newCall(req).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    val reason = when (resp.code) {
                        401, 403 -> AiFailureReason.AUTH
                        429 -> AiFailureReason.RATE_LIMIT
                        in 500..599 -> AiFailureReason.SERVER_ERROR
                        else -> AiFailureReason.UNKNOWN
                    }
                    return@withContext AiProviderResult.Failure(reason, "HTTP ${resp.code}: ${raw.take(200)}", config.id)
                }
                val text = parseText(raw) ?: return@withContext AiProviderResult.Failure(
                    AiFailureReason.PARSE_ERROR, "Empty Gemini response", config.id,
                )
                AiProviderResult.Success(AiResponse(text = text, providerId = config.id, modelId = modelId))
            }
        } catch (t: Throwable) {
            AiProviderResult.Failure(AiFailureReason.NETWORK, t.message ?: "network", config.id)
        }
    }

    override fun stream(request: AiRequest): Flow<AiStreamEvent> = flow {
        when (val r = generate(request)) {
            is AiProviderResult.Success -> {
                emit(AiStreamEvent.Text(r.response.text))
                emit(AiStreamEvent.Final("stop", r.response.text))
            }
            is AiProviderResult.Failure -> emit(AiStreamEvent.Error(r.reason, r.message))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun testConnection() = generate(AiRequest(messages = listOf(AiMessage("user", "ping")), maxTokens = 4))

    private fun parseText(raw: String): String? = try {
        val root = json.parseToJsonElement(raw).jsonObject
        val candidates = root["candidates"]?.jsonArray ?: return null
        candidates.firstOrNull()?.jsonObject?.get("content")?.jsonObject
            ?.get("parts")?.jsonArray
            ?.joinToString("") { (it.jsonObject["text"]?.jsonPrimitive?.contentOrNull).orEmpty() }
    } catch (_: Throwable) { null }

    private fun pickModel(req: AiRequest): String {
        val needsVision = req.preferredCapabilities.contains(AiCapability.VISION) || req.messages.any { it.attachments.any() }
        return if (needsVision) config.multimodalModel ?: config.textModel else config.textModel
    }

    companion object {
        const val ID = "gemini-builtin"
        fun defaultConfig() = AiProviderConfig(
            id = ID,
            type = AiProviderType.GEMINI,
            displayName = "Google Gemini",
            baseUrl = "https://generativelanguage.googleapis.com",
            requestFormat = AiRequestFormat.GEMINI_NATIVE,
            textModel = "gemini-1.5-flash",
            multimodalModel = "gemini-1.5-flash",
            supportsFileUpload = true,
            capabilities = setOf(AiCapability.TEXT, AiCapability.VISION, AiCapability.PDF, AiCapability.STREAMING),
            priority = 40, enabled = true, isBuiltIn = true, needsApiKey = true,
            notes = "Add your Gemini API key in Settings.",
        )

        fun fallbackModels(): List<AiModel> = listOf(
            AiModel("gemini-1.5-flash", "Gemini 1.5 Flash", setOf(AiCapability.TEXT, AiCapability.VISION, AiCapability.PDF)),
            AiModel("gemini-1.5-pro", "Gemini 1.5 Pro", setOf(AiCapability.TEXT, AiCapability.VISION, AiCapability.PDF)),
            AiModel("gemini-2.0-flash-exp", "Gemini 2.0 Flash (exp)", setOf(AiCapability.TEXT, AiCapability.VISION)),
        )
    }
}
