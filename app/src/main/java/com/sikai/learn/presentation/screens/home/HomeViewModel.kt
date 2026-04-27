package com.sikai.learn.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.repository.UserProfileRepository
import com.sikai.learn.domain.model.NepalCurriculum
import com.sikai.learn.domain.model.StudentProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val profile: StudentProfile? = null,
    val subjectNames: List<String> = emptyList(),
    val daysToExam: Int? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val users: UserProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            users.observe().collect { profile ->
                val subjectNames = profile?.let {
                    val curriculum = NepalCurriculum.subjectsFor(it.classLevel)
                    it.subjects.mapNotNull { id -> curriculum.firstOrNull { s -> s.id == id }?.displayName }
                } ?: emptyList()
                val days = profile?.examDateMillis?.let { millis ->
                    val now = System.currentTimeMillis()
                    ((millis - now) / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                }
                _state.value = HomeState(profile, subjectNames, days)
            }
        }
    }
}
