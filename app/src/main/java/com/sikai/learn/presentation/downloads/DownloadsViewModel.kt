package com.sikai.learn.presentation.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.local.ContentDao
import com.sikai.learn.data.local.DownloadedFileEntity
import com.sikai.learn.data.local.toDomain
import com.sikai.learn.domain.content.ContentManifest
import com.sikai.learn.domain.content.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadsUiState(val items: List<ContentManifest> = emptyList(), val downloads: Map<String, DownloadedFileEntity> = emptyMap(), val busy: String? = null, val message: String? = null)

@HiltViewModel
class DownloadsViewModel @Inject constructor(private val dao: ContentDao, private val repo: ContentRepository) : ViewModel() {
    var busy = androidx.compose.runtime.mutableStateOf<String?>(null)
    var message = androidx.compose.runtime.mutableStateOf<String?>(null)
    val state = combine(dao.manifest(), dao.downloads()) { manifest, downloads -> DownloadsUiState(manifest.map { it.toDomain() }, downloads.associateBy { it.manifestId }, busy.value, message.value) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DownloadsUiState())
    init { refresh() }
    fun refresh() = viewModelScope.launch { repo.refreshManifest().onFailure { message.value = it.message } }
    fun download(item: ContentManifest) = viewModelScope.launch { busy.value = item.id; repo.download(item).onSuccess { message.value = "Downloaded ${item.title}" }.onFailure { message.value = it.message }; busy.value = null }
    fun delete(id: String) = viewModelScope.launch { repo.deleteDownload(id); message.value = "Deleted local file" }
}
