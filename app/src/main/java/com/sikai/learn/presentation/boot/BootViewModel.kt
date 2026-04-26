package com.sikai.learn.presentation.boot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.repository.AiProviderRepository
import com.sikai.learn.data.repository.SeedBootstrap
import com.sikai.learn.data.repository.SettingsRepository
import com.sikai.learn.data.repository.UserProfileRepository
import com.sikai.learn.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

data class BootState(
    val ready: Boolean = false,
    val isOnboarded: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.System,
)

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
                withTimeout(10_000L) {
                    withContext(Dispatchers.IO) {
                        try {
                            aiProviders.ensureBootstrap()
                        } catch (e: Exception) {
                            Log.e(TAG, "Bootstrap providers failed", e)
                        }
                        try {
                            seed.ensureSeeded()
                        } catch (e: Exception) {
                            Log.e(TAG, "Seed bootstrap failed", e)
                        }
                    }
                    try {
                        val onboarded = users.isOnboarded()
                        val theme = settings.themeMode.first()
                        _state.value = BootState(ready = true, isOnboarded = onboarded, themeMode = theme)
                    } catch (e: Exception) {
                        Log.e(TAG, "User state read failed", e)
                        _state.value = BootState(ready = true)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Boot timed out, forcing ready", e)
                _state.value = BootState(ready = true)
            }

            try {
                settings.themeMode.collect { mode ->
                    _state.value = _state.value.copy(themeMode = mode)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Theme collect failed", e)
            }
        }
    }

    companion object {
        private const val TAG = "BootViewModel"
    }
}
