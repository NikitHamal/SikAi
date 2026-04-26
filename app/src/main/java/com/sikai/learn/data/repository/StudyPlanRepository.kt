package com.sikai.learn.data.repository

import com.sikai.learn.data.db.dao.StudyPlanDao
import com.sikai.learn.data.db.entity.StudyPlanEntity
import com.sikai.learn.data.db.entity.StudyTaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyPlanRepository @Inject constructor(
    private val dao: StudyPlanDao,
) {
    fun observeActive(): Flow<StudyPlanEntity?> = dao.observeActive()
    fun observeTasks(planId: String): Flow<List<StudyTaskEntity>> = dao.observeTasks(planId)

    suspend fun upsertPlan(plan: StudyPlanEntity) = dao.upsert(plan)

    suspend fun replaceTasks(planId: String, tasks: List<StudyTaskEntity>) {
        dao.insertTasks(tasks)
    }

    suspend fun newPlan(classLevel: Int, examDate: Long?): StudyPlanEntity {
        val plan = StudyPlanEntity(
            id = UUID.randomUUID().toString(),
            title = "My exam plan",
            examDate = examDate,
            createdAt = System.currentTimeMillis(),
            classLevel = classLevel,
            active = true,
        )
        dao.upsert(plan)
        return plan
    }

    suspend fun toggleTask(task: StudyTaskEntity, completed: Boolean) {
        dao.updateTask(
            task.copy(
                completed = completed,
                completedAt = if (completed) System.currentTimeMillis() else null,
            ),
        )
    }
}
