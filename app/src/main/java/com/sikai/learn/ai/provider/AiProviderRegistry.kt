package com.sikai.learn.ai.provider

import com.sikai.learn.ai.providers.deepinfra.DeepInfraProvider
import com.sikai.learn.ai.providers.deepseek.DeepSeekProvider
import com.sikai.learn.ai.providers.gemini.GeminiProvider
import com.sikai.learn.ai.providers.nvidia.NvidiaProvider
import com.sikai.learn.ai.providers.openrouter.OpenRouterProvider
import com.sikai.learn.ai.providers.qwen.QwenProvider
import com.sikai.learn.ai.providers.custom.CustomOpenAiCompatibleProvider
import com.sikai.learn.core.storage.SecureKeyStore
import com.sikai.learn.data.repository.ProviderConfigRepository
import com.sikai.learn.ai.model.AiProviderConfig
import com.sikai.learn.ai.model.AiProviderType
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Central registry that materializes [AiProvider] instances from saved configs +
 * built-in defaults. Built-in providers (Qwen, DeepInfra) work without API keys.
 */
@Singleton
class AiProviderRegistry @Inject constructor(
    private val qwenProvider: Provider<QwenProvider>,
    private val deepInfraProvider: Provider<DeepInfraProvider>,
    private val geminiProvider: Provider<GeminiProvider>,
    private val openRouterProvider: Provider<OpenRouterProvider>,
    private val nvidiaProvider: Provider<NvidiaProvider>,
    private val deepSeekProvider: Provider<DeepSeekProvider>,
    private val customProvider: Provider<CustomOpenAiCompatibleProvider>,
    private val configRepo: ProviderConfigRepository,
    private val keyStore: SecureKeyStore,
) {

    fun activeProviders(): List<AiProvider> {
        val configs = configRepo.allActiveConfigsBlocking()
        return configs.mapNotNull { c -> resolve(c) }
    }

    fun getById(id: String): AiProvider? {
        val config = configRepo.findByIdBlocking(id) ?: return null
        return resolve(config)
    }

    private fun resolve(config: AiProviderConfig): AiProvider? {
        val key = if (config.needsApiKey) keyStore.getApiKey(config.id) else null
        return when (config.type) {
            AiProviderType.QWEN -> qwenProvider.get().bind(config, key)
            AiProviderType.DEEPINFRA -> deepInfraProvider.get().bind(config, key)
            AiProviderType.GEMINI -> geminiProvider.get().bind(config, key)
            AiProviderType.OPENROUTER -> openRouterProvider.get().bind(config, key)
            AiProviderType.NVIDIA -> nvidiaProvider.get().bind(config, key)
            AiProviderType.DEEPSEEK -> deepSeekProvider.get().bind(config, key)
            AiProviderType.OPENAI_COMPATIBLE,
            AiProviderType.CUSTOM -> customProvider.get().bind(config, key)
        }
    }
}
