package com.sikai.learn.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QuizQuestion(
    val id: String,
    val classLevel: Int,
    val subject: String,
    val topic: String,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String? = null,
)

@Serializable
data class QuizAttemptSummary(
    val id: String,
    val subject: String,
    val classLevel: Int,
    val total: Int,
    val correct: Int,
    val takenAtMillis: Long,
)
