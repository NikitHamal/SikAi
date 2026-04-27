package com.sikai.learn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 0,
    val classLevel: Int,
    val subjectIdsCsv: String,
    val language: String = "en",
    val examDateMillis: Long? = null,
    val studyMinutesPerDay: Int = 60,
    val streakDays: Int = 0,
    val xp: Int = 0,
    val onboardedAtMillis: Long,
)

@Entity(tableName = "subject")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val classLevel: Int,
)

@Entity(tableName = "content_manifest")
data class ContentManifestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val classLevel: Int,
    val subject: String,
    val year: Int? = null,
    val fileUrl: String? = null,
    val sizeBytes: Long,
    val checksumSha256: String? = null,
    val version: Int,
    val updatedAt: Long,
    val language: String,
    val tagsCsv: String,
)

@Entity(tableName = "downloaded_file")
data class DownloadedFileEntity(
    @PrimaryKey val manifestId: String,
    val localPath: String,
    val sizeBytes: Long,
    val checksumSha256: String?,
    val downloadedAtMillis: Long,
)

@Entity(tableName = "past_paper")
data class PastPaperEntity(
    @PrimaryKey val id: String,
    val classLevel: Int,
    val subject: String,
    val year: Int,
    val title: String,
    val manifestId: String?,
)

@Entity(tableName = "question")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val classLevel: Int,
    val subject: String,
    val topic: String,
    val prompt: String,
    val optionsJson: String,
    val correctIndex: Int,
    val explanation: String? = null,
    val source: String = "seed",
)

@Entity(tableName = "quiz_attempt")
data class QuizAttemptEntity(
    @PrimaryKey val id: String,
    val classLevel: Int,
    val subject: String,
    val topic: String?,
    val total: Int,
    val correct: Int,
    val startedAtMillis: Long,
    val finishedAtMillis: Long,
)

@Entity(tableName = "quiz_answer")
data class QuizAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attemptId: String,
    val questionId: String,
    val selectedIndex: Int,
    val correct: Boolean,
)

@Entity(tableName = "weak_topic")
data class WeakTopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classLevel: Int,
    val subject: String,
    val topic: String,
    val strengthScore: Float,
    val updatedAtMillis: Long,
)

@Entity(tableName = "note")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val subject: String?,
    val topic: String?,
    val tagsCsv: String = "",
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

@Entity(tableName = "saved_ai_answer")
data class SavedAiAnswerEntity(
    @PrimaryKey val id: String,
    val prompt: String,
    val markdown: String,
    val providerId: String,
    val providerLabel: String,
    val modelId: String?,
    val subject: String?,
    val savedAtMillis: Long,
)

@Entity(tableName = "study_plan")
data class StudyPlanEntity(
    @PrimaryKey val id: String,
    val classLevel: Int,
    val examDateMillis: Long?,
    val title: String,
    val createdAtMillis: Long,
)

@Entity(tableName = "study_task")
data class StudyTaskEntity(
    @PrimaryKey val id: String,
    val planId: String,
    val dayIndex: Int,
    val subject: String,
    val title: String,
    val description: String?,
    val durationMinutes: Int,
    val isCompleted: Boolean = false,
)

@Entity(tableName = "ai_provider_config")
data class AiProviderConfigEntity(
    @PrimaryKey val id: String,
    val type: String,
    val displayName: String,
    val baseUrl: String? = null,
    val apiKeyAlias: String? = null,
    val defaultTextModel: String? = null,
    val defaultMultimodalModel: String? = null,
    val requestFormat: String,
    val supportsFileUpload: Boolean,
    val capabilitiesCsv: String,
    val priority: Int,
    val enabled: Boolean,
    val isBuiltIn: Boolean,
)

@Entity(tableName = "provider_log")
data class ProviderLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val providerId: String,
    val task: String,
    val success: Boolean,
    val failureReason: String?,
    val message: String?,
    val tookMs: Long,
    val timestampMillis: Long,
)
