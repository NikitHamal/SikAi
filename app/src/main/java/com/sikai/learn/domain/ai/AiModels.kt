package com.sikai.learn.domain.ai

import kotlinx.serialization.Serializable

enum class AiProviderType { QWEN, DEEPINFRA, GEMINI, OPENROUTER, NVIDIA, DEEPSEEK, CUSTOM_OPENAI, CUSTOM_GEMINI, CUSTOM_SIMPLE }
enum class AiCapability { TEXT, VISION, FILE_UPLOAD, PDF, STREAMING }
enum class AiRole { SYSTEM, USER, ASSISTANT }
enum class AiPromptMode { SOCRATIC, DIRECT, SIMPLE, EXAM, SOLVE_STEPS, GENERATE_QUIZ, SUMMARIZE_NOTE, FLASHCARDS, STUDY_PLANNER, WEAKNESS_ANALYSIS }
enum class AiFailureReason { RATE_LIMIT, TIMEOUT, SERVER_ERROR, UNAVAILABLE_MODEL, PARSING_ERROR, AUTH_REQUIRED, NETWORK, UNSUPPORTED_CAPABILITY, SAFETY_BLOCKED, UNKNOWN }
enum class ProviderHealthState { UNKNOWN, HEALTHY, DEGRADED, OFFLINE, AUTH_REQUIRED }
enum class AiRequestFormat { OPENAI_COMPATIBLE, GEMINI_COMPATIBLE, CUSTOM_SIMPLE }

@Serializable
data class AiProviderConfig(
    val id: String,
    val name: String,
    val type: AiProviderType,
    val baseUrl: String,
    val apiKeyAlias: String? = null,
    val textModel: String,
    val multimodalModel: String,
    val capabilities: Set<AiCapability>,
    val priority: Int,
    val enabled: Boolean = true,
    val requestFormat: AiRequestFormat = AiRequestFormat.OPENAI_COMPATIBLE,
    val supportsSearch: Boolean = false,
    val supportsThinking: Boolean = false
)

@Serializable
data class AiModel(
    val id: String,
    val name: String = id,
    val capabilities: Set<AiCapability> = setOf(AiCapability.TEXT),
    val contextWindow: Int? = null
)

@Serializable
data class AiMessage(
    val role: AiRole,
    val content: String,
    val attachments: List<AiAttachment> = emptyList()
)

@Serializable
data class AiAttachment(
    val uri: String,
    val mimeType: String,
    val fileName: String,
    val bytesBase64: String? = null,
    val providerFileId: String? = null
)

@Serializable
data class AiRequest(
    val messages: List<AiMessage>,
    val classLevel: Int,
    val subject: String,
    val mode: AiPromptMode,
    val preferredProviderId: String? = null,
    val modelOverride: String? = null,
    val enableSearch: Boolean = false,
    val enableThinking: Boolean = false,
    val stream: Boolean = false
)

@Serializable
data class AiResponse(
    val text: String,
    val providerId: String,
    val providerName: String,
    val model: String,
    val usedFallback: Boolean,
    val tokenCount: Int? = null
)

sealed interface AiProviderResult {
    data class Success(val response: AiResponse) : AiProviderResult
    data class Failure(val providerId: String, val reason: AiFailureReason, val message: String, val retryable: Boolean = true) : AiProviderResult
}
