package com.sikai.learn.ai.providers.qwen

import com.sikai.learn.ai.model.AiCapability
import com.sikai.learn.ai.model.AiModel
import com.sikai.learn.ai.model.AiProviderConfig
import com.sikai.learn.ai.model.AiProviderType
import com.sikai.learn.ai.model.AiRequestFormat

object QwenDefaults {
    const val ID = "qwen-builtin"
    const val BASE = "https://chat.qwen.ai"

    fun config(): AiProviderConfig = AiProviderConfig(
        id = ID,
        type = AiProviderType.QWEN,
        displayName = "Qwen (free)",
        baseUrl = BASE,
        requestFormat = AiRequestFormat.QWEN_NATIVE,
        textModel = "qwen3.6-plus",
        multimodalModel = "qwen3.5-omni-plus",
        supportsFileUpload = true,
        capabilities = setOf(
            AiCapability.TEXT, AiCapability.VISION, AiCapability.FILE_UPLOAD,
            AiCapability.PDF, AiCapability.STREAMING, AiCapability.THINKING, AiCapability.SEARCH,
        ),
        priority = 10, // primary
        enabled = true,
        isBuiltIn = true,
        needsApiKey = false,
        notes = "Free, no API key required. Optional token can be added in Settings.",
    )

    fun fallbackModels(): List<AiModel> = listOf(
        AiModel(
            id = "qwen3.6-plus",
            displayName = "Qwen3.6-Plus",
            capabilities = setOf(AiCapability.TEXT, AiCapability.STREAMING, AiCapability.THINKING),
            maxContext = 1_000_000,
            description = "Latest Qwen3.6 plus model",
            supportsThinking = true,
        ),
        AiModel(
            id = "qwen3.5-plus",
            displayName = "Qwen3.5-Plus",
            capabilities = setOf(AiCapability.TEXT, AiCapability.STREAMING, AiCapability.THINKING),
            maxContext = 1_000_000,
            description = "Qwen3.5 plus model",
            supportsThinking = true,
        ),
        AiModel(
            id = "qwen3.5-omni-plus",
            displayName = "Qwen3.5-Omni-Plus",
            capabilities = setOf(AiCapability.TEXT, AiCapability.VISION, AiCapability.FILE_UPLOAD, AiCapability.PDF, AiCapability.STREAMING),
            maxContext = 262_144,
            description = "Multimodal: text + image + audio + video",
        ),
    )
}
