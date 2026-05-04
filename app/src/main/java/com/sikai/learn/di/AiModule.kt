package com.sikai.learn.di

import android.content.Context
import com.sikai.learn.ai.AiProvider
import com.sikai.learn.ai.openai.NamedProviders
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

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides @Singleton @IntoSet
    fun provideQwenProvider(qwen: QwenProvider): AiProvider = qwen

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