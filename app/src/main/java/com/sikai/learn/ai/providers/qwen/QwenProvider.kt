package com.sikai.learn.ai.providers.qwen

import com.sikai.learn.ai.model.AiCapability
import com.sikai.learn.ai.model.AiFailureReason
import com.sikai.learn.ai.model.AiMessage
import com.sikai.learn.ai.model.AiModel
import com.sikai.learn.ai.model.AiProviderConfig
import com.sikai.learn.ai.model.AiProviderResult
import com.sikai.learn.ai.model.AiRequest
import com.sikai.learn.ai.model.AiResponse
import com.sikai.learn.ai.model.AiStreamEvent
import com.sikai.learn.ai.model.ProviderHealthState
import com.sikai.learn.ai.provider.AiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * Qwen primary provider — chat.qwen.ai, free / no-auth (token optional).
 *
 * This is a pragmatic Kotlin port of the Flashy reference implementation. It handles:
 * - cookie-driven anonymous auth + bx-ua/midtoken header rotation
 * - WAF/Cloudflare detection with retries (up to 5)
 * - dynamic model fetching (with a static fallback list)
 * - file/image upload through Qwen's STS upload flow
 * - streaming chat completions via SSE
 * - thinking modes (Auto / Thinking / Fast) and search/deep-research chat types
 */
class QwenProvider @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
) : AiProvider {

    private var _config: AiProviderConfig = QwenDefaults.config()
    private var _apiKey: String? = null
    private val core: QwenCore by lazy { QwenCore(client, json) }

    fun bind(config: AiProviderConfig, apiKey: String?): QwenProvider {
        _config = config
        _apiKey = apiKey
        return this
    }

    override val config: AiProviderConfig get() = _config
    override val health: ProviderHealthState get() = ProviderHealthState.Unknown

    override fun capabilities(): Set<AiCapability> = setOf(
        AiCapability.TEXT, AiCapability.VISION, AiCapability.FILE_UPLOAD, AiCapability.PDF,
        AiCapability.STREAMING, AiCapability.THINKING, AiCapability.SEARCH,
    )

    override suspend fun listModels(): List<AiModel> = withContext(Dispatchers.IO) {
        runCatching { core.fetchModels() }.getOrDefault(QwenDefaults.fallbackModels())
    }

    override suspend fun generate(request: AiRequest): AiProviderResult = withContext(Dispatchers.IO) {
        val modelId = pickModel(request)
        runCatching {
            val response = core.generateOnce(
                modelId = modelId,
                messages = request.messages,
                systemPrompt = request.systemPrompt,
                token = _apiKey?.takeIf { it.isNotBlank() },
                thinkingMode = request.thinkingMode,
                thinkingEnabled = request.thinkingMode != "Off",
                chatType = if (request.enableSearch) "search" else "t2t",
                conversationId = request.conversationId,
                parentMessageId = request.parentMessageId,
            )
            AiProviderResult.Success(
                AiResponse(
                    text = response.text,
                    thinking = response.thinking,
                    providerId = config.id,
                    modelId = modelId,
                    conversationId = response.chatId,
                    parentMessageId = response.parentMessageId,
                ),
            )
        }.getOrElse { t ->
            val reason = when (t) {
                is QwenWafException -> AiFailureReason.WAF_BLOCK
                is QwenRateLimitException -> AiFailureReason.RATE_LIMIT
                is QwenAuthException -> AiFailureReason.AUTH
                else -> AiFailureReason.NETWORK
            }
            AiProviderResult.Failure(reason, t.message ?: t::class.java.simpleName, config.id)
        }
    }

    override fun stream(request: AiRequest): Flow<AiStreamEvent> = flow {
        val modelId = pickModel(request)
        try {
            core.generateStream(
                modelId = modelId,
                messages = request.messages,
                systemPrompt = request.systemPrompt,
                token = _apiKey?.takeIf { it.isNotBlank() },
                thinkingMode = request.thinkingMode,
                thinkingEnabled = request.thinkingMode != "Off",
                chatType = if (request.enableSearch) "search" else "t2t",
                conversationId = request.conversationId,
                parentMessageId = request.parentMessageId,
                emit = { event -> emit(event) },
            )
        } catch (t: Throwable) {
            val reason = when (t) {
                is QwenWafException -> AiFailureReason.WAF_BLOCK
                is QwenRateLimitException -> AiFailureReason.RATE_LIMIT
                is QwenAuthException -> AiFailureReason.AUTH
                else -> AiFailureReason.NETWORK
            }
            emit(AiStreamEvent.Error(reason, t.message ?: "stream failed"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun testConnection(): AiProviderResult = withContext(Dispatchers.IO) {
        runCatching { core.fetchModels() }
            .map { AiProviderResult.Success(AiResponse("ok", providerId = config.id, modelId = config.textModel)) }
            .getOrElse { AiProviderResult.Failure(AiFailureReason.NETWORK, it.message ?: "failed", config.id) }
    }

    private fun pickModel(request: AiRequest): String {
        val needsVision = request.preferredCapabilities.contains(AiCapability.VISION) ||
            request.messages.any { it.attachments.any() }
        return if (needsVision) config.multimodalModel ?: config.textModel else config.textModel
    }
}

class QwenWafException(message: String) : RuntimeException(message)
class QwenRateLimitException(message: String) : RuntimeException(message)
class QwenAuthException(message: String) : RuntimeException(message)
