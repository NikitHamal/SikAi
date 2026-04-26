package com.sikai.learn.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.local.NoteEntity
import com.sikai.learn.data.local.NotesDao
import com.sikai.learn.domain.ai.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(private val dao: NotesDao, private val ai: AiRepository) : ViewModel() {
    val notes = dao.notes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val savedAnswers = dao.savedAnswers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    var summary = androidx.compose.runtime.mutableStateOf<String?>(null)
    fun save(title: String, body: String, subject: String) = viewModelScope.launch { if (body.isNotBlank()) dao.upsertNote(NoteEntity(UUID.randomUUID().toString(), title.ifBlank { "Untitled note" }, body, 10, subject, subject, System.currentTimeMillis())) }
    fun summarize(body: String) = viewModelScope.launch {
        val result = ai.complete(AiRequest(listOf(AiMessage(AiRole.USER, body)), 10, "General", AiPromptMode.SUMMARIZE_NOTE))
        summary.value = when (result) { is AiProviderResult.Success -> result.response.text; is AiProviderResult.Failure -> result.message }
    }
}
