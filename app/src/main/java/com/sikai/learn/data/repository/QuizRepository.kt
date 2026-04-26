package com.sikai.learn.data.repository

import com.sikai.learn.data.db.dao.QuestionDao
import com.sikai.learn.data.db.dao.QuizAttemptDao
import com.sikai.learn.data.db.dao.WeakTopicDao
import com.sikai.learn.data.db.entity.QuestionEntity
import com.sikai.learn.data.db.entity.QuizAnswerEntity
import com.sikai.learn.data.db.entity.QuizAttemptEntity
import com.sikai.learn.data.db.entity.WeakTopicEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

@Singleton
class QuizRepository @Inject constructor(
    private val questionDao: QuestionDao,
    private val attemptDao: QuizAttemptDao,
    private val weakDao: WeakTopicDao,
    private val json: Json,
) {
    fun observeRecentAttempts(limit: Int = 20): Flow<List<QuizAttemptEntity>> = attemptDao.recent(limit)
    fun observeWeakTopics(limit: Int = 10): Flow<List<WeakTopicEntity>> = weakDao.observeWeakest(limit)

    suspend fun startQuiz(classLevel: Int, subject: String, count: Int = 5): List<QuestionEntity> =
        questionDao.random(classLevel, subject, count)

    suspend fun finishQuiz(
        classLevel: Int,
        subject: String,
        topic: String?,
        questions: List<QuestionEntity>,
        answers: List<Int>,
        startedAt: Long,
    ): QuizAttemptEntity {
        val endedAt = System.currentTimeMillis()
        val correct = questions.zip(answers).count { (q, a) -> q.correctIndex == a }
        val attemptId = UUID.randomUUID().toString()
        val attempt = QuizAttemptEntity(
            id = attemptId,
            subject = subject,
            classLevel = classLevel,
            topic = topic,
            totalQuestions = questions.size,
            correctCount = correct,
            durationMs = endedAt - startedAt,
            startedAt = startedAt,
            endedAt = endedAt,
        )
        attemptDao.insert(attempt)
        attemptDao.insertAnswers(
            questions.zip(answers).map { (q, a) ->
                QuizAnswerEntity(
                    attemptId = attemptId,
                    questionId = q.id,
                    selectedIndex = a,
                    isCorrect = q.correctIndex == a,
                    timeMs = 0L,
                )
            },
        )
        // Update weak topics: track per question topic
        questions.zip(answers).groupBy { (q, _) -> q.topic }.forEach { (t, group) ->
            val ratio = group.count { (q, a) -> q.correctIndex == a }.toFloat() / group.size
            weakDao.upsert(
                WeakTopicEntity(
                    subject = subject,
                    topic = t,
                    classLevel = classLevel,
                    score = ratio,
                    attempts = group.size,
                    updatedAt = endedAt,
                ),
            )
        }
        return attempt
    }

    fun parseOptions(question: QuestionEntity): List<String> = runCatching {
        json.parseToJsonElement(question.optionsJson).jsonArray.map { it.jsonPrimitive.content }
    }.getOrDefault(emptyList())
}
