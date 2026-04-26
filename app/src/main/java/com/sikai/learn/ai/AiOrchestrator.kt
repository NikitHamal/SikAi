package com.sikai.learn.ai

import com.sikai.learn.data.local.ProviderLogDao
import com.sikai.learn.data.local.ProviderLogEntity
import com.sikai.learn.data.repository.AiProviderRepository
import com.sikai.learn.domain.model.AiCapability
import com.sikai.learn.domain.model.AiFailureReason
import com.sikai.learn.domain.model.AiProviderConfig
import com.sikai.learn.domain.model.AiProviderResult
import com.sikai.learn.domain.model.AiRequest
import com.sikai.learn.domain.model.AiTask
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Routes a request through the configured providers in priority order, falling
 * over from one to the next on retryable failures. The selection rules:
 *  - SOLVE_FILE prefers providers with VISION or FILE_UPLOAD capability
 *  - text tasks accept any TEXT-capable provider
 *  - the explicit [AiRequest.preferredProviderId] is tried first when present
 *  - failures with [AiFailureReason.Auth] do NOT retry on the same provider
 *  - logs every attempt so users can see what happened in Settings
 */
@Singleton
class AiOrchestrator @Inject constructor(
    private val providers: AiProviderRegistry,
    private val configs: AiProviderRepository,
    private val log: ProviderLogDao,
) {

    suspend fun complete(request: AiRequest): AiProviderResult {
        val ordered = orderedConfigs(request)
        if (ordered.isEmpty()) {
            return AiProviderResult.Failure(
                AiFailureReason.UnsupportedCapability,
                providerId = "",
                message = "No AI provider is configured. Open Settings → AI providers to add one."
            )
        }

        var lastFailure: AiProviderResult.Failure? = null
        for (cfg in ordered) {
            val provider = providers.byType(cfg.type) ?: continue
            if (!provider.supports(request)) continue
            if (!matchesCapability(provider, request)) continue

            val started = System.currentTimeMillis()
            val outcome = runCatching { provider.complete(cfg, request) }
                .getOrElse {
                    AiProviderResult.Failure(
                        AiFailureReason.Unknown(it.message ?: it::class.simpleName.orEmpty()),
                        provider.id,
                        it.message ?: ""
                    )
                }
            val elapsed = System.currentTimeMillis() - started

            log.insert(
                ProviderLogEntity(
                    providerId = cfg.id,
                    task = request.task.name,
                    success = outcome is AiProviderResult.Success,
                    failureReason = (outcome as? AiProviderResult.Failure)?.reason?.let { it::class.simpleName },
                    message = (outcome as? AiProviderResult.Failure)?.message,
                    tookMs = elapsed,
                    timestampMillis = System.currentTimeMillis(),
                )
            )

            if (outcome is AiProviderResult.Success) return outcome
            val failure = outcome as AiProviderResult.Failure
            lastFailure = failure
            if (!isRetryable(failure.reason)) return failure
        }
        return lastFailure ?: AiProviderResult.Failure(
            AiFailureReason.Unknown("no provider produced a response"),
            "",
            "All providers exhausted"
        )
    }

    private suspend fun orderedConfigs(request: AiRequest): List<AiProviderConfig> {
        val all = configs.enabledOrdered()
        if (request.preferredProviderId.isNullOrBlank()) return all
        val (preferred, rest) = all.partition { it.id == request.preferredProviderId }
        return preferred + rest
    }

    private fun matchesCapability(p: AiProvider, req: AiRequest): Boolean = when (req.task) {
        AiTask.SOLVE_FILE -> p.capabilities.any {
            it == AiCapability.VISION || it == AiCapability.FILE_UPLOAD || it == AiCapability.PDF
        }
        else -> AiCapability.TEXT in p.capabilities
    }

    private fun isRetryable(reason: AiFailureReason): Boolean = when (reason) {
        AiFailureReason.Auth -> false
        AiFailureReason.UnsupportedCapability -> false
        else -> true
    }
}
