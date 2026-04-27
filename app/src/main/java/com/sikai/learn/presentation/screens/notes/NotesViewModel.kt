package com.sikai.learn.presentation.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.local.NoteDao
import com.sikai.learn.data.local.NoteEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class NotesState(
    val notes: List<NoteEntity> = emptyList(),
    val editingId: String? = null,
    val draftTitle: String = "",
    val draftBody: String = "",
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notes: NoteDao,
) : ViewModel() {

    private val _state = MutableStateFlow(NotesState())
    val state: StateFlow<NotesState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            notes.observeAll().collect { list ->
                _state.update { it.copy(notes = list) }
            }
        }
    }

    fun startNew() {
        _state.update { it.copy(editingId = "new", draftTitle = "", draftBody = "") }
    }

    fun edit(id: String) {
        viewModelScope.launch {
            val note = notes.byId(id) ?: return@launch
            _state.update { it.copy(editingId = id, draftTitle = note.title, draftBody = note.body) }
        }
    }

    fun setTitle(value: String) { _state.update { it.copy(draftTitle = value) } }
    fun setBody(value: String) { _state.update { it.copy(draftBody = value) } }

    fun save() {
        val s = _state.value
        if (s.draftTitle.isBlank() && s.draftBody.isBlank()) {
            _state.update { it.copy(editingId = null) }
            return
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val id = if (s.editingId == "new" || s.editingId == null) UUID.randomUUID().toString()
            else s.editingId
            val existing = notes.byId(id)
            notes.upsert(
                NoteEntity(
                    id = id,
                    title = s.draftTitle.ifBlank { "Untitled" },
                    body = s.draftBody,
                    subject = existing?.subject,
                    topic = existing?.topic,
                    tagsCsv = existing?.tagsCsv ?: "",
                    createdAtMillis = existing?.createdAtMillis ?: now,
                    updatedAtMillis = now,
                )
            )
            _state.update { it.copy(editingId = null, draftTitle = "", draftBody = "") }
        }
    }

    fun cancel() {
        _state.update { it.copy(editingId = null, draftTitle = "", draftBody = "") }
    }

    fun delete(id: String) {
        viewModelScope.launch { notes.delete(id) }
    }
}
