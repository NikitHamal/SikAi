package com.sikai.learn.ai.providers.custom

import com.sikai.learn.ai.model.AiCapability
import com.sikai.learn.ai.model.AiMessage
import com.sikai.learn.ai.model.AiModel
import com.sikai.learn.ai.model.AiProviderConfig
import com.sikai.learn.ai.model.AiProviderResult
import com.sikai.learn.ai.model.AiRequest
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
 * User-defined OpenAI-compatible provider. The user configures base URL, models, and
 * an API key in Settings → AI providers → Add custom. Used as the catch-all for any
 * service that exposes an OpenAI-shaped /v1/chat/completions endpoint.
 */
class CustomOpenAiCompatibleProvider @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
) : AiProvider {

    private var _config: AiProviderConfig = stub()
    private var _apiKey: String? = null
    private val rest = OpenAiCompatibleClient(client, json)

    fun bind(config: AiProviderConfig, apiKey: String?): CustomOpenAiCompatibleProvider {
        _config = config; _apiKey = apiKey; return this
    }

    override val config get() = _config

    override suspend fun listModels() = withContext(Dispatchers.IO) {
        listOf(AiModel(config.textModel, config.textModel, setOf(AiCapability.TEXT)))
    }

    override suspend fun generate(request: AiRequest) = withContext(Dispatchers.IO) {
        rest.call(
            baseUrl = config.baseUrl,
            path = "/v1/chat/completions",
            modelId = config.textModel,
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

    private fun stub() = AiProviderConfig(
        id = "custom-stub",
        type = com.sikai.learn.ai.model.AiProviderType.CUSTOM,
        displayName = "Custom",
        baseUrl = "https://api.example.com",
        requestFormat = com.sikai.learn.ai.model.AiRequestFormat.OPENAI_COMPATIBLE,
        textModel = "model",
        multimodalModel = null,
        supportsFileUpload = false,
        priority = 200, enabled = false, isBuiltIn = false, needsApiKey = true,
    )
}
