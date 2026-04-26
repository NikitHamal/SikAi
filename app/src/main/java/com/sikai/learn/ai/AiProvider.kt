package com.sikai.learn.ai

import com.sikai.learn.domain.model.AiCapability
import com.sikai.learn.domain.model.AiProviderConfig
import com.sikai.learn.domain.model.AiProviderResult
import com.sikai.learn.domain.model.AiRequest

/**
 * The single contract every concrete AI integration implements. The runtime
 * never calls a provider's HTTP code directly — it goes through this. That is
 * what lets the fallback chain be provider-agnostic and what lets users add
 * custom OpenAI-compatible servers without code changes.
 */
interface AiProvider {

    val id: String
    val displayName: String
    val capabilities: Set<AiCapability>

    fun supports(request: AiRequest): Boolean

    suspend fun complete(
        config: AiProviderConfig,
        request: AiRequest,
    ): AiProviderResult
}
