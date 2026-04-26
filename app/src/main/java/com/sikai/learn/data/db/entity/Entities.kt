package com.sikai.learn.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "me",
    val displayName: String = "Student",
    val classLevel: Int = 10,
    val language: String = "en",
    val examDate: Long? = null,
    val streakDays: Int = 0,
    val xp: Int = 0,
    val lastActiveAt: Long = 0,
    val onboarded: Boolean = false,
    val subjectsCsv: String = "",
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val classLevel: Int,
    val symbol: String = "",
    val accentColor: String = "",
    val orderIndex: Int = 0,
)

@Entity(
    tableName = "content_manifest",
    indices = [Index("classLevel"), Index("subject"), Index("type")],
)
data class ContentManifestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String, // textbook, past_paper, mcq_pack, syllabus, notes
    val classLevel: Int,
    val subject: String,
    val year: Int? = null,
    val fileUrl: String,
    val fileKey: String? = null,
    val sizeBytes: Long,
    val checksumSha256: String,
    val version: Int = 1,
    val updatedAt: Long,
    val language: String = "en",
    val tagsCsv: String = "",
    val description: String = "",
)

@Entity(
    tableName = "downloaded_files",
    indices = [Index("manifestId", unique = true)],
)
data class DownloadedFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val manifestId: String,
    val localPath: String,
    val sizeBytes: Long,
    val checksumSha256: String,
    val downloadedAt: Long,
    val verified: Boolean = false,
)

@Entity(tableName = "past_papers", indices = [Index("classLevel"), Index("subject"), Index("year")])
data class PastPaperEntity(
    @PrimaryKey val id: String,
    val title: String,
    val classLevel: Int,
    val subject: String,
    val year: Int,
    val board: String = "NEB",
    val manifestId: String? = null,
    val language: String = "en",
)

@Entity(tableName = "questions", indices = [Index("classLevel"), Index("subject"), Index("topic")])
data class QuestionEntity(
    @PrimaryKey val id: String,
    val classLevel: Int,
    val subject: String,
    val topic: String,
    val prompt: String,
    val optionsJson: String, // JSON array
    val correctIndex: Int,
    val explanation: String,
    val difficulty: Int = 2, // 1..5
    val source: String = "seed", // seed | ai | user
    val createdAt: Long,
)

@Entity(tableName = "quiz_attempts", indices = [Index("subject"), Index("startedAt")])
data class QuizAttemptEntity(
    @PrimaryKey val id: String,
    val subject: String,
    val classLevel: Int,
    val topic: String?,
    val totalQuestions: Int,
    val correctCount: Int,
    val durationMs: Long,
    val startedAt: Long,
    val endedAt: Long,
)

@Entity(tableName = "quiz_answers", indices = [Index("attemptId")])
data class QuizAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attemptId: String,
    val questionId: String,
    val selectedIndex: Int,
    val isCorrect: Boolean,
    val timeMs: Long,
)

@Entity(tableName = "weak_topics", indices = [Index(value = ["subject", "topic"], unique = true)])
data class WeakTopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val topic: String,
    val classLevel: Int,
    val score: Float, // 0..1
    val attempts: Int,
    val updatedAt: Long,
)

@Entity(tableName = "notes", indices = [Index("subject"), Index("createdAt")])
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val subject: String?,
    val topic: String?,
    val tagsCsv: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val sourceType: String = "manual", // manual | ai_answer | summary | flashcard
)

@Entity(tableName = "saved_ai_answers")
data class SavedAiAnswerEntity(
    @PrimaryKey val id: String,
    val question: String,
    val answer: String,
    val providerId: String,
    val modelId: String,
    val mode: String,
    val subject: String?,
    val createdAt: Long,
    val noteId: String? = null,
)

@Entity(tableName = "study_plans")
data class StudyPlanEntity(
    @PrimaryKey val id: String,
    val title: String,
    val examDate: Long?,
    val createdAt: Long,
    val classLevel: Int,
    val active: Boolean = true,
)

@Entity(tableName = "study_tasks", indices = [Index("planId"), Index("scheduledAt")])
data class StudyTaskEntity(
    @PrimaryKey val id: String,
    val planId: String,
    val title: String,
    val subject: String?,
    val topic: String?,
    val scheduledAt: Long,
    val durationMinutes: Int,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val notes: String = "",
)

@Entity(tableName = "ai_provider_configs")
data class AiProviderConfigEntity(
    @PrimaryKey val id: String,
    val type: String,
    val displayName: String,
    val baseUrl: String,
    val requestFormat: String,
    val textModel: String,
    val multimodalModel: String?,
    val supportsFileUpload: Boolean,
    val capabilitiesCsv: String,
    val priority: Int,
    val enabled: Boolean,
    val isBuiltIn: Boolean,
    val needsApiKey: Boolean,
    val notes: String = "",
)

@Entity(tableName = "provider_logs")
data class ProviderLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val providerId: String,
    val outcome: String, // success | failure
    val reason: String? = null,
    val message: String? = null,
    val modelId: String? = null,
    val createdAt: Long,
)
