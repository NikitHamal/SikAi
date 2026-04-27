package com.sikai.learn.presentation.screens.aitutor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.ai.AiOrchestrator
import com.sikai.learn.ai.qwen.QwenModelCatalog
import com.sikai.learn.data.repository.UserProfileRepository
import com.sikai.learn.domain.model.AiAttachment
import com.sikai.learn.domain.model.AiCapability
import com.sikai.learn.domain.model.AiMessage
import com.sikai.learn.domain.model.AiMessageRole
import com.sikai.learn.domain.model.AiMode
import com.sikai.learn.domain.model.AiModel
import com.sikai.learn.domain.model.AiProviderResult
import com.sikai.learn.domain.model.AiRequest
import com.sikai.learn.domain.model.AiTask
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TutorMessage(
    val id: Long,
    val fromUser: Boolean,
    val text: String,
    val providerLabel: String? = null,
    val modelId: String? = null,
    val isError: Boolean = false,
)

data class TutorState(
    val messages: List<TutorMessage> = emptyList(),
    val sending: Boolean = false,
    val mode: AiMode = AiMode.SimpleExplanation,
    val classLevel: Int? = null,
    val subjectHint: String? = null,
    val availableModels: List<AiModel> = emptyList(),
    val selectedModel: AiModel? = null,
    val refreshingModels: Boolean = false,
    val attachmentUri: String? = null,
    val attachmentMimeType: String? = null,
    val attachmentFileName: String? = null,
    val attachmentSizeBytes: Long = 0,
)

@HiltViewModel
class AiTutorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val orchestrator: AiOrchestrator,
    private val users: UserProfileRepository,
    private val modelCatalog: QwenModelCatalog,
) : ViewModel() {

    private val _state = MutableStateFlow(TutorState())
    val state: StateFlow<TutorState> = _state.asStateFlow()
    private var nextId = 1L

    init {
        viewModelScope.launch {
            users.observe().collect { profile ->
                _state.update { it.copy(classLevel = profile?.classLevel) }
            }
        }
        loadModels(forceRefresh = false)
    }

    private fun loadModels(forceRefresh: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(refreshingModels = true) }
            val models = runCatching { modelCatalog.get(forceRefresh) }.getOrDefault(QwenModelCatalog.FALLBACK)
            _state.update {
                it.copy(
                    availableModels = models,
                    selectedModel = it.selectedModel ?: models.firstOrNull(),
                    refreshingModels = false,
                )
            }
        }
    }

    fun refreshModels() {
        loadModels(forceRefresh = true)
    }

    fun selectModel(model: AiModel) {
        _state.update { it.copy(selectedModel = model) }
    }

    fun setMode(mode: AiMode) {
        _state.update { it.copy(mode = mode) }
    }

    fun setAttachment(uri: Uri, mimeType: String?) {
        val resolver = context.contentResolver
        val effectiveMime = mimeType ?: resolver.getType(uri) ?: "image/jpeg"
        val (name, size) = resolveDisplayInfo(uri)
        _state.update {
            it.copy(
                attachmentUri = uri.toString(),
                attachmentMimeType = effectiveMime,
                attachmentFileName = name,
                attachmentSizeBytes = size,
            )
        }
    }

    fun clearAttachment() {
        _state.update { it.copy(attachmentUri = null, attachmentMimeType = null, attachmentFileName = null, attachmentSizeBytes = 0) }
    }

    fun send(prompt: String) {
        val text = prompt.trim()
        if (text.isEmpty() && _state.value.attachmentUri == null) return
        if (_state.value.sending) return

        val userMsg = TutorMessage(id = nextId++, fromUser = true, text = text.ifBlank { "(image/file attachment)" })
        _state.update { it.copy(messages = it.messages + userMsg, sending = true) }

        viewModelScope.launch {
            val s = _state.value
            val attachments = s.attachmentUri?.let { uri ->
                listOf(
                    AiAttachment(
                        uri = uri,
                        mimeType = s.attachmentMimeType ?: "image/jpeg",
                        sizeBytes = s.attachmentSizeBytes,
                        displayName = s.attachmentFileName ?: "attachment",
                    )
                )
            } ?: emptyList()

            val hasAttachment = attachments.isNotEmpty()
            val history = s.messages.map {
                AiMessage(
                    role = if (it.fromUser) AiMessageRole.USER else AiMessageRole.ASSISTANT,
                    content = it.text,
                )
            }
            if (hasAttachment && history.lastOrNull()?.role == AiMessageRole.USER) {
                history.last().attachments + attachments
            }
            val messages = if (hasAttachment) {
                history.toMutableList().apply {
                    val lastIdx = indexOfLast { it.role == AiMessageRole.USER }
                    if (lastIdx >= 0) {
                        this[lastIdx] = this[lastIdx].copy(attachments = this[lastIdx].attachments + attachments)
                    } else {
                        add(AiMessage(role = AiMessageRole.USER, content = text.ifBlank { "Describe this" }, attachments = attachments))
                    }
                }
            } else {
                history
            }

            val model = s.selectedModel
            val preferredProviderId = if (model?.providerId == "qwen") "qwen" else null
            val preferredModelId = model?.id
            val task = if (hasAttachment) AiTask.SOLVE_FILE else AiTask.TEXT_CHAT
            val request = AiRequest(
                task = task,
                messages = messages,
                classLevel = s.classLevel,
                subject = s.subjectHint,
                mode = s.mode,
                preferredProviderId = preferredProviderId,
                preferredModelId = preferredModelId,
            )
            _state.update { it.copy(attachmentUri = null, attachmentMimeType = null, attachmentFileName = null, attachmentSizeBytes = 0) }
            val outcome = orchestrator.complete(request)
            val reply = when (outcome) {
                is AiProviderResult.Success -> TutorMessage(
                    id = nextId++,
                    fromUser = false,
                    text = outcome.response.text,
                    providerLabel = outcome.response.providerLabel,
                    modelId = outcome.response.modelId,
                )
                is AiProviderResult.Failure -> TutorMessage(
                    id = nextId++,
                    fromUser = false,
                    text = outcome.message.ifBlank { "All providers failed. Check your network or try again." },
                    providerLabel = outcome.providerId.takeIf { it.isNotBlank() },
                    isError = true,
                )
            }
            _state.update { it.copy(messages = it.messages + reply, sending = false) }
        }
    }

    private fun resolveDisplayInfo(uri: Uri): Pair<String?, Long> = runCatching {
        var name: String? = null
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val nameIdx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIdx = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (nameIdx >= 0) name = c.getString(nameIdx)
                if (sizeIdx >= 0) size = c.getLong(sizeIdx)
            }
        }
        name to size
    }.getOrElse { null to 0L }
}