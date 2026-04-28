package com.sikai.learn.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ContentType {
    @SerialName("textbook") TEXTBOOK,
    @SerialName("past_paper") PAST_PAPER,
    @SerialName("mcq_pack") MCQ_PACK,
    @SerialName("syllabus") SYLLABUS,
    @SerialName("notes") NOTES,
    @SerialName("model_question") MODEL_QUESTION,
    @SerialName("solution") SOLUTION,
}

@Serializable
data class ContentManifestEntry(
    val id: String,
    val title: String,
    val type: ContentType,
    val classLevel: Int,
    val subject: String,
    val year: Int? = null,
    val fileUrl: String? = null,
    val sizeBytes: Long = 0,
    val checksumSha256: String? = null,
    val version: Int = 1,
    val updatedAt: Long = 0,
    val language: String = "en",
    val tags: List<String> = emptyList(),
)

@Serializable
data class ContentManifest(
    val items: List<ContentManifestEntry>,
    val generatedAt: Long = 0,
)

@Serializable
data class StudentProfile(
    val classLevel: Int,
    val subjects: List<String>,
    val language: String = "en",
    val examDateMillis: Long? = null,
    val studyMinutesPerDay: Int = 60,
)

@Serializable
data class Subject(val id: String, val displayName: String, val classLevels: List<Int>)
