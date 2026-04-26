package com.sikai.learn.ai.provider

import com.sikai.learn.ai.model.AiCapability
import com.sikai.learn.ai.model.AiFailureReason
import com.sikai.learn.ai.model.AiProviderResult
import com.sikai.learn.ai.model.AiRequest
import com.sikai.learn.core.storage.ProviderLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tries providers in priority order until one succeeds, respecting capability requirements.
 * For text requests it falls back to next TEXT-capable provider; for vision/file requests
 * it only falls back to providers that declare VISION or FILE_UPLOAD capability.
 *
 * Failures are logged locally (never with API keys) so the Settings → Provider logs
 * screen can show the user why a request was retried.
 */
@Singleton
class AiOrchestrator @Inject constructor(
    private val registry: AiProviderRegistry,
    private val logger: ProviderLogger,
) {

    suspend fun generate(request: AiRequest, preferredProviderId: String? = null): AiProviderResult {
        val candidates = pickCandidates(request, preferredProviderId)
        if (candidates.isEmpty()) {
            return AiProviderResult.Failure(
                AiFailureReason.UNSUPPORTED_CAPABILITY,
                "No provider supports the requested capabilities",
                providerId = "<orchestrator>",
            )
        }

        var lastFailure: AiProviderResult.Failure? = null
        candidates.forEachIndexed { idx, provider ->
            val result = runCatching { provider.generate(request) }.getOrElse { t ->
                AiProviderResult.Failure(
                    AiFailureReason.UNKNOWN,
                    t.message ?: t::class.java.simpleName,
                    provider.config.id,
                )
            }
            when (result) {
                is AiProviderResult.Success -> {
                    val response = result.response.copy(
                        usedFallback = idx > 0,
                        attemptCount = idx + 1,
                    )
                    logger.logSuccess(provider.config.id, response.modelId)
                    return AiProviderResult.Success(response)
                }
                is AiProviderResult.Failure -> {
                    lastFailure = result
                    logger.logFailure(provider.config.id, result.reason.name, result.message)
                    if (!shouldFallback(result.reason)) return result
                }
            }
        }
        return lastFailure ?: AiProviderResult.Failure(
            AiFailureReason.UNKNOWN, "All providers failed", "<orchestrator>",
        )
    }

    private fun pickCandidates(request: AiRequest, preferredProviderId: String?): List<AiProvider> {
        val needsVision = request.preferredCapabilities.contains(AiCapability.VISION) ||
            request.messages.any { msg -> msg.attachments.any { it.isImage } }
        val needsFile = request.preferredCapabilities.contains(AiCapability.FILE_UPLOAD) ||
            request.messages.any { msg -> msg.attachments.any { !it.isImage } }

        val all = registry.activeProviders().filter { it.config.enabled }
        val filtered = all.filter { p ->
            val caps = p.capabilities()
            (!needsVision || caps.contains(AiCapability.VISION)) &&
                (!needsFile || (caps.contains(AiCapability.FILE_UPLOAD) || caps.contains(AiCapability.PDF) || caps.contains(AiCapability.VISION)))
        }

        val sorted = filtered.sortedWith(
            compareByDescending<AiProvider> { it.config.id == preferredProviderId }
                .thenBy { it.config.priority },
        )
        return sorted
    }

    private fun shouldFallback(reason: AiFailureReason): Boolean = when (reason) {
        AiFailureReason.RATE_LIMIT,
        AiFailureReason.TIMEOUT,
        AiFailureReason.SERVER_ERROR,
        AiFailureReason.MODEL_UNAVAILABLE,
        AiFailureReason.PARSE_ERROR,
        AiFailureReason.WAF_BLOCK,
        AiFailureReason.NETWORK -> true
        AiFailureReason.AUTH,
        AiFailureReason.UNSUPPORTED_CAPABILITY,
        AiFailureReason.UNKNOWN -> true
    }
}
