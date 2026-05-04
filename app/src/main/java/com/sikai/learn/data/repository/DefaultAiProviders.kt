package com.sikai.learn.data.repository

import com.sikai.learn.domain.model.AiCapability
import com.sikai.learn.domain.model.AiProviderConfig
import com.sikai.learn.domain.model.AiRequestFormat

object DefaultAiProviders {
    fun builtIns(): List<AiProviderConfig> = listOf(
        AiProviderConfig(
            id = "qwen",
            type = "qwen",
            displayName = "Qwen",
            baseUrl = "https://chat.qwen.ai",
            apiKeyAlias = null,
            defaultTextModel = "qwen3.6-plus",
            defaultMultimodalModel = "qwen3.5-omni-plus",
            requestFormat = AiRequestFormat.CUSTOM_SIMPLE,
            supportsFileUpload = true,
            capabilities = setOf(
                AiCapability.TEXT, AiCapability.VISION, AiCapability.FILE_UPLOAD,
                AiCapability.PDF, AiCapability.STREAMING, AiCapability.THINKING, AiCapability.SEARCH
            ),
            priority = 0,
            enabled = true,
            isBuiltIn = true,
        ),
    )
}