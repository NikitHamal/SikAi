package com.sikai.learn.presentation.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.download.DownloadManager
import com.sikai.learn.data.download.DownloadProgress
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

data class DownloadsState(
    val rows: List<ManifestRow> = emptyList(),
    val progressById: Map<String, DownloadProgress> = emptyMap(),
    val refreshing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val users: UserProfileRepository,
    private val manifests: ContentManifestRepository,
    private val downloads: DownloadManager,
) : ViewModel() {

    private val _state = MutableStateFlow(DownloadsState())
    val state: StateFlow<DownloadsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            users.observe().collect { profile ->
                val flow = profile?.classLevel?.let { manifests.observeForClass(it) }
                    ?: manifests.observeAll()
                flow.collectLatest { rows ->
                    _state.update { it.copy(rows = rows) }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true, errorMessage = null) }
            val r = manifests.refreshAll()
            _state.update { it.copy(refreshing = false, errorMessage = r.exceptionOrNull()?.message) }
        }
    }

    fun startDownload(id: String) {
        downloads.enqueue(id)
        viewModelScope.launch {
            downloads.observe(id).collect { progress ->
                _state.update { it.copy(progressById = it.progressById + (id to progress)) }
            }
        }
    }

    fun cancelDownload(id: String) {
        downloads.cancel(id)
    }
}
