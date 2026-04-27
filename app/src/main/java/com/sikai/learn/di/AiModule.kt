package com.sikai.learn.di

import android.content.Context
import com.sikai.learn.ai.AiProvider
import com.sikai.learn.ai.gemini.GeminiProvider
import com.sikai.learn.ai.openai.NamedProviders
import com.sikai.learn.ai.openai.OpenAiCompatibleProvider
import com.sikai.learn.ai.qwen.QwenProvider
import com.sikai.learn.data.secure.EncryptedKeyStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

/**
 * Wires every concrete [AiProvider] into the multi-bound `Set<AiProvider>` that
 * [com.sikai.learn.ai.AiProviderRegistry] consumes. Adding a new provider means
 * adding one more `@Provides @IntoSet` here — no changes to the orchestrator.
 *
 * Auth-bearing providers (Gemini, OpenRouter, NVIDIA, DeepSeek, custom) are
 * still bound at startup but stay dormant until the user pastes an API key in
 * Settings; the runtime check lives in each provider's `complete()` body.
 */
@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides @Singleton @IntoSet
    fun provideQwenProvider(qwen: QwenProvider): AiProvider = qwen

    @Provides @Singleton @IntoSet
    fun provideGeminiProvider(
        client: OkHttpClient,
        keyStore: EncryptedKeyStore,
        @ApplicationContext context: Context,
    ): AiProvider = GeminiProvider(client, keyStore, context)

    @Provides @Singleton @IntoSet
    fun provideDeepInfraProvider(
        client: OkHttpClient,
        keyStore: EncryptedKeyStore,
        @ApplicationContext context: Context,
    ): AiProvider = NamedProviders.deepInfra(client, keyStore, context)

    @Provides @Singleton @IntoSet
    fun provideOpenRouterProvider(
        client: OkHttpClient,
        keyStore: EncryptedKeyStore,
        @ApplicationContext context: Context,
    ): AiProvider = NamedProviders.openRouter(client, keyStore, context)

    @Provides @Singleton @IntoSet
    fun provideNvidiaProvider(
        client: OkHttpClient,
        keyStore: EncryptedKeyStore,
        @ApplicationContext context: Context,
    ): AiProvider = NamedProviders.nvidia(client, keyStore, context)

    @Provides @Singleton @IntoSet
    fun provideDeepSeekProvider(
        client: OkHttpClient,
        keyStore: EncryptedKeyStore,
        @ApplicationContext context: Context,
    ): AiProvider = NamedProviders.deepSeek(client, keyStore, context)

    /**
     * The custom OpenAI-compatible runner that handles any user-defined
     * provider whose [com.sikai.learn.domain.model.AiProviderConfig.type]
     * starts with "custom:" or equals "openai_compatible". Marked @Named so
     * the registry can fish it out specifically.
     */
    @Provides @Singleton @Named("custom_openai_compatible")
    fun provideCustomOpenAiCompatible(
        client: OkHttpClient,
        keyStore: EncryptedKeyStore,
        @ApplicationContext context: Context,
    ): AiProvider = NamedProviders.openAiCompatible(
        id = "openai_compatible",
        displayName = "Custom OpenAI-compatible",
        client = client,
        keys = keyStore,
        ctx = context,
    )
}
