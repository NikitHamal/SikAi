package com.sikai.learn.data.repository

import com.sikai.learn.data.db.dao.PastPaperDao
import com.sikai.learn.data.db.entity.PastPaperEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PastPaperRepository @Inject constructor(
    private val dao: PastPaperDao,
) {
    fun observeForClass(classLevel: Int): Flow<List<PastPaperEntity>> = dao.observeForClass(classLevel)
    fun observeAll(): Flow<List<PastPaperEntity>> = dao.observeAll()
}
