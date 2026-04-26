package com.sikai.learn.di

import com.sikai.learn.data.repository.AiRepositoryImpl
import com.sikai.learn.data.repository.ContentRepositoryImpl
import com.sikai.learn.domain.ai.AiRepository
import com.sikai.learn.domain.content.ContentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAiRepository(impl: AiRepositoryImpl): AiRepository
    @Binds @Singleton abstract fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository
}
