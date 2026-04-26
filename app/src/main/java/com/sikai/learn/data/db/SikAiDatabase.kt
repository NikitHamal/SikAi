package com.sikai.learn.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
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
import com.sikai.learn.data.db.entity.AiProviderConfigEntity
import com.sikai.learn.data.db.entity.ContentManifestEntity
import com.sikai.learn.data.db.entity.DownloadedFileEntity
import com.sikai.learn.data.db.entity.NoteEntity
import com.sikai.learn.data.db.entity.PastPaperEntity
import com.sikai.learn.data.db.entity.ProviderLogEntity
import com.sikai.learn.data.db.entity.QuestionEntity
import com.sikai.learn.data.db.entity.QuizAnswerEntity
import com.sikai.learn.data.db.entity.QuizAttemptEntity
import com.sikai.learn.data.db.entity.SavedAiAnswerEntity
import com.sikai.learn.data.db.entity.StudyPlanEntity
import com.sikai.learn.data.db.entity.StudyTaskEntity
import com.sikai.learn.data.db.entity.SubjectEntity
import com.sikai.learn.data.db.entity.UserProfileEntity
import com.sikai.learn.data.db.entity.WeakTopicEntity

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
}
