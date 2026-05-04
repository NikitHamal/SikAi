package com.sikai.learn.domain.model

import kotlinx.serialization.Serializable

/**
 * The set of canonical providers we ship support for. Custom providers can also
 * be configured by the user, modelled with [AiProviderType.Custom].
 */
@Serializable
sealed class AiProviderType(val id: String) {
    @Serializable object Qwen : AiProviderType("qwen")
    @Serializable object OpenAiCompatible : AiProviderType("openai_compatible")
    @Serializable data class Custom(val customId: String) : AiProviderType("custom:$customId")

    companion object {
        fun fromId(id: String): AiProviderType = when (id) {
            "qwen" -> Qwen
            "openai_compatible" -> OpenAiCompatible
            else -> if (id.startsWith("custom:")) Custom(id.removePrefix("custom:")) else Custom(id)
        }
    }
}

@Serializable
enum class AiCapability { TEXT, VISION, FILE_UPLOAD, PDF, STREAMING, SEARCH, THINKING, AUDIO, VIDEO }

/**
 * Wire-format hint for custom providers. The built-in providers know their own
 * format; this only controls how requests are serialised for [AiProviderType.Custom]
 * and [AiProviderType.OpenAiCompatible].
 */
@Serializable
enum class AiRequestFormat { OPENAI_COMPATIBLE, GEMINI_COMPATIBLE, CUSTOM_SIMPLE }

@Serializable
@Serializable
data class AiModel(
    val id: String,
    val displayName: String,
    val providerId: String,
    val capabilities: Set<AiCapability> = setOf(AiCapability.TEXT),
    val maxContext: Int = 32768,
    val description: String = "",
    val supportsThinking: Boolean = false,
    val supportsSearch: Boolean = false,
)

@Serializable
data class AiProviderConfig(
    val id: String,                       // unique storage key
    val type: String,                     // one of AiProviderType.id values, or custom:<id>
    val displayName: String,
    val baseUrl: String? = null,
    val apiKeyAlias: String? = null,      // points at EncryptedKeyStore entry; null = no key required
    val defaultTextModel: String? = null,
    val defaultMultimodalModel: String? = null,
    val requestFormat: AiRequestFormat = AiRequestFormat.OPENAI_COMPATIBLE,
    val supportsFileUpload: Boolean = false,
    val capabilities: Set<AiCapability> = setOf(AiCapability.TEXT),
    val priority: Int = 0,                // lower = tried earlier in fallback
    val enabled: Boolean = true,
    val isBuiltIn: Boolean = false,
)

@Serializable
data class AiAttachment(
    val uri: String,                      // local content:// or file:// uri
    val mimeType: String,
    val sizeBytes: Long = 0,
    val displayName: String = "attachment",
)

@Serializable
enum class AiMessageRole { SYSTEM, USER, ASSISTANT, TOOL }

@Serializable
data class AiMessage(
    val role: AiMessageRole,
    val content: String,
    val attachments: List<AiAttachment> = emptyList(),
)

/** Hint about which kind of work the request is. Drives provider/model picking. */
@Serializable
enum class AiTask { TEXT_CHAT, EXPLAIN, SOLVE_FILE, GENERATE_QUIZ, SUMMARISE, FLASHCARDS, STUDY_PLAN, WEAKNESS_ANALYSIS }

@Serializable
data class AiRequest(
    val task: AiTask,
    val messages: List<AiMessage>,
    val classLevel: Int? = null,           // 8 / 10 / 12
    val subject: String? = null,
    val topic: String? = null,
    val mode: AiMode = AiMode.SimpleExplanation,
    val preferredProviderId: String? = null,
    val preferredModelId: String? = null,
    val temperature: Double = 0.4,
    val maxOutputTokens: Int = 1500,
    val stream: Boolean = true,
)

@Serializable
enum class AiMode { Socratic, DirectAnswer, SimpleExplanation, ExamFocused, StepByStep }

@Serializable
sealed class AiFailureReason {
    @Serializable object Network : AiFailureReason()
    @Serializable object Timeout : AiFailureReason()
    @Serializable object RateLimit : AiFailureReason()
    @Serializable object ServerError : AiFailureReason()
    @Serializable object Auth : AiFailureReason()
    @Serializable object Parsing : AiFailureReason()
    @Serializable object Waf : AiFailureReason()
    @Serializable object UnsupportedCapability : AiFailureReason()
    @Serializable data class Unknown(val message: String) : AiFailureReason()
}

@Serializable
data class AiUsage(
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
    val totalTokens: Int? = null,
)

@Serializable
data class AiResponse(
    val text: String,
    val providerId: String,
    val providerLabel: String,
    val modelId: String?,
    val usage: AiUsage? = null,
    val reasoning: String? = null,
)

sealed interface AiProviderResult {
    data class Success(val response: AiResponse) : AiProviderResult
    data class Failure(val reason: AiFailureReason, val providerId: String, val message: String) : AiProviderResult
}

@Serializable
enum class ProviderHealthState { Unknown, Healthy, Degraded, Down }
