package com.sikai.learn.presentation.screens.studyplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.local.StudyPlanDao
import com.sikai.learn.data.local.StudyPlanEntity
import com.sikai.learn.data.local.StudyTaskEntity
import com.sikai.learn.data.repository.UserProfileRepository
import com.sikai.learn.domain.model.NepalCurriculum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class StudyPlanState(
    val plan: StudyPlanEntity? = null,
    val tasks: List<StudyTaskEntity> = emptyList(),
    val classLevel: Int? = null,
    val subjectIds: List<String> = emptyList(),
    val examDateMillis: Long? = null,
    val studyMinutesPerDay: Int = 60,
)

@HiltViewModel
class StudyPlanViewModel @Inject constructor(
    private val users: UserProfileRepository,
    private val plans: StudyPlanDao,
) : ViewModel() {

    private val _state = MutableStateFlow(StudyPlanState())
    val state: StateFlow<StudyPlanState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            users.observe().collect { profile ->
                _state.update {
                    it.copy(
                        classLevel = profile?.classLevel,
                        subjectIds = profile?.subjects.orEmpty(),
                        examDateMillis = profile?.examDateMillis,
                        studyMinutesPerDay = profile?.studyMinutesPerDay ?: 60,
                    )
                }
            }
        }
        viewModelScope.launch {
            plans.observeCurrent().collectLatest { plan ->
                _state.update { it.copy(plan = plan) }
                if (plan != null) {
                    plans.tasks(plan.id).collect { tasks ->
                        _state.update { it.copy(tasks = tasks) }
                    }
                }
            }
        }
    }

    fun generatePlan() {
        val s = _state.value
        val cls = s.classLevel ?: return
        if (s.subjectIds.isEmpty()) return
        viewModelScope.launch {
            val planId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val totalDays = if (s.examDateMillis != null) {
                val diff = ((s.examDateMillis - now) / (1000L * 60 * 60 * 24)).toInt()
                diff.coerceIn(7, 90)
            } else 14
            plans.upsert(
                StudyPlanEntity(
                    id = planId,
                    classLevel = cls,
                    examDateMillis = s.examDateMillis,
                    title = "Plan for Class $cls",
                    createdAtMillis = now,
                )
            )
            val curriculumNames = NepalCurriculum.subjectsFor(cls).filter { it.id in s.subjectIds }
            val tasks = (0 until totalDays).map { day ->
                val subject = curriculumNames[day % curriculumNames.size]
                StudyTaskEntity(
                    id = "$planId-$day",
                    planId = planId,
                    dayIndex = day,
                    subject = subject.displayName,
                    title = "${subject.displayName} · core concept review",
                    description = "Spend ${s.studyMinutesPerDay} minutes on the next chapter and a 5-question quick check.",
                    durationMinutes = s.studyMinutesPerDay,
                )
            }
            plans.upsertTasks(tasks)
        }
    }

    fun toggleTaskComplete(task: StudyTaskEntity) {
        viewModelScope.launch {
            plans.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }
}
