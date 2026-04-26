package com.sikai.learn.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.db.entity.NoteEntity
import com.sikai.learn.data.db.entity.SavedAiAnswerEntity
import com.sikai.learn.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repo: NoteRepository,
) : ViewModel() {

    val notes: Flow<List<NoteEntity>> = repo.observeAll()
    val savedAi: Flow<List<SavedAiAnswerEntity>> = repo.observeSavedAiAnswers()

    fun newNote(onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val note = repo.newBlank()
            onCreated(note.id)
        }
    }

    fun update(note: NoteEntity, title: String, body: String) {
        viewModelScope.launch {
            repo.upsert(
                note.copy(
                    title = title,
                    body = body,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    fun delete(id: String) {
        viewModelScope.launch { repo.delete(id) }
    }
}
