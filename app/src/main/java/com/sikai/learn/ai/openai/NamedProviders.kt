package com.sikai.learn.ai.openai

import android.content.Context
import com.sikai.learn.data.secure.EncryptedKeyStore
import com.sikai.learn.domain.model.AiCapability
import okhttp3.OkHttpClient

/**
 * Concrete OpenAI-compatible providers. Each one is just a thin configuration
 * over [OpenAiCompatibleProvider] — we intentionally avoid hardcoding any
 * single provider inside business logic; everything is dispatched through the
 * [com.sikai.learn.ai.AiProviderRegistry].
 */
object NamedProviders {

    fun deepInfra(client: OkHttpClient, keys: EncryptedKeyStore, ctx: Context): OpenAiCompatibleProvider =
        OpenAiCompatibleProvider(
            id = "deepinfra",
            displayName = "DeepInfra",
            capabilities = setOf(AiCapability.TEXT, AiCapability.STREAMING),
            defaultBaseUrl = "https://api.deepinfra.com/v1/openai",
            client = client,
            keyStore = keys,
            context = ctx,
            authScheme = OpenAiCompatibleProvider.AuthScheme.None,
            extraHeaders = mapOf(
                "Origin" to "https://deepinfra.com",
                "Referer" to "https://deepinfra.com/",
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36",
                "X-Deepinfra-Source" to "web-page",
            ),
        )

    fun openRouter(client: OkHttpClient, keys: EncryptedKeyStore, ctx: Context): OpenAiCompatibleProvider =
        OpenAiCompatibleProvider(
            id = "openrouter",
            displayName = "OpenRouter",
            capabilities = setOf(AiCapability.TEXT, AiCapability.VISION),
            defaultBaseUrl = "https://openrouter.ai/api/v1",
            client = client,
            keyStore = keys,
            context = ctx,
            extraHeaders = mapOf(
                "HTTP-Referer" to "https://sikai.app",
                "X-Title" to "SikAi",
            ),
        )

    fun nvidia(client: OkHttpClient, keys: EncryptedKeyStore, ctx: Context): OpenAiCompatibleProvider =
        OpenAiCompatibleProvider(
            id = "nvidia",
            displayName = "NVIDIA NIM",
            capabilities = setOf(AiCapability.TEXT, AiCapability.VISION),
            defaultBaseUrl = "https://integrate.api.nvidia.com/v1",
            client = client,
            keyStore = keys,
            context = ctx,
        )

    fun deepSeek(client: OkHttpClient, keys: EncryptedKeyStore, ctx: Context): OpenAiCompatibleProvider =
        OpenAiCompatibleProvider(
            id = "deepseek",
            displayName = "DeepSeek",
            capabilities = setOf(AiCapability.TEXT),
            defaultBaseUrl = "https://api.deepseek.com/v1",
            client = client,
            keyStore = keys,
            context = ctx,
        )

    fun openAiCompatible(
        id: String,
        displayName: String,
        client: OkHttpClient,
        keys: EncryptedKeyStore,
        ctx: Context,
    ): OpenAiCompatibleProvider = OpenAiCompatibleProvider(
        id = id,
        displayName = displayName,
        capabilities = setOf(AiCapability.TEXT, AiCapability.VISION),
        defaultBaseUrl = "https://api.openai.com/v1",
        client = client,
        keyStore = keys,
        context = ctx,
    )
}
