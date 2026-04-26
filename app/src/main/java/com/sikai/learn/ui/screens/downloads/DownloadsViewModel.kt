package com.sikai.learn.ui.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.db.entity.ContentManifestEntity
import com.sikai.learn.data.db.entity.DownloadedFileEntity
import com.sikai.learn.data.repository.ContentManifestRepository
import com.sikai.learn.data.repository.DownloadProgress
import com.sikai.learn.data.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadsState(
    val refreshing: Boolean = false,
    val message: String? = null,
    val activeDownloads: Map<String, DownloadProgress> = emptyMap(),
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val manifestRepo: ContentManifestRepository,
    private val downloadRepo: DownloadRepository,
) : ViewModel() {

    val manifest: Flow<List<ContentManifestEntity>> = manifestRepo.observeAll()
    val downloads: Flow<List<DownloadedFileEntity>> = downloadRepo.observeAll()

    private val _state = MutableStateFlow(DownloadsState())
    val state: StateFlow<DownloadsState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true, message = null) }
            val result = manifestRepo.refreshFromBackend()
            _state.update {
                it.copy(
                    refreshing = false,
                    message = result.fold(
                        onSuccess = { count -> "Refreshed $count items" },
                        onFailure = { t -> "Refresh failed: ${t.message}" },
                    ),
                )
            }
        }
    }

    fun startDownload(entry: ContentManifestEntity) {
        viewModelScope.launch {
            downloadRepo.download(entry) { progress ->
                _state.update { it.copy(activeDownloads = it.activeDownloads + (entry.id to progress)) }
            }
        }
    }

    fun delete(entry: ContentManifestEntity) {
        viewModelScope.launch { downloadRepo.deleteDownload(entry.id) }
    }
}
