package com.sikai.learn.presentation.studyplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.local.StudyPlanDao
import com.sikai.learn.data.local.StudyPlanEntity
import com.sikai.learn.data.local.StudyTaskEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class StudyUiState(val plan: StudyPlanEntity? = null, val tasks: List<StudyTaskEntity> = emptyList())

@HiltViewModel
class StudyPlanViewModel @Inject constructor(private val dao: StudyPlanDao) : ViewModel() {
    val state = combine(dao.activePlan(), dao.tasks()) { plan, tasks -> StudyUiState(plan, tasks) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StudyUiState())
    fun generate(classLevel: Int, examDate: String, minutes: Int) = viewModelScope.launch {
        val planId = UUID.randomUUID().toString()
        dao.upsertPlan(StudyPlanEntity(planId, classLevel, examDate, minutes, "Adaptive board plan", System.currentTimeMillis()))
        val subjects = if (classLevel == 12) listOf("Physics", "Chemistry", "Mathematics", "English") else listOf("Mathematics", "Science", "English", "Social Studies")
        dao.upsertTasks((0 until 14).map { day -> StudyTaskEntity(UUID.randomUUID().toString(), planId, "${subjects[day % subjects.size]} focused revision", subjects[day % subjects.size], LocalDate.now().plusDays(day.toLong()).toString(), false) })
    }
    fun setDone(id: String, done: Boolean) = viewModelScope.launch { dao.setTaskDone(id, done) }
}
