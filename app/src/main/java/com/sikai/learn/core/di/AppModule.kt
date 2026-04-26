package com.sikai.learn.core.di

import android.content.Context
import androidx.room.Room
import com.sikai.learn.core.network.HttpClients
import com.sikai.learn.data.db.SikAiDatabase
import com.sikai.learn.data.db.dao.AiProviderConfigDao
import com.sikai.learn.data.db.dao.ContentManifestDao
import com.sikai.learn.data.db.dao.DownloadedFileDao
import com.sikai.learn.data.db.dao.NoteDao
import com.sikai.learn.data.db.dao.PastPaperDao
import com.sikai.learn.data.db.dao.ProviderLogDao
import com.sikai.learn.data.db.dao.QuestionDao
import com.sikai.learn.data.db.dao.QuizAttemptDao
import com.sikai.learn.data.db.dao.SavedAiAnswerDao
import com.sikai.learn.data.db.dao.StudyPlanDao
import com.sikai.learn.data.db.dao.SubjectDao
import com.sikai.learn.data.db.dao.UserProfileDao
import com.sikai.learn.data.db.dao.WeakTopicDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier annotation class AiHttp
@Qualifier annotation class ContentHttp
@Qualifier annotation class DownloadHttp

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    @Provides @Singleton @AiHttp
    fun provideAiClient(): OkHttpClient = HttpClients.aiClient()

    @Provides @Singleton @ContentHttp
    fun provideContentClient(): OkHttpClient = HttpClients.contentClient()

    @Provides @Singleton @DownloadHttp
    fun provideDownloadClient(): OkHttpClient = HttpClients.downloadClient()

    @Provides @Singleton
    fun provideOkHttpForProviders(@AiHttp client: OkHttpClient): OkHttpClient = client

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): SikAiDatabase =
        Room.databaseBuilder(ctx, SikAiDatabase::class.java, "sikai.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun userProfileDao(db: SikAiDatabase): UserProfileDao = db.userProfileDao()
    @Provides fun subjectDao(db: SikAiDatabase): SubjectDao = db.subjectDao()
    @Provides fun contentManifestDao(db: SikAiDatabase): ContentManifestDao = db.contentManifestDao()
    @Provides fun downloadedFileDao(db: SikAiDatabase): DownloadedFileDao = db.downloadedFileDao()
    @Provides fun pastPaperDao(db: SikAiDatabase): PastPaperDao = db.pastPaperDao()
    @Provides fun questionDao(db: SikAiDatabase): QuestionDao = db.questionDao()
    @Provides fun quizAttemptDao(db: SikAiDatabase): QuizAttemptDao = db.quizAttemptDao()
    @Provides fun weakTopicDao(db: SikAiDatabase): WeakTopicDao = db.weakTopicDao()
    @Provides fun noteDao(db: SikAiDatabase): NoteDao = db.noteDao()
    @Provides fun savedAiAnswerDao(db: SikAiDatabase): SavedAiAnswerDao = db.savedAiAnswerDao()
    @Provides fun studyPlanDao(db: SikAiDatabase): StudyPlanDao = db.studyPlanDao()
    @Provides fun aiProviderConfigDao(db: SikAiDatabase): AiProviderConfigDao = db.aiProviderConfigDao()
    @Provides fun providerLogDao(db: SikAiDatabase): ProviderLogDao = db.providerLogDao()
}
