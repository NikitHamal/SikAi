package com.sikai.learn.ai.providers.openrouter

import com.sikai.learn.ai.model.AiCapability
import com.sikai.learn.ai.model.AiMessage
import com.sikai.learn.ai.model.AiModel
import com.sikai.learn.ai.model.AiProviderConfig
import com.sikai.learn.ai.model.AiProviderResult
import com.sikai.learn.ai.model.AiProviderType
import com.sikai.learn.ai.model.AiRequest
import com.sikai.learn.ai.model.AiRequestFormat
import com.sikai.learn.ai.model.AiStreamEvent
import com.sikai.learn.ai.provider.AiProvider
import com.sikai.learn.ai.providers.OpenAiCompatibleClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Inject

class OpenRouterProvider @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
) : AiProvider {
    private var _config: AiProviderConfig = defaultConfig()
    private var _apiKey: String? = null
    private val rest = OpenAiCompatibleClient(client, json)

    fun bind(config: AiProviderConfig, apiKey: String?): OpenRouterProvider {
        _config = config; _apiKey = apiKey; return this
    }

    override val config get() = _config
    override fun capabilities() = setOf(AiCapability.TEXT, AiCapability.STREAMING, AiCapability.VISION)

    override suspend fun listModels() = withContext(Dispatchers.IO) { fallbackModels() }

    override suspend fun generate(request: AiRequest) = withContext(Dispatchers.IO) {
        rest.call(
            baseUrl = config.baseUrl,
            path = "/api/v1/chat/completions",
            modelId = pickModel(request),
            request = request,
            apiKey = _apiKey,
            providerId = config.id,
            extraHeaders = mapOf(
                "HTTP-Referer" to "https://sikai.app",
                "X-Title" to "SikAi",
            ),
        )
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

    private fun pickModel(req: AiRequest): String {
        val needsVision = req.preferredCapabilities.contains(AiCapability.VISION) || req.messages.any { it.attachments.any() }
        return if (needsVision) config.multimodalModel ?: config.textModel else config.textModel
    }

    companion object {
        const val ID = "openrouter-builtin"
        fun defaultConfig() = AiProviderConfig(
            id = ID,
            type = AiProviderType.OPENROUTER,
            displayName = "OpenRouter",
            baseUrl = "https://openrouter.ai",
            requestFormat = AiRequestFormat.OPENAI_COMPATIBLE,
            textModel = "google/gemini-flash-1.5",
            multimodalModel = "google/gemini-flash-1.5",
            supportsFileUpload = false,
            capabilities = setOf(AiCapability.TEXT, AiCapability.STREAMING, AiCapability.VISION),
            priority = 50, enabled = true, isBuiltIn = true, needsApiKey = true,
            notes = "Add your OpenRouter API key in Settings to enable.",
        )

        fun fallbackModels(): List<AiModel> = listOf(
            AiModel("google/gemini-flash-1.5", "Gemini Flash 1.5", setOf(AiCapability.TEXT, AiCapability.VISION)),
            AiModel("google/gemini-pro-1.5", "Gemini Pro 1.5", setOf(AiCapability.TEXT, AiCapability.VISION)),
            AiModel("anthropic/claude-3.5-sonnet", "Claude 3.5 Sonnet", setOf(AiCapability.TEXT, AiCapability.VISION)),
            AiModel("openai/gpt-4o-mini", "GPT-4o mini", setOf(AiCapability.TEXT, AiCapability.VISION)),
            AiModel("meta-llama/llama-3.1-70b-instruct", "Llama 3.1 70B", setOf(AiCapability.TEXT)),
        )
    }
}
