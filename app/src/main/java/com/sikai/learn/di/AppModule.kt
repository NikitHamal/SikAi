package com.sikai.learn.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.sikai.learn.BuildConfig
import com.sikai.learn.data.local.SikAiDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "sikai_settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        prettyPrint = false
    }

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
            // Even in debug we mask sensitive headers — never log API keys.
            redactHeader("Authorization")
            redactHeader("api-key")
            redactHeader("x-api-key")
        }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .callTimeout(180, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit {
        val raw = BuildConfig.BACKEND_BASE_URL
        // Retrofit requires the base URL to end with '/'. Normalise so that
        // mis-configured signing.properties values don't blow up app startup.
        val normalised = if (raw.endsWith("/")) raw else "$raw/"
        return Retrofit.Builder()
            .baseUrl(normalised)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides @Singleton
    fun provideBackendApi(retrofit: Retrofit): com.sikai.learn.data.remote.BackendApi =
        retrofit.create(com.sikai.learn.data.remote.BackendApi::class.java)

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SikAiDatabase =
        Room.databaseBuilder(context, SikAiDatabase::class.java, SikAiDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserProfileDao(db: SikAiDatabase) = db.userProfileDao()
    @Provides fun provideSubjectDao(db: SikAiDatabase) = db.subjectDao()
    @Provides fun provideContentManifestDao(db: SikAiDatabase) = db.contentManifestDao()
    @Provides fun provideDownloadedFileDao(db: SikAiDatabase) = db.downloadedFileDao()
    @Provides fun providePastPaperDao(db: SikAiDatabase) = db.pastPaperDao()
    @Provides fun provideQuestionDao(db: SikAiDatabase) = db.questionDao()
    @Provides fun provideQuizAttemptDao(db: SikAiDatabase) = db.quizAttemptDao()
    @Provides fun provideWeakTopicDao(db: SikAiDatabase) = db.weakTopicDao()
    @Provides fun provideNoteDao(db: SikAiDatabase) = db.noteDao()
    @Provides fun provideSavedAiAnswerDao(db: SikAiDatabase) = db.savedAiAnswerDao()
    @Provides fun provideStudyPlanDao(db: SikAiDatabase) = db.studyPlanDao()
    @Provides fun provideAiProviderConfigDao(db: SikAiDatabase) = db.aiProviderConfigDao()
    @Provides fun provideProviderLogDao(db: SikAiDatabase) = db.providerLogDao()

    @Provides @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.appDataStore

    @Provides @Singleton @Named("backendBaseUrl")
    fun provideBackendBaseUrl(): String = BuildConfig.BACKEND_BASE_URL
}
