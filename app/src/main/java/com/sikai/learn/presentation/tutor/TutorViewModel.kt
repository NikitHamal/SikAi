package com.sikai.learn.presentation.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.local.NotesDao
import com.sikai.learn.data.local.SavedAiAnswerEntity
import com.sikai.learn.data.local.UserDao
import com.sikai.learn.domain.ai.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TutorUiState(val loading: Boolean = false, val answer: AiResponse? = null, val error: String? = null, val mode: AiPromptMode = AiPromptMode.SIMPLE)

@HiltViewModel
class TutorViewModel @Inject constructor(
    private val ai: AiRepository,
    private val notesDao: NotesDao,
    private val userDao: UserDao
) : ViewModel() {
    var state = androidx.compose.runtime.mutableStateOf(TutorUiState())
        private set
    val profile = userDao.profile().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private var lastPrompt = ""

    fun setMode(mode: AiPromptMode) { state.value = state.value.copy(mode = mode) }
    fun ask(prompt: String, subject: String) {
        if (prompt.isBlank()) return
        lastPrompt = prompt
        viewModelScope.launch {
            state.value = state.value.copy(loading = true, error = null)
            val profile = userDao.profile().first()
            val result = ai.complete(AiRequest(listOf(AiMessage(AiRole.USER, prompt)), profile?.classLevel ?: 10, subject, state.value.mode))
            state.value = when (result) {
                is AiProviderResult.Success -> state.value.copy(loading = false, answer = result.response)
                is AiProviderResult.Failure -> state.value.copy(loading = false, error = result.message)
            }
        }
    }
    fun saveAnswer() = viewModelScope.launch {
        val answer = state.value.answer ?: return@launch
        val profile = userDao.profile().first()
        notesDao.saveAiAnswer(SavedAiAnswerEntity(UUID.randomUUID().toString(), lastPrompt, answer.text, answer.providerName, profile?.classLevel ?: 10, "General", System.currentTimeMillis()))
    }
}
