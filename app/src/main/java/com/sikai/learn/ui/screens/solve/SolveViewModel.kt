package com.sikai.learn.ui.screens.solve

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.ai.model.AiAttachment
import com.sikai.learn.ai.model.AiCapability
import com.sikai.learn.ai.model.AiMessage
import com.sikai.learn.ai.model.AiProviderResult
import com.sikai.learn.ai.model.AiRequest
import com.sikai.learn.ai.prompt.PromptBuilder
import com.sikai.learn.ai.prompt.TutorMode
import com.sikai.learn.ai.provider.AiOrchestrator
import com.sikai.learn.core.storage.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class SolveState(
    val capturedImagePath: String? = null,
    val pending: Boolean = false,
    val answer: String? = null,
    val errorMessage: String? = null,
    val classLevel: Int = 10,
)

@HiltViewModel
class SolveViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val orchestrator: AiOrchestrator,
    private val prompt: PromptBuilder,
    private val prefs: UserPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(SolveState())
    val state: StateFlow<SolveState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(classLevel = prefs.classLevel.first()) }
        }
    }

    fun setImagePath(path: String?) {
        _state.update { it.copy(capturedImagePath = path, answer = null, errorMessage = null) }
    }

    fun reset() = _state.update { SolveState(classLevel = it.classLevel) }

    fun solve() {
        val imagePath = _state.value.capturedImagePath ?: return
        if (_state.value.pending) return
        _state.update { it.copy(pending = true, errorMessage = null, answer = null) }

        viewModelScope.launch {
            try {
                val attachment = withContext(Dispatchers.IO) {
                    val file = File(imagePath)
                    val bytes = file.readBytes()
                    AiAttachment(
                        uri = Uri.fromFile(file).toString(),
                        mimeType = "image/jpeg",
                        name = file.name,
                        sizeBytes = file.length(),
                        isImage = true,
                        isPdf = false,
                        base64Data = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP),
                    )
                }
                val systemPrompt = prompt.build(
                    userText = "",
                    mode = TutorMode.SolveStepByStep,
                    classLevel = _state.value.classLevel,
                    nepaliMode = prefs.nepaliMode.first(),
                )
                val request = AiRequest(
                    messages = listOf(
                        AiMessage(
                            role = "user",
                            content = "Read this question carefully and solve it. Show every step. End with the final answer in a box.",
                            attachments = listOf(attachment),
                        ),
                    ),
                    systemPrompt = systemPrompt,
                    preferredCapabilities = setOf(AiCapability.VISION),
                )
                val preferred = prefs.defaultMultimodalProviderId.first()
                val result = orchestrator.generate(request, preferred)
                when (result) {
                    is AiProviderResult.Success -> _state.update { it.copy(pending = false, answer = result.response.text) }
                    is AiProviderResult.Failure -> _state.update {
                        it.copy(
                            pending = false,
                            errorMessage = "Could not solve: ${result.reason}. ${result.message}",
                        )
                    }
                }
            } catch (t: Throwable) {
                _state.update { it.copy(pending = false, errorMessage = t.message ?: "Unknown error") }
            }
        }
    }
}
