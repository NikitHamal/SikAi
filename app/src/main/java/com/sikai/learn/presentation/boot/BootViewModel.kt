package com.sikai.learn.presentation.boot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.repository.AiProviderRepository
import com.sikai.learn.data.repository.SeedBootstrap
import com.sikai.learn.data.repository.SettingsRepository
import com.sikai.learn.data.repository.UserProfileRepository
import com.sikai.learn.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BootState(
    val ready: Boolean = false,
    val isOnboarded: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.System,
)

/**
 * Decides what the very first frame of the app looks like: are we onboarded,
 * what's the theme, and have we seeded the AI provider table. Splash stays up
 * until [BootState.ready] flips true.
 */
@HiltViewModel
class BootViewModel @Inject constructor(
    private val users: UserProfileRepository,
    private val settings: SettingsRepository,
    private val aiProviders: AiProviderRepository,
    private val seed: SeedBootstrap,
) : ViewModel() {

    private val _state = MutableStateFlow(BootState())
    val state: StateFlow<BootState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                aiProviders.ensureBootstrap()
            } catch (e: Exception) {
                android.util.Log.e("BootViewModel", "Bootstrap providers failed", e)
            }
            try {
                seed.ensureSeeded()
            } catch (e: Exception) {
                android.util.Log.e("BootViewModel", "Seed bootstrap failed", e)
            }
            try {
                val onboarded = users.isOnboarded()
                val theme = settings.themeMode.first()
                _state.value = BootState(ready = true, isOnboarded = onboarded, themeMode = theme)
            } catch (e: Exception) {
                android.util.Log.e("BootViewModel", "User state read failed", e)
                _state.value = BootState(ready = true)
            }

            settings.themeMode.collect { mode ->
                _state.value = _state.value.copy(themeMode = mode)
            }
        }
    }
}
