package com.sikai.learn.ai.provider

import com.sikai.learn.ai.model.AiCapability
import com.sikai.learn.ai.model.AiModel
import com.sikai.learn.ai.model.AiProviderConfig
import com.sikai.learn.ai.model.AiProviderResult
import com.sikai.learn.ai.model.AiRequest
import com.sikai.learn.ai.model.AiStreamEvent
import com.sikai.learn.ai.model.ProviderHealthState
import kotlinx.coroutines.flow.Flow

/**
 * Generic AI provider abstraction. All providers (Qwen, DeepInfra, Gemini, OpenRouter,
 * NVIDIA, DeepSeek, custom OpenAI-compatible) implement this interface.
 *
 * Implementations are stateless other than caches; conversation continuity is opaque
 * to callers and surfaced through AiResponse.conversationId/parentMessageId.
 */
interface AiProvider {
    val config: AiProviderConfig
    val health: ProviderHealthState get() = ProviderHealthState.Unknown

    /** Capabilities this provider currently supports. */
    fun capabilities(): Set<AiCapability> = config.capabilities

    /** Returns the list of models available from this provider, fetched dynamically when possible. */
    suspend fun listModels(): List<AiModel>

    /**
     * Submits a request and returns a single result. Concrete providers may internally
     * stream and aggregate; the caller does not need to differentiate.
     */
    suspend fun generate(request: AiRequest): AiProviderResult

    /** Optional streaming flow for providers/UIs that prefer event-driven rendering. */
    fun stream(request: AiRequest): Flow<AiStreamEvent>

    /** Lightweight health probe used by the Settings "Test connection" button. */
    suspend fun testConnection(): AiProviderResult
}
