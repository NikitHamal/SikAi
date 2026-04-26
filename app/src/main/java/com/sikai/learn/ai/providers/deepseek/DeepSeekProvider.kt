package com.sikai.learn.ai.providers.deepseek

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

class DeepSeekProvider @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
) : AiProvider {
    private var _config: AiProviderConfig = defaultConfig()
    private var _apiKey: String? = null
    private val rest = OpenAiCompatibleClient(client, json)

    fun bind(config: AiProviderConfig, apiKey: String?): DeepSeekProvider {
        _config = config; _apiKey = apiKey; return this
    }

    override val config get() = _config
    override fun capabilities() = setOf(AiCapability.TEXT, AiCapability.STREAMING, AiCapability.THINKING)

    override suspend fun listModels() = withContext(Dispatchers.IO) { fallbackModels() }

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

    companion object {
        const val ID = "deepseek-builtin"
        fun defaultConfig() = AiProviderConfig(
            id = ID,
            type = AiProviderType.DEEPSEEK,
            displayName = "DeepSeek",
            baseUrl = "https://api.deepseek.com",
            requestFormat = AiRequestFormat.OPENAI_COMPATIBLE,
            textModel = "deepseek-chat",
            multimodalModel = null,
            supportsFileUpload = false,
            capabilities = setOf(AiCapability.TEXT, AiCapability.STREAMING, AiCapability.THINKING),
            priority = 70, enabled = true, isBuiltIn = true, needsApiKey = true,
            notes = "Add your DeepSeek API key in Settings.",
        )

        fun fallbackModels(): List<AiModel> = listOf(
            AiModel("deepseek-chat", "DeepSeek Chat", setOf(AiCapability.TEXT)),
            AiModel("deepseek-reasoner", "DeepSeek Reasoner", setOf(AiCapability.TEXT, AiCapability.THINKING), supportsThinking = true),
        )
    }
}
