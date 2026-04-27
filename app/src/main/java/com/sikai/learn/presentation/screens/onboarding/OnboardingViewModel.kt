package com.sikai.learn.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.repository.UserProfileRepository
import com.sikai.learn.domain.model.NepalCurriculum
import com.sikai.learn.domain.model.StudentProfile
import com.sikai.learn.domain.model.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val step: Int = 0,
    val classLevel: Int? = null,
    val selectedSubjectIds: Set<String> = emptySet(),
    val studyMinutes: Int = 60,
    val examDateMillis: Long? = null,
    val saving: Boolean = false,
    val finished: Boolean = false,
) {
    val availableSubjects: List<Subject>
        get() = classLevel?.let { NepalCurriculum.subjectsFor(it) } ?: emptyList()

    val canAdvance: Boolean
        get() = when (step) {
            0 -> classLevel != null
            1 -> selectedSubjectIds.isNotEmpty()
            2 -> studyMinutes in 10..240
            else -> true
        }
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val users: UserProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun selectClass(classLevel: Int) {
        _state.update { it.copy(classLevel = classLevel, selectedSubjectIds = emptySet()) }
    }

    fun toggleSubject(id: String) {
        _state.update { current ->
            val updated = if (id in current.selectedSubjectIds) current.selectedSubjectIds - id
            else current.selectedSubjectIds + id
            current.copy(selectedSubjectIds = updated)
        }
    }

    fun setStudyMinutes(minutes: Int) {
        _state.update { it.copy(studyMinutes = minutes.coerceIn(10, 240)) }
    }

    fun setExamDate(millis: Long?) {
        _state.update { it.copy(examDateMillis = millis) }
    }

    fun next() {
        _state.update { it.copy(step = (it.step + 1).coerceAtMost(2)) }
    }

    fun previous() {
        _state.update { it.copy(step = (it.step - 1).coerceAtLeast(0)) }
    }

    fun finish() {
        val s = _state.value
        val classLevel = s.classLevel ?: return
        if (s.selectedSubjectIds.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            users.completeOnboarding(
                StudentProfile(
                    classLevel = classLevel,
                    subjects = s.selectedSubjectIds.toList(),
                    studyMinutesPerDay = s.studyMinutes,
                    examDateMillis = s.examDateMillis,
                )
            )
            _state.update { it.copy(saving = false, finished = true) }
        }
    }
}
