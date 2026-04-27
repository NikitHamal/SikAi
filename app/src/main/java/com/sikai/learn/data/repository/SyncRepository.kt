package com.sikai.learn.data.repository

import com.sikai.learn.data.local.QuestionDao
import com.sikai.learn.data.local.QuestionEntity
import com.sikai.learn.data.local.SubjectDao
import com.sikai.learn.data.local.SubjectEntity
import com.sikai.learn.data.remote.BackendApi
import com.sikai.learn.data.remote.RemoteQuestion
import com.sikai.learn.data.remote.RemoteSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val api: BackendApi,
    private val questionDao: QuestionDao,
    private val subjectDao: SubjectDao,
) {

    suspend fun syncSubjects(classLevel: Int? = null): Result<Int> = runCatching {
        val response = if (classLevel != null) api.subjects(classLevel) else api.subjects()
        val entities = response.items.map { it.toEntity() }
        subjectDao.upsertAll(entities)
        entities.size
    }

    suspend fun syncQuestions(classLevel: Int, subject: String? = null): Result<Int> = runCatching {
        val response = api.questions(classLevel, subject, limit = 100, offset = 0)
        val entities = response.items.map { it.toEntity() }
        if (entities.isNotEmpty()) {
            questionDao.upsertAll(entities)
        }
        entities.size
    }
}

private fun RemoteSubject.toEntity() = SubjectEntity(
    id = id,
    displayName = displayName,
    classLevel = classLevel,
)

private fun RemoteQuestion.toEntity() = QuestionEntity(
    id = id,
    classLevel = classLevel,
    subject = subject,
    topic = topic,
    prompt = prompt,
    optionsJson = options.joinToString("\u0001"),
    correctIndex = correctIndex,
    explanation = explanation,
    source = source,
)