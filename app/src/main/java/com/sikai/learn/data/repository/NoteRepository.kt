package com.sikai.learn.data.repository

import com.sikai.learn.data.db.dao.NoteDao
import com.sikai.learn.data.db.dao.SavedAiAnswerDao
import com.sikai.learn.data.db.entity.NoteEntity
import com.sikai.learn.data.db.entity.SavedAiAnswerEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val dao: NoteDao,
    private val savedAiDao: SavedAiAnswerDao,
) {
    fun observeAll(): Flow<List<NoteEntity>> = dao.observeAll()
    fun observeSavedAiAnswers(): Flow<List<SavedAiAnswerEntity>> = savedAiDao.observeAll()

    suspend fun byId(id: String): NoteEntity? = dao.byId(id)

    suspend fun upsert(note: NoteEntity) = dao.upsert(note)

    suspend fun delete(id: String) = dao.delete(id)

    suspend fun newBlank(): NoteEntity {
        val now = System.currentTimeMillis()
        val note = NoteEntity(
            id = UUID.randomUUID().toString(),
            title = "Untitled note",
            body = "",
            subject = null,
            topic = null,
            createdAt = now,
            updatedAt = now,
        )
        dao.upsert(note)
        return note
    }

    suspend fun saveAiAnswer(
        question: String,
        answer: String,
        providerId: String,
        modelId: String,
        mode: String,
        subject: String?,
    ): SavedAiAnswerEntity {
        val entity = SavedAiAnswerEntity(
            id = UUID.randomUUID().toString(),
            question = question,
            answer = answer,
            providerId = providerId,
            modelId = modelId,
            mode = mode,
            subject = subject,
            createdAt = System.currentTimeMillis(),
        )
        savedAiDao.insert(entity)
        return entity
    }
}
