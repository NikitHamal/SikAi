package com.sikai.learn.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.db.dao.SubjectDao
import com.sikai.learn.data.db.entity.SubjectEntity
import com.sikai.learn.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val step: Int = 0,
    val classLevel: Int = 10,
    val language: String = "en",
    val subjects: Set<String> = emptySet(),
    val examDate: Long? = null,
    val availableSubjects: List<SubjectEntity> = emptyList(),
    val saving: Boolean = false,
    val finished: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepo: UserProfileRepository,
    private val subjectDao: SubjectDao,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init { reloadSubjects(_state.value.classLevel) }

    fun setClass(level: Int) {
        _state.update { it.copy(classLevel = level, subjects = emptySet()) }
        reloadSubjects(level)
    }

    fun setLanguage(lang: String) = _state.update { it.copy(language = lang) }

    fun toggleSubject(name: String) = _state.update {
        val newSet = if (it.subjects.contains(name)) it.subjects - name else it.subjects + name
        it.copy(subjects = newSet)
    }

    fun setExamDate(date: Long?) = _state.update { it.copy(examDate = date) }

    fun nextStep() = _state.update { it.copy(step = (it.step + 1).coerceAtMost(4)) }
    fun prevStep() = _state.update { it.copy(step = (it.step - 1).coerceAtLeast(0)) }

    fun finish() {
        val s = _state.value
        if (s.saving) return
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            userRepo.completeOnboarding(
                classLevel = s.classLevel,
                language = s.language,
                subjects = s.subjects.toList(),
                examDate = s.examDate,
            )
            _state.update { it.copy(saving = false, finished = true) }
        }
    }

    private fun reloadSubjects(level: Int) {
        viewModelScope.launch {
            val all = subjectDao.all().filter { it.classLevel == level }
            _state.update { it.copy(availableSubjects = all) }
        }
    }
}
