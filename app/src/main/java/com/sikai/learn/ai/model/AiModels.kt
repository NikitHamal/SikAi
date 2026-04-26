package com.sikai.learn.ai.model

import kotlinx.serialization.Serializable

@Serializable
enum class AiProviderType {
    QWEN, DEEPINFRA, GEMINI, OPENROUTER, NVIDIA, DEEPSEEK, OPENAI_COMPATIBLE, CUSTOM,
}

@Serializable
enum class AiCapability {
    TEXT, VISION, FILE_UPLOAD, PDF, STREAMING, THINKING, SEARCH,
}

@Serializable
enum class AiRequestFormat {
    OPENAI_COMPATIBLE, GEMINI_NATIVE, QWEN_NATIVE, DEEPINFRA_NATIVE, SIMPLE_PROMPT,
}

@Serializable
data class AiModel(
    val id: String,
    val displayName: String,
    val capabilities: Set<AiCapability> = setOf(AiCapability.TEXT),
    val maxContext: Long = 32_000,
    val description: String = "",
    val supportsThinking: Boolean = false,
    val supportsSearch: Boolean = false,
)

@Serializable
data class AiProviderConfig(
    val id: String,
    val type: AiProviderType,
    val displayName: String,
    val baseUrl: String,
    val requestFormat: AiRequestFormat,
    val textModel: String,
    val multimodalModel: String? = null,
    val supportsFileUpload: Boolean = false,
    val capabilities: Set<AiCapability> = setOf(AiCapability.TEXT),
    val priority: Int = 100,
    val enabled: Boolean = true,
    val isBuiltIn: Boolean = false,
    val needsApiKey: Boolean = true,
    val notes: String = "",
)

@Serializable
data class AiAttachment(
    val uri: String,
    val mimeType: String,
    val name: String,
    val sizeBytes: Long,
    val isImage: Boolean,
    val isPdf: Boolean,
    val base64Data: String? = null,
)

@Serializable
data class AiMessage(
    val role: String, // "system" | "user" | "assistant"
    val content: String,
    val attachments: List<AiAttachment> = emptyList(),
)

enum class AiThinkingMode { Off, Auto, Thinking, Fast }

@Serializable
data class AiRequest(
    val messages: List<AiMessage>,
    val systemPrompt: String? = null,
    val preferredCapabilities: Set<AiCapability> = setOf(AiCapability.TEXT),
    val thinkingMode: String = "Auto",
    val enableSearch: Boolean = false,
    val temperature: Float = 0.7f,
    val maxTokens: Int? = null,
    val stream: Boolean = false,
    val conversationId: String? = null,
    val parentMessageId: String? = null,
)

sealed class AiStreamEvent {
    data class Text(val delta: String) : AiStreamEvent()
    data class Thinking(val delta: String) : AiStreamEvent()
    data class Final(val finishReason: String, val fullText: String) : AiStreamEvent()
    data class ConversationUpdate(val conversationId: String?, val parentMessageId: String?) : AiStreamEvent()
    data class Error(val reason: AiFailureReason, val message: String) : AiStreamEvent()
}

@Serializable
data class AiResponse(
    val text: String,
    val thinking: String? = null,
    val providerId: String,
    val modelId: String,
    val conversationId: String? = null,
    val parentMessageId: String? = null,
    val usedFallback: Boolean = false,
    val attemptCount: Int = 1,
)

enum class AiFailureReason {
    RATE_LIMIT, TIMEOUT, SERVER_ERROR, MODEL_UNAVAILABLE, PARSE_ERROR, AUTH, WAF_BLOCK,
    UNSUPPORTED_CAPABILITY, NETWORK, UNKNOWN,
}

sealed class AiProviderResult {
    data class Success(val response: AiResponse) : AiProviderResult()
    data class Failure(val reason: AiFailureReason, val message: String, val providerId: String) : AiProviderResult()
}

enum class ProviderHealthState { Healthy, Degraded, Down, Unknown }
