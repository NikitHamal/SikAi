package com.sikai.learn.ai

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Routes a [AiProviderConfig] to its concrete [AiProvider]. Custom providers
 * fall through to a generic OpenAI-compatible runner so users can add any
 * Chat Completions-shaped server in Settings.
 */
@Singleton
class AiProviderRegistry @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards AiProvider>,
    @Named("custom_openai_compatible") private val customFallback: AiProvider,
) {

    private val byId = providers.associateBy { it.id }

    fun byType(typeId: String): AiProvider? {
        val direct = byId[typeId]
        if (direct != null) return direct
        return when {
            typeId.startsWith("custom:") -> customFallback
            typeId == "openai_compatible" -> customFallback
            else -> null
        }
    }

    fun all(): Collection<AiProvider> = providers
}
