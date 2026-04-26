package com.sikai.learn.presentation.solve

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.local.NotesDao
import com.sikai.learn.data.local.SavedAiAnswerEntity
import com.sikai.learn.data.local.UserDao
import com.sikai.learn.domain.ai.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SolveUiState(val loading: Boolean = false, val answer: AiResponse? = null, val error: String? = null, val attachmentName: String? = null)

@HiltViewModel
class SolveViewModel @Inject constructor(
    private val ai: AiRepository,
    private val userDao: UserDao,
    private val notesDao: NotesDao
) : ViewModel() {
    var state = androidx.compose.runtime.mutableStateOf(SolveUiState())
        private set
    private var lastAttachment: AiAttachment? = null
    fun setAttachment(attachment: AiAttachment) { lastAttachment = attachment; state.value = SolveUiState(attachmentName = attachment.fileName) }
    fun solve(subject: String, providerId: String? = null) {
        val attachment = lastAttachment ?: return
        viewModelScope.launch {
            val profile = userDao.profile().first()
            state.value = state.value.copy(loading = true, error = null)
            val prompt = "Identify the question in the attached ${attachment.mimeType} file/image and solve it step by step for Class ${profile?.classLevel ?: 10} $subject in Nepal board context. Do not assume missing information."
            val result = ai.complete(AiRequest(listOf(AiMessage(AiRole.USER, prompt, listOf(attachment))), profile?.classLevel ?: 10, subject, AiPromptMode.SOLVE_STEPS, preferredProviderId = providerId))
            state.value = when (result) {
                is AiProviderResult.Success -> state.value.copy(loading = false, answer = result.response)
                is AiProviderResult.Failure -> state.value.copy(loading = false, error = result.message)
            }
        }
    }
    fun saveSolved() = viewModelScope.launch {
        val answer = state.value.answer ?: return@launch
        notesDao.saveAiAnswer(SavedAiAnswerEntity(UUID.randomUUID().toString(), state.value.attachmentName ?: "Solved attachment", answer.text, answer.providerName, 10, "Solve", System.currentTimeMillis()))
    }
}
