package com.sikai.learn.data.repository

import com.sikai.learn.ai.AiOrchestrator
import com.sikai.learn.data.local.QuestionDao
import com.sikai.learn.data.local.QuestionEntity
import com.sikai.learn.data.local.QuizAnswerEntity
import com.sikai.learn.data.local.QuizAttemptDao
import com.sikai.learn.data.local.QuizAttemptEntity
import com.sikai.learn.domain.model.AiMessage
import com.sikai.learn.domain.model.AiMessageRole
import com.sikai.learn.domain.model.AiProviderResult
import com.sikai.learn.domain.model.AiRequest
import com.sikai.learn.domain.model.AiTask
import com.sikai.learn.domain.model.QuizQuestion
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val questions: QuestionDao,
    private val attempts: QuizAttemptDao,
    private val orchestrator: AiOrchestrator,
    private val json: Json,
) {

    suspend fun pickQuestions(classLevel: Int, subject: String?, count: Int = 8): List<QuizQuestion> {
        val seedFromDb = if (subject != null) questions.bySubject(classLevel, subject, count)
        else questions.random(classLevel, count)
        if (seedFromDb.size >= count) {
            return seedFromDb.map { it.toDomain() }
        }

        // Fall back to AI generation when we don't have enough seed MCQs.
        val generated = generateMcqs(classLevel, subject, count - seedFromDb.size)
        if (generated.isNotEmpty()) {
            questions.upsertAll(generated.map { it.toEntity() })
        }
        return (seedFromDb.map { it.toDomain() } + generated).take(count)
    }

    fun recentAttempts(): Flow<List<QuizAttemptEntity>> = attempts.recent()

    suspend fun saveAttempt(
        classLevel: Int,
        subject: String,
        topic: String?,
        all: List<QuizQuestion>,
        selected: Map<String, Int>,
        startedAtMillis: Long,
    ) {
        val finished = System.currentTimeMillis()
        val correctCount = all.count { selected[it.id] == it.correctIndex }
        val attemptId = UUID.randomUUID().toString()
        attempts.upsert(
            QuizAttemptEntity(
                id = attemptId,
                classLevel = classLevel,
                subject = subject,
                topic = topic,
                total = all.size,
                correct = correctCount,
                startedAtMillis = startedAtMillis,
                finishedAtMillis = finished,
            )
        )
        attempts.insertAnswers(
            all.map { q ->
                QuizAnswerEntity(
                    attemptId = attemptId,
                    questionId = q.id,
                    selectedIndex = selected[q.id] ?: -1,
                    correct = selected[q.id] == q.correctIndex,
                )
            }
        )
    }

    private suspend fun generateMcqs(classLevel: Int, subject: String?, count: Int): List<QuizQuestion> {
        if (count <= 0) return emptyList()
        val request = AiRequest(
            task = AiTask.GENERATE_QUIZ,
            messages = listOf(
                AiMessage(
                    role = AiMessageRole.USER,
                    content = "Generate $count MCQs for Class $classLevel${subject?.let { ", $it" } ?: ""}. " +
                        "Return ONLY a fenced ```json block with an array of " +
                        "{\"prompt\":..., \"options\":[\"a\",\"b\",\"c\",\"d\"], \"correctIndex\":0, \"explanation\":\"...\", \"topic\":\"...\"}."
                )
            ),
            classLevel = classLevel,
            subject = subject,
        )
        val outcome = orchestrator.complete(request)
        if (outcome !is AiProviderResult.Success) return emptyList()
        return parseQuestions(outcome.response.text, classLevel, subject ?: "general")
    }

    private fun parseQuestions(text: String, classLevel: Int, subject: String): List<QuizQuestion> = runCatching {
        val raw = extractJsonArray(text) ?: return emptyList()
        val arr = json.parseToJsonElement(raw).jsonArray
        arr.mapNotNull { el ->
            val obj = el as? JsonObject ?: return@mapNotNull null
            val prompt = (obj["prompt"] as? JsonPrimitive)?.contentOrNull ?: return@mapNotNull null
            val opts = (obj["options"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
                ?: return@mapNotNull null
            if (opts.size < 2) return@mapNotNull null
            val correct = (obj["correctIndex"] as? JsonPrimitive)?.intOrNull ?: 0
            val topic = (obj["topic"] as? JsonPrimitive)?.contentOrNull ?: subject
            val explanation = (obj["explanation"] as? JsonPrimitive)?.contentOrNull
            QuizQuestion(
                id = "ai-${UUID.randomUUID()}",
                classLevel = classLevel,
                subject = subject,
                topic = topic,
                prompt = prompt,
                options = opts,
                correctIndex = correct.coerceIn(0, opts.lastIndex),
                explanation = explanation,
            )
        }
    }.getOrDefault(emptyList())

    private fun extractJsonArray(text: String): String? {
        val fenceStart = text.indexOf("```")
        if (fenceStart >= 0) {
            val afterFence = text.indexOf('\n', fenceStart) + 1
            val closing = text.indexOf("```", afterFence)
            if (afterFence > 0 && closing > 0) {
                val body = text.substring(afterFence, closing).trim()
                if (body.startsWith("[")) return body
            }
        }
        val first = text.indexOf('[')
        val last = text.lastIndexOf(']')
        if (first in 0 until last) return text.substring(first, last + 1)
        return null
    }
}

private fun QuestionEntity.toDomain() = QuizQuestion(
    id = id,
    classLevel = classLevel,
    subject = subject,
    topic = topic,
    prompt = prompt,
    options = optionsJson.split("\u0001").filter { it.isNotEmpty() },
    correctIndex = correctIndex,
    explanation = explanation,
)

private fun QuizQuestion.toEntity() = QuestionEntity(
    id = id,
    classLevel = classLevel,
    subject = subject,
    topic = topic,
    prompt = prompt,
    optionsJson = options.joinToString("\u0001"),
    correctIndex = correctIndex,
    explanation = explanation,
    source = "ai",
)
