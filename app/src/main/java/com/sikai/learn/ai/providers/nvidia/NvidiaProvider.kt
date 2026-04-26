package com.sikai.learn.ai.providers.nvidia

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

class NvidiaProvider @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
) : AiProvider {
    private var _config: AiProviderConfig = defaultConfig()
    private var _apiKey: String? = null
    private val rest = OpenAiCompatibleClient(client, json)

    fun bind(config: AiProviderConfig, apiKey: String?): NvidiaProvider {
        _config = config; _apiKey = apiKey; return this
    }

    override val config get() = _config
    override fun capabilities() = setOf(AiCapability.TEXT, AiCapability.STREAMING, AiCapability.VISION)

    override suspend fun listModels() = withContext(Dispatchers.IO) { fallbackModels() }

    override suspend fun generate(request: AiRequest) = withContext(Dispatchers.IO) {
        rest.call(
            baseUrl = config.baseUrl,
            path = "/v1/chat/completions",
            modelId = pickModel(request),
            request = request,
            apiKey = _apiKey,
            providerId = config.id,
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
        const val ID = "nvidia-builtin"
        fun defaultConfig() = AiProviderConfig(
            id = ID,
            type = AiProviderType.NVIDIA,
            displayName = "NVIDIA NIM",
            baseUrl = "https://integrate.api.nvidia.com",
            requestFormat = AiRequestFormat.OPENAI_COMPATIBLE,
            textModel = "meta/llama-3.1-70b-instruct",
            multimodalModel = "meta/llama-3.2-90b-vision-instruct",
            supportsFileUpload = false,
            capabilities = setOf(AiCapability.TEXT, AiCapability.STREAMING, AiCapability.VISION),
            priority = 60, enabled = true, isBuiltIn = true, needsApiKey = true,
            notes = "Add your NVIDIA NIM API key in Settings.",
        )

        fun fallbackModels(): List<AiModel> = listOf(
            AiModel("meta/llama-3.1-70b-instruct", "Llama 3.1 70B Instruct", setOf(AiCapability.TEXT)),
            AiModel("meta/llama-3.1-405b-instruct", "Llama 3.1 405B Instruct", setOf(AiCapability.TEXT)),
            AiModel("meta/llama-3.2-90b-vision-instruct", "Llama 3.2 90B Vision", setOf(AiCapability.TEXT, AiCapability.VISION)),
            AiModel("nvidia/nemotron-4-340b-instruct", "Nemotron-4 340B", setOf(AiCapability.TEXT)),
            AiModel("mistralai/mixtral-8x22b-instruct-v0.1", "Mixtral 8x22B", setOf(AiCapability.TEXT)),
        )
    }
}
