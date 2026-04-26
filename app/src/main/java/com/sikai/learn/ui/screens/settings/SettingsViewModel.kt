package com.sikai.learn.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.core.storage.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferences,
) : ViewModel() {

    val themeMode: Flow<String> = prefs.themeMode
    val language: Flow<String> = prefs.language
    val classLevel: Flow<Int> = prefs.classLevel
    val nepaliMode: Flow<Boolean> = prefs.nepaliMode
    val defaultProviderId: Flow<String> = prefs.defaultProviderId
    val defaultMultimodalProviderId: Flow<String> = prefs.defaultMultimodalProviderId

    fun setTheme(mode: String) = viewModelScope.launch { prefs.setThemeMode(mode) }
    fun setLanguage(lang: String) = viewModelScope.launch { prefs.setLanguage(lang) }
    fun setClassLevel(level: Int) = viewModelScope.launch { prefs.setClassLevel(level) }
    fun setNepaliMode(value: Boolean) = viewModelScope.launch { prefs.setNepaliMode(value) }
}
