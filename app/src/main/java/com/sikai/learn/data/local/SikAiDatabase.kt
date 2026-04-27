package com.sikai.learn.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        SubjectEntity::class,
        ContentManifestEntity::class,
        DownloadedFileEntity::class,
        PastPaperEntity::class,
        QuestionEntity::class,
        QuizAttemptEntity::class,
        QuizAnswerEntity::class,
        WeakTopicEntity::class,
        NoteEntity::class,
        SavedAiAnswerEntity::class,
        StudyPlanEntity::class,
        StudyTaskEntity::class,
        AiProviderConfigEntity::class,
        ProviderLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class SikAiDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun subjectDao(): SubjectDao
    abstract fun contentManifestDao(): ContentManifestDao
    abstract fun downloadedFileDao(): DownloadedFileDao
    abstract fun pastPaperDao(): PastPaperDao
    abstract fun questionDao(): QuestionDao
    abstract fun quizAttemptDao(): QuizAttemptDao
    abstract fun weakTopicDao(): WeakTopicDao
    abstract fun noteDao(): NoteDao
    abstract fun savedAiAnswerDao(): SavedAiAnswerDao
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun aiProviderConfigDao(): AiProviderConfigDao
    abstract fun providerLogDao(): ProviderLogDao

    companion object {
        const val NAME = "sikai.db"
    }
}
