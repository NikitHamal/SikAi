package com.sikai.learn.presentation.screens.papers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.repository.ContentManifestRepository
import com.sikai.learn.data.repository.ManifestRow
import com.sikai.learn.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PastPapersState(
    val classLevel: Int? = null,
    val papers: List<ManifestRow> = emptyList(),
    val refreshing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class PastPapersViewModel @Inject constructor(
    private val users: UserProfileRepository,
    private val manifests: ContentManifestRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PastPapersState())
    val state: StateFlow<PastPapersState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            users.observe().collect { profile ->
                val cls = profile?.classLevel
                _state.update { it.copy(classLevel = cls) }
                if (cls != null) {
                    manifests.observeForClass(cls).collectLatest { rows ->
                        val papers = rows.filter { it.type == "past_paper" }
                        _state.update { it.copy(papers = papers) }
                    }
                }
            }
        }
    }

    fun refresh() {
        val cls = _state.value.classLevel ?: return
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true, errorMessage = null) }
            val r = manifests.refreshForClass(cls)
            _state.update { current ->
                current.copy(
                    refreshing = false,
                    errorMessage = r.exceptionOrNull()?.message,
                )
            }
        }
    }
}
