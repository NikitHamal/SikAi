package com.sikai.learn.ui.screens.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.core.storage.UserPreferences
import com.sikai.learn.data.db.entity.StudyPlanEntity
import com.sikai.learn.data.db.entity.StudyTaskEntity
import com.sikai.learn.data.repository.StudyPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StudyPlanViewModel @Inject constructor(
    private val repo: StudyPlanRepository,
    private val prefs: UserPreferences,
) : ViewModel() {

    val activePlan: Flow<StudyPlanEntity?> = repo.observeActive()

    val tasks: Flow<List<StudyTaskEntity>> = activePlan.flatMapLatest { plan ->
        if (plan == null) flowOf(emptyList()) else repo.observeTasks(plan.id)
    }

    fun toggle(task: StudyTaskEntity, completed: Boolean) {
        viewModelScope.launch { repo.toggleTask(task, completed) }
    }

    fun generatePlan() {
        viewModelScope.launch {
            val classLevel = prefs.classLevel.first()
            val subjects = prefs.subjects.first().ifEmpty { listOf("Mathematics", "Science", "English") }
            val examDate = prefs.examDate.first()
            val plan = repo.newPlan(classLevel, examDate)
            val tasks = buildSchedule(plan.id, subjects, examDate)
            repo.replaceTasks(plan.id, tasks)
        }
    }

    private fun buildSchedule(planId: String, subjects: List<String>, examDate: Long?): List<StudyTaskEntity> {
        val now = System.currentTimeMillis()
        val end = examDate ?: (now + TimeUnit.DAYS.toMillis(30))
        val days = ((end - now) / TimeUnit.DAYS.toMillis(1)).toInt().coerceIn(7, 90)
        val list = mutableListOf<StudyTaskEntity>()
        for (i in 0 until days) {
            val subject = subjects[i % subjects.size]
            val scheduled = now + TimeUnit.DAYS.toMillis(i.toLong())
            list += StudyTaskEntity(
                id = UUID.randomUUID().toString(),
                planId = planId,
                title = "$subject — Daily focus",
                subject = subject,
                topic = null,
                scheduledAt = scheduled,
                durationMinutes = 45,
            )
        }
        return list
    }
}
