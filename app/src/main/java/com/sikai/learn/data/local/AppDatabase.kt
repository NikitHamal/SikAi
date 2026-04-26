package com.sikai.learn.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class, SubjectEntity::class, ContentManifestEntity::class, DownloadedFileEntity::class,
        PastPaperEntity::class, QuestionEntity::class, QuizAttemptEntity::class, QuizAnswerEntity::class,
        WeakTopicEntity::class, NoteEntity::class, SavedAiAnswerEntity::class, StudyPlanEntity::class,
        StudyTaskEntity::class, AiProviderConfigEntity::class, ProviderLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun learningDao(): LearningDao
    abstract fun contentDao(): ContentDao
    abstract fun notesDao(): NotesDao
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun aiProviderDao(): AiProviderDao
}
