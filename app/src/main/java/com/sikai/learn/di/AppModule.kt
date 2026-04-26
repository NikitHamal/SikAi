package com.sikai.learn.di

import android.content.Context
import androidx.room.Room
import com.sikai.learn.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "sikai.db")
        .fallbackToDestructiveMigration()
        .build()

    @Provides fun userDao(db: AppDatabase) = db.userDao()
    @Provides fun learningDao(db: AppDatabase) = db.learningDao()
    @Provides fun contentDao(db: AppDatabase) = db.contentDao()
    @Provides fun notesDao(db: AppDatabase) = db.notesDao()
    @Provides fun studyPlanDao(db: AppDatabase) = db.studyPlanDao()
    @Provides fun aiProviderDao(db: AppDatabase) = db.aiProviderDao()

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Provides @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true; prettyPrint = true; encodeDefaults = true }
}
