package com.sikai.learn.presentation.boot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.data.repository.AiProviderRepository
import com.sikai.learn.data.repository.ContentManifestRepository
import com.sikai.learn.data.repository.SeedBootstrap
import com.sikai.learn.data.repository.SettingsRepository
import com.sikai.learn.data.repository.SyncRepository
import com.sikai.learn.data.repository.UserProfileRepository
import com.sikai.learn.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val contentRepo: ContentManifestRepository,
    private val syncRepo: SyncRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BootState())
    val state: StateFlow<BootState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            Log.d(TAG, "Boot starting")
            try {
                withTimeout(10_000L) {
                    withContext(Dispatchers.IO) {
                        Log.d(TAG, "Boot: seeding AI providers")
                        try {
                            aiProviders.ensureBootstrap()
                            Log.d(TAG, "Boot: AI providers seeded OK")
                        } catch (e: Exception) {
                            Log.e(TAG, "Bootstrap providers failed", e)
                        }
                        try {
                            Log.d(TAG, "Boot: seeding questions/manifest")
                            seed.ensureSeeded()
                            Log.d(TAG, "Boot: seed OK")
                        } catch (e: Exception) {
                            Log.e(TAG, "Seed bootstrap failed", e)
                        }
                        try {
                            Log.d(TAG, "Boot: syncing from remote")
                            contentRepo.refreshAll()
                            Log.d(TAG, "Boot: manifest sync OK")
                        } catch (e: Exception) {
                            Log.e(TAG, "Manifest sync failed", e)
                        }
                        try {
                            Log.d(TAG, "Boot: syncing subjects from remote")
                            syncRepo.syncSubjects()
                            Log.d(TAG, "Boot: subjects sync OK")
                        } catch (e: Exception) {
                            Log.e(TAG, "Subjects sync failed", e)
                        }
                        try {
                            val classLevel = users.get()?.classLevel ?: 10
                            Log.d(TAG, "Boot: syncing questions for class $classLevel")
                            syncRepo.syncQuestions(classLevel)
                            Log.d(TAG, "Boot: questions sync OK")
                        } catch (e: Exception) {
                            Log.e(TAG, "Questions sync failed", e)
                        }
                    }
                    Log.d(TAG, "Boot: reading user state")
                    try {
                        val onboarded = users.isOnboarded()
                        Log.d(TAG, "Boot: isOnboarded=$onboarded")
                        val theme = settings.themeMode.first()
                        Log.d(TAG, "Boot: theme=$theme")
                        _state.value = BootState(ready = true, isOnboarded = onboarded, themeMode = theme)
                    } catch (e: Exception) {
                        Log.e(TAG, "User state read failed, defaulting", e)
                        _state.value = BootState(ready = true)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Boot timed out, forcing ready", e)
                _state.value = BootState(ready = true)
            }
            Log.d(TAG, "Boot complete, ready=true")

            try {
                settings.themeMode.collect { mode ->
                    _state.value = _state.value.copy(themeMode = mode)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Theme collect failed", e)
            }
        }

        viewModelScope.launch {
            delay(5_000L)
            if (!_state.value.ready) {
                Log.e(TAG, "Boot safety timeout fired, forcing ready")
                _state.value = BootState(ready = true)
            }
        }
    }

    companion object {
        private const val TAG = "BootViewModel"
    }
}
