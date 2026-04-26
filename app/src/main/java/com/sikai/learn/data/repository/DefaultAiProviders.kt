package com.sikai.learn.data.repository

import com.sikai.learn.domain.model.AiCapability
import com.sikai.learn.domain.model.AiProviderConfig
import com.sikai.learn.domain.model.AiRequestFormat

/**
 * Default AI provider seeds. Qwen and DeepInfra are auth-free, so they ship
 * enabled by default and form the free fallback chain. The rest start
 * disabled and become useful once the user adds an API key in Settings.
 */
object DefaultAiProviders {
    fun builtIns(): List<AiProviderConfig> = listOf(
        AiProviderConfig(
            id = "qwen",
            type = "qwen",
            displayName = "Qwen (free)",
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
        AiProviderConfig(
            id = "deepinfra",
            type = "deepinfra",
            displayName = "DeepInfra (free)",
            baseUrl = "https://api.deepinfra.com/v1/openai",
            apiKeyAlias = null,
            defaultTextModel = "deepseek-ai/DeepSeek-V3.2",
            defaultMultimodalModel = null,
            requestFormat = AiRequestFormat.OPENAI_COMPATIBLE,
            supportsFileUpload = false,
            capabilities = setOf(AiCapability.TEXT, AiCapability.STREAMING),
            priority = 1,
            enabled = true,
            isBuiltIn = true,
        ),
        AiProviderConfig(
            id = "gemini",
            type = "gemini",
            displayName = "Google Gemini",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            apiKeyAlias = "provider_key_gemini",
            defaultTextModel = "gemini-1.5-flash",
            defaultMultimodalModel = "gemini-1.5-flash",
            requestFormat = AiRequestFormat.GEMINI_COMPATIBLE,
            supportsFileUpload = true,
            capabilities = setOf(
                AiCapability.TEXT, AiCapability.VISION, AiCapability.PDF, AiCapability.STREAMING
            ),
            priority = 2,
            enabled = false,
            isBuiltIn = true,
        ),
        AiProviderConfig(
            id = "openrouter",
            type = "openrouter",
            displayName = "OpenRouter",
            baseUrl = "https://openrouter.ai/api/v1",
            apiKeyAlias = "provider_key_openrouter",
            defaultTextModel = "openai/gpt-4o-mini",
            defaultMultimodalModel = "openai/gpt-4o-mini",
            requestFormat = AiRequestFormat.OPENAI_COMPATIBLE,
            supportsFileUpload = false,
            capabilities = setOf(AiCapability.TEXT, AiCapability.VISION),
            priority = 3,
            enabled = false,
            isBuiltIn = true,
        ),
        AiProviderConfig(
            id = "nvidia",
            type = "nvidia",
            displayName = "NVIDIA NIM",
            baseUrl = "https://integrate.api.nvidia.com/v1",
            apiKeyAlias = "provider_key_nvidia",
            defaultTextModel = "meta/llama-3.1-70b-instruct",
            defaultMultimodalModel = "meta/llama-3.2-90b-vision-instruct",
            requestFormat = AiRequestFormat.OPENAI_COMPATIBLE,
            supportsFileUpload = false,
            capabilities = setOf(AiCapability.TEXT, AiCapability.VISION),
            priority = 4,
            enabled = false,
            isBuiltIn = true,
        ),
        AiProviderConfig(
            id = "deepseek",
            type = "deepseek",
            displayName = "DeepSeek",
            baseUrl = "https://api.deepseek.com/v1",
            apiKeyAlias = "provider_key_deepseek",
            defaultTextModel = "deepseek-chat",
            defaultMultimodalModel = null,
            requestFormat = AiRequestFormat.OPENAI_COMPATIBLE,
            supportsFileUpload = false,
            capabilities = setOf(AiCapability.TEXT),
            priority = 5,
            enabled = false,
            isBuiltIn = true,
        ),
    )
}
