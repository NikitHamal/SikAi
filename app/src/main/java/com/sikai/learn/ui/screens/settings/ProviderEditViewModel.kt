package com.sikai.learn.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.ai.model.AiCapability
import com.sikai.learn.ai.model.AiProviderConfig
import com.sikai.learn.ai.model.AiProviderType
import com.sikai.learn.ai.model.AiRequestFormat
import com.sikai.learn.core.storage.SecureKeyStore
import com.sikai.learn.data.repository.ProviderConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ProviderEditState(
    val loading: Boolean = true,
    val isNew: Boolean = false,
    val displayName: String = "",
    val baseUrl: String = "",
    val textModel: String = "",
    val multimodalModel: String = "",
    val needsApiKey: Boolean = true,
    val supportsFileUpload: Boolean = false,
    val enabled: Boolean = true,
    val isBuiltIn: Boolean = false,
    val capabilities: Set<AiCapability> = setOf(AiCapability.TEXT),
    val type: AiProviderType = AiProviderType.OPENAI_COMPATIBLE,
    val requestFormat: AiRequestFormat = AiRequestFormat.OPENAI_COMPATIBLE,
    val apiKeyInput: String = "",
    val apiKeyMasked: String = "",
    val priority: Int = 100,
    val notes: String = "",
    val originalId: String? = null,
)

@HiltViewModel
class ProviderEditViewModel @Inject constructor(
    private val repo: ProviderConfigRepository,
    private val keyStore: SecureKeyStore,
) : ViewModel() {

    private val _state = MutableStateFlow(ProviderEditState())
    val state: StateFlow<ProviderEditState> = _state.asStateFlow()

    fun load(id: String?) {
        viewModelScope.launch {
            if (id == null) {
                _state.update {
                    ProviderEditState(loading = false, isNew = true)
                }
            } else {
                val cfg = repo.findById(id)
                if (cfg != null) {
                    _state.update {
                        ProviderEditState(
                            loading = false,
                            isNew = false,
                            originalId = cfg.id,
                            displayName = cfg.displayName,
                            baseUrl = cfg.baseUrl,
                            textModel = cfg.textModel,
                            multimodalModel = cfg.multimodalModel.orEmpty(),
                            needsApiKey = cfg.needsApiKey,
                            supportsFileUpload = cfg.supportsFileUpload,
                            enabled = cfg.enabled,
                            isBuiltIn = cfg.isBuiltIn,
                            capabilities = cfg.capabilities,
                            type = cfg.type,
                            requestFormat = cfg.requestFormat,
                            apiKeyMasked = keyStore.maskedPreview(cfg.id),
                            priority = cfg.priority,
                            notes = cfg.notes,
                        )
                    }
                } else {
                    _state.update { ProviderEditState(loading = false, isNew = true) }
                }
            }
        }
    }

    fun update(transform: (ProviderEditState) -> ProviderEditState) = _state.update(transform)

    fun toggleCapability(c: AiCapability) {
        _state.update { s ->
            val next = if (c in s.capabilities) s.capabilities - c else s.capabilities + c
            s.copy(capabilities = next)
        }
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            val s = _state.value
            val id = s.originalId ?: "custom-${UUID.randomUUID().toString().take(8)}"
            val cfg = AiProviderConfig(
                id = id,
                type = s.type,
                displayName = s.displayName.ifBlank { "Custom Provider" },
                baseUrl = s.baseUrl,
                requestFormat = s.requestFormat,
                textModel = s.textModel,
                multimodalModel = s.multimodalModel.takeIf { it.isNotBlank() },
                supportsFileUpload = s.supportsFileUpload,
                capabilities = s.capabilities,
                priority = s.priority,
                enabled = s.enabled,
                isBuiltIn = s.isBuiltIn,
                needsApiKey = s.needsApiKey,
                notes = s.notes,
            )
            repo.upsert(cfg)
            if (s.apiKeyInput.isNotBlank()) {
                keyStore.setApiKey(id, s.apiKeyInput)
            }
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            _state.value.originalId?.let { id ->
                if (!_state.value.isBuiltIn) {
                    repo.delete(id)
                    keyStore.setApiKey(id, null)
                }
            }
            onDone()
        }
    }

    fun clearKey() {
        val id = _state.value.originalId ?: return
        keyStore.setApiKey(id, null)
        _state.update { it.copy(apiKeyMasked = "", apiKeyInput = "") }
    }
}
