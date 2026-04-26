package com.sikai.learn.ui.screens.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.ai.model.AiCapability
import com.sikai.learn.ai.model.AiMessage
import com.sikai.learn.ai.model.AiProviderResult
import com.sikai.learn.ai.model.AiRequest
import com.sikai.learn.ai.prompt.PromptBuilder
import com.sikai.learn.ai.prompt.TutorMode
import com.sikai.learn.ai.provider.AiOrchestrator
import com.sikai.learn.core.storage.UserPreferences
import com.sikai.learn.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatTurn(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // user | assistant | system | thinking
    val text: String,
    val providerId: String? = null,
    val modelId: String? = null,
    val isError: Boolean = false,
)

data class TutorState(
    val turns: List<ChatTurn> = emptyList(),
    val pending: Boolean = false,
    val mode: TutorMode = TutorMode.Direct,
    val nepaliMode: Boolean = false,
    val classLevel: Int = 10,
    val currentSubject: String? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class TutorViewModel @Inject constructor(
    private val orchestrator: AiOrchestrator,
    private val prompt: PromptBuilder,
    private val prefs: UserPreferences,
    private val notes: NoteRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TutorState())
    val state: StateFlow<TutorState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    classLevel = prefs.classLevel.first(),
                    nepaliMode = prefs.nepaliMode.first(),
                )
            }
        }
    }

    fun setMode(mode: TutorMode) = _state.update { it.copy(mode = mode) }
    fun setSubject(subject: String?) = _state.update { it.copy(currentSubject = subject) }

    fun send(text: String) {
        if (text.isBlank() || _state.value.pending) return
        val s = _state.value
        val userTurn = ChatTurn(role = "user", text = text)
        _state.update { it.copy(turns = it.turns + userTurn, pending = true, errorMessage = null) }

        viewModelScope.launch {
            val systemPrompt = prompt.build(
                userText = "",
                mode = s.mode,
                classLevel = s.classLevel,
                subject = s.currentSubject,
                topic = null,
                nepaliMode = s.nepaliMode,
            )
            // We split: system block carries identity + style; user message carries question.
            val request = AiRequest(
                messages = listOf(AiMessage("user", text)),
                systemPrompt = systemPrompt,
                preferredCapabilities = setOf(AiCapability.TEXT),
                stream = false,
            )
            val preferred = prefs.defaultProviderId.first()
            val result = orchestrator.generate(request, preferredProviderId = preferred)
            when (result) {
                is AiProviderResult.Success -> {
                    val r = result.response
                    _state.update {
                        it.copy(
                            turns = it.turns + ChatTurn(
                                role = "assistant",
                                text = r.text,
                                providerId = r.providerId,
                                modelId = r.modelId,
                            ),
                            pending = false,
                        )
                    }
                }
                is AiProviderResult.Failure -> {
                    _state.update {
                        it.copy(
                            turns = it.turns + ChatTurn(
                                role = "assistant",
                                text = "Could not reach an AI provider. Reason: ${result.reason}. ${result.message}",
                                isError = true,
                            ),
                            pending = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun saveLastAsNote() {
        val last = _state.value.turns.lastOrNull { it.role == "assistant" } ?: return
        val q = _state.value.turns.filter { it.role == "user" }.lastOrNull()?.text ?: ""
        viewModelScope.launch {
            notes.saveAiAnswer(
                question = q,
                answer = last.text,
                providerId = last.providerId ?: "unknown",
                modelId = last.modelId ?: "unknown",
                mode = _state.value.mode.name,
                subject = _state.value.currentSubject,
            )
        }
    }

    fun clear() = _state.update { it.copy(turns = emptyList()) }
}
