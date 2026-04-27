package com.sikai.learn.presentation.screens.snap

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.ai.AiOrchestrator
import com.sikai.learn.ai.qwen.QwenModelCatalog
import com.sikai.learn.data.repository.UserProfileRepository
import com.sikai.learn.domain.model.AiCapability
import com.sikai.learn.domain.model.AiAttachment
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

data class SnapState(
    val attachmentUri: String? = null,
    val mimeType: String? = null,
    val fileName: String? = null,
    val sizeBytes: Long = 0,
    val solving: Boolean = false,
    val answerMarkdown: String? = null,
    val providerLabel: String? = null,
    val errorMessage: String? = null,
    val availableModels: List<AiModel> = emptyList(),
    val selectedModel: AiModel? = null,
    val refreshingModels: Boolean = false,
)

@HiltViewModel
class SnapAndSolveViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val orchestrator: AiOrchestrator,
    private val users: UserProfileRepository,
    private val modelCatalog: QwenModelCatalog,
) : ViewModel() {

    private val _state = MutableStateFlow(SnapState())
    val state: StateFlow<SnapState> = _state.asStateFlow()

    init {
        loadModels(forceRefresh = false)
    }

    private fun loadModels(forceRefresh: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(refreshingModels = true) }
            val models = runCatching { modelCatalog.get(forceRefresh) }.getOrDefault(QwenModelCatalog.FALLBACK)
            val visionModels = models.filter { model ->
                model.capabilities.any { it == AiCapability.VISION || it == AiCapability.FILE_UPLOAD || it == AiCapability.PDF }
            }
            _state.update {
                it.copy(
                    availableModels = visionModels,
                    selectedModel = it.selectedModel ?: visionModels.firstOrNull(),
                    refreshingModels = false,
                )
            }
        }
    }

    fun refreshModels() = loadModels(forceRefresh = true)

    fun selectModel(model: AiModel) {
        _state.update { it.copy(selectedModel = model) }
    }

    fun setAttachment(uri: Uri, mimeType: String?) {
        val resolver = context.contentResolver
        val effectiveMime = mimeType ?: resolver.getType(uri) ?: "image/jpeg"
        val (name, size) = resolveDisplayInfo(uri)
        _state.update {
            it.copy(
                attachmentUri = uri.toString(),
                mimeType = effectiveMime,
                fileName = name,
                sizeBytes = size,
                answerMarkdown = null,
                errorMessage = null,
            )
        }
    }

    fun clearAttachment() {
        _state.update { SnapState(availableModels = it.availableModels, selectedModel = it.selectedModel) }
    }

    fun solve(extraPrompt: String?) {
        val s = _state.value
        val uri = s.attachmentUri ?: return
        if (s.solving) return
        viewModelScope.launch {
            _state.update { it.copy(solving = true, errorMessage = null, answerMarkdown = null) }
            val profile = users.current()
            val text = extraPrompt?.takeIf { it.isNotBlank() }
                ?: "Solve this question for me. Show your working clearly."
            val model = s.selectedModel
            val preferredProviderId = if (model?.providerId == "qwen") "qwen" else null
            val request = AiRequest(
                task = AiTask.SOLVE_FILE,
                messages = listOf(
                    AiMessage(
                        role = AiMessageRole.USER,
                        content = text,
                        attachments = listOf(
                            AiAttachment(
                                uri = uri,
                                mimeType = s.mimeType ?: "image/jpeg",
                                sizeBytes = s.sizeBytes,
                                displayName = s.fileName ?: "snap.jpg",
                            )
                        )
                    )
                ),
                classLevel = profile?.classLevel,
                mode = AiMode.StepByStep,
                preferredProviderId = preferredProviderId,
                preferredModelId = model?.id,
            )
            val outcome = orchestrator.complete(request)
            when (outcome) {
                is AiProviderResult.Success -> _state.update {
                    it.copy(
                        solving = false,
                        answerMarkdown = outcome.response.text,
                        providerLabel = outcome.response.providerLabel,
                    )
                }
                is AiProviderResult.Failure -> _state.update {
                    it.copy(
                        solving = false,
                        errorMessage = outcome.message.ifBlank { "Couldn't solve. Try a clearer photo or text input." },
                    )
                }
            }
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