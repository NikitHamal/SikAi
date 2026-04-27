package com.sikai.learn.presentation.screens.aitutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.ai.AiOrchestrator
import com.sikai.learn.ai.qwen.QwenModelCatalog
import com.sikai.learn.data.repository.UserProfileRepository
import com.sikai.learn.domain.model.AiMessage
import com.sikai.learn.domain.model.AiMessageRole
import com.sikai.learn.domain.model.AiMode
import com.sikai.learn.domain.model.AiModel
import com.sikai.learn.domain.model.AiProviderResult
import com.sikai.learn.domain.model.AiRequest
import com.sikai.learn.domain.model.AiTask
import dagger.hilt.android.lifecycle.HiltViewModel
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
)

@HiltViewModel
class AiTutorViewModel @Inject constructor(
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

    fun send(prompt: String) {
        val text = prompt.trim()
        if (text.isEmpty() || _state.value.sending) return

        val userMsg = TutorMessage(id = nextId++, fromUser = true, text = text)
        _state.update { it.copy(messages = it.messages + userMsg, sending = true) }

        viewModelScope.launch {
            val history = _state.value.messages.map {
                AiMessage(
                    role = if (it.fromUser) AiMessageRole.USER else AiMessageRole.ASSISTANT,
                    content = it.text,
                )
            }
            val model = _state.value.selectedModel
            val preferredProviderId = if (model?.providerId == "qwen") "qwen" else null
            val preferredModelId = model?.id
            val request = AiRequest(
                task = AiTask.TEXT_CHAT,
                messages = history,
                classLevel = _state.value.classLevel,
                subject = _state.value.subjectHint,
                mode = _state.value.mode,
                preferredProviderId = preferredProviderId,
                preferredModelId = preferredModelId,
            )
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
}