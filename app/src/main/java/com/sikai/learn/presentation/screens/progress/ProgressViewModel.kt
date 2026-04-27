package com.sikai.learn.presentation.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.local.QuizAttemptDao
import com.sikai.learn.data.local.QuizAttemptEntity
import com.sikai.learn.data.local.WeakTopicDao
import com.sikai.learn.data.local.WeakTopicEntity
import com.sikai.learn.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressState(
    val attempts: List<QuizAttemptEntity> = emptyList(),
    val weakTopics: List<WeakTopicEntity> = emptyList(),
    val classLevel: Int? = null,
    val streakDays: Int = 0,
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val users: UserProfileRepository,
    private val attempts: QuizAttemptDao,
    private val weakTopics: WeakTopicDao,
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressState())
    val state: StateFlow<ProgressState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            users.observe().collect { profile ->
                _state.update { it.copy(classLevel = profile?.classLevel) }
                if (profile?.classLevel != null) {
                    weakTopics.weakest(profile.classLevel).collectLatest { rows ->
                        _state.update { it.copy(weakTopics = rows) }
                    }
                }
            }
        }
        viewModelScope.launch {
            attempts.recent().collectLatest { rows ->
                _state.update { it.copy(attempts = rows) }
            }
        }
    }
}
