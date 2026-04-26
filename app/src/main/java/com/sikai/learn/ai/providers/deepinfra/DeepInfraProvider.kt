package com.sikai.learn.ai.providers.deepinfra

import com.sikai.learn.ai.model.AiCapability
import com.sikai.learn.ai.model.AiFailureReason
import com.sikai.learn.ai.model.AiModel
import com.sikai.learn.ai.model.AiProviderConfig
import com.sikai.learn.ai.model.AiProviderResult
import com.sikai.learn.ai.model.AiProviderType
import com.sikai.learn.ai.model.AiRequest
import com.sikai.learn.ai.model.AiRequestFormat
import com.sikai.learn.ai.model.AiResponse
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

/**
 * DeepInfra provider — free OpenAI-compatible endpoint at api.deepinfra.com.
 * The /v1/openai/chat/completions endpoint accepts requests without an API key for
 * a number of community models, which keeps SikAi working out of the box.
 */
class DeepInfraProvider @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
) : AiProvider {

    private var _config: AiProviderConfig = defaultConfig()
    private var _apiKey: String? = null
    private val rest = OpenAiCompatibleClient(client, json)

    fun bind(config: AiProviderConfig, apiKey: String?): DeepInfraProvider {
        _config = config; _apiKey = apiKey; return this
    }

    override val config: AiProviderConfig get() = _config

    override fun capabilities(): Set<AiCapability> =
        setOf(AiCapability.TEXT, AiCapability.STREAMING, AiCapability.VISION)

    override suspend fun listModels(): List<AiModel> = withContext(Dispatchers.IO) { fallbackModels() }

    override suspend fun generate(request: AiRequest): AiProviderResult = withContext(Dispatchers.IO) {
        rest.call(
            baseUrl = config.baseUrl,
            path = "/v1/openai/chat/completions",
            modelId = pickModel(request),
            request = request,
            apiKey = _apiKey,
            providerId = config.id,
            extraHeaders = mapOf("X-Source" to "sikai"),
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

    override suspend fun testConnection(): AiProviderResult {
        val ping = AiRequest(messages = listOf(com.sikai.learn.ai.model.AiMessage("user", "ping")), maxTokens = 4)
        return when (val r = generate(ping)) {
            is AiProviderResult.Success -> r
            is AiProviderResult.Failure -> r
        }
    }

    private fun pickModel(req: AiRequest): String {
        val needsVision = req.preferredCapabilities.contains(AiCapability.VISION) || req.messages.any { it.attachments.any() }
        return if (needsVision) config.multimodalModel ?: config.textModel else config.textModel
    }

    companion object {
        const val ID = "deepinfra-builtin"
        fun defaultConfig() = AiProviderConfig(
            id = ID,
            type = AiProviderType.DEEPINFRA,
            displayName = "DeepInfra (free)",
            baseUrl = "https://api.deepinfra.com",
            requestFormat = AiRequestFormat.OPENAI_COMPATIBLE,
            textModel = "meta-llama/Meta-Llama-3.1-70B-Instruct",
            multimodalModel = "meta-llama/Llama-3.2-90B-Vision-Instruct",
            supportsFileUpload = false,
            capabilities = setOf(AiCapability.TEXT, AiCapability.STREAMING, AiCapability.VISION),
            priority = 20,
            enabled = true,
            isBuiltIn = true,
            needsApiKey = false,
            notes = "Free; accepts OpenAI-compatible requests at /v1/openai/chat/completions.",
        )

        fun fallbackModels(): List<AiModel> = listOf(
            AiModel("meta-llama/Meta-Llama-3.1-70B-Instruct", "Llama 3.1 70B Instruct", setOf(AiCapability.TEXT)),
            AiModel("meta-llama/Meta-Llama-3.1-8B-Instruct", "Llama 3.1 8B Instruct", setOf(AiCapability.TEXT)),
            AiModel("Qwen/Qwen2.5-72B-Instruct", "Qwen 2.5 72B Instruct", setOf(AiCapability.TEXT)),
            AiModel("meta-llama/Llama-3.2-90B-Vision-Instruct", "Llama 3.2 90B Vision", setOf(AiCapability.TEXT, AiCapability.VISION)),
            AiModel("microsoft/Phi-3-medium-4k-instruct", "Phi-3 Medium", setOf(AiCapability.TEXT)),
        )
    }
}
