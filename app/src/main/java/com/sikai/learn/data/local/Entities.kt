package com.sikai.learn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String = "local",
    val classLevel: Int = 10,
    val language: String = "en",
    val subjectsCsv: String = "Mathematics,Science,English",
    val examDate: String? = null,
    val onboardingComplete: Boolean = false,
    val streak: Int = 0,
    val xp: Int = 0
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val classLevelsCsv: String
)

@Entity(tableName = "content_manifest")
data class ContentManifestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val classLevel: Int,
    val subject: String,
    val year: Int?,
    val fileUrl: String?,
    val fileKey: String?,
    val sizeBytes: Long,
    val checksumSha256: String,
    val version: Int,
    val updatedAt: String,
    val language: String,
    val tagsCsv: String
)

@Entity(tableName = "downloaded_files")
data class DownloadedFileEntity(
    @PrimaryKey val manifestId: String,
    val localPath: String,
    val sizeBytes: Long,
    val checksumSha256: String,
    val downloadedAt: Long
)

@Entity(tableName = "past_papers")
data class PastPaperEntity(
    @PrimaryKey val id: String,
    val manifestId: String,
    val classLevel: Int,
    val subject: String,
    val year: Int,
    val title: String
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val classLevel: Int,
    val subject: String,
    val topic: String,
    val prompt: String,
    val optionsCsv: String,
    val answerIndex: Int,
    val explanation: String
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey val id: String,
    val classLevel: Int,
    val subject: String,
    val topic: String,
    val correct: Int,
    val total: Int,
    val createdAt: Long
)

@Entity(tableName = "quiz_answers", primaryKeys = ["attemptId", "questionId"])
data class QuizAnswerEntity(
    val attemptId: String,
    val questionId: String,
    val selectedIndex: Int,
    val correct: Boolean
)

@Entity(tableName = "weak_topics", primaryKeys = ["classLevel", "subject", "topic"])
data class WeakTopicEntity(
    val classLevel: Int,
    val subject: String,
    val topic: String,
    val misses: Int,
    val updatedAt: Long
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val classLevel: Int,
    val subject: String,
    val tagsCsv: String,
    val updatedAt: Long
)

@Entity(tableName = "saved_ai_answers")
data class SavedAiAnswerEntity(
    @PrimaryKey val id: String,
    val prompt: String,
    val answer: String,
    val providerName: String,
    val classLevel: Int,
    val subject: String,
    val createdAt: Long
)

@Entity(tableName = "study_plans")
data class StudyPlanEntity(
    @PrimaryKey val id: String,
    val classLevel: Int,
    val examDate: String,
    val dailyMinutes: Int,
    val title: String,
    val createdAt: Long
)

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey val id: String,
    val planId: String,
    val title: String,
    val subject: String,
    val dueDate: String,
    val done: Boolean
)

@Entity(tableName = "ai_provider_configs")
data class AiProviderConfigEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val baseUrl: String,
    val apiKeyAlias: String?,
    val textModel: String,
    val multimodalModel: String,
    val capabilitiesCsv: String,
    val priority: Int,
    val enabled: Boolean,
    val requestFormat: String,
    val supportsSearch: Boolean,
    val supportsThinking: Boolean
)

@Entity(tableName = "provider_logs")
data class ProviderLogEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val reason: String,
    val message: String,
    val createdAt: Long
)
