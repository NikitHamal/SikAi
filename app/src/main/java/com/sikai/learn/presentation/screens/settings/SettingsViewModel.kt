package com.sikai.learn.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.local.ProviderLogDao
import com.sikai.learn.data.local.ProviderLogEntity
import com.sikai.learn.data.repository.AiProviderRepository
import com.sikai.learn.data.repository.SettingsRepository
import com.sikai.learn.domain.model.AiProviderConfig
import com.sikai.learn.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProviderRow(
    val config: AiProviderConfig,
    val maskedKey: String?,
)

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.System,
    val providers: List<ProviderRow> = emptyList(),
    val recentLogs: List<ProviderLogEntity> = emptyList(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val aiProviders: AiProviderRepository,
    private val logs: ProviderLogDao,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settings.themeMode.collect { mode -> _state.update { it.copy(themeMode = mode) } }
        }
        viewModelScope.launch {
            aiProviders.observeAll().collectLatest { configs ->
                val rows = configs.map { ProviderRow(it, aiProviders.apiKeyMasked(it.id)) }
                _state.update { it.copy(providers = rows) }
            }
        }
        viewModelScope.launch {
            logs.recent().collectLatest { entries -> _state.update { it.copy(recentLogs = entries) } }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    fun setProviderEnabled(id: String, enabled: Boolean) {
        viewModelScope.launch { aiProviders.setEnabled(id, enabled) }
    }

    fun saveApiKey(id: String, apiKey: String) {
        if (apiKey.isBlank()) return
        viewModelScope.launch {
            aiProviders.setApiKey(id, apiKey)
            // Refresh masked previews.
            aiProviders.observeAll().collect { configs ->
                val rows = configs.map { ProviderRow(it, aiProviders.apiKeyMasked(it.id)) }
                _state.update { it.copy(providers = rows) }
                return@collect
            }
        }
    }

    fun reorder(newOrder: List<String>) {
        viewModelScope.launch { aiProviders.reorder(newOrder) }
    }
}
