package com.sikai.learn.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sikai.learn.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-level user preferences (theme mode, last-selected provider, etc.). Wraps
 * [DataStore] so other layers don't need to know about Preferences keys.
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        when (prefs[KEY_THEME]) {
            "light" -> ThemeMode.Light
            "dark" -> ThemeMode.Dark
            else -> ThemeMode.System
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME] = mode.name.lowercase() }
    }

    val preferredProviderId: Flow<String?> = dataStore.data.map { it[KEY_PROVIDER] }

    suspend fun setPreferredProviderId(id: String?) {
        dataStore.edit { prefs ->
            if (id == null) prefs.remove(KEY_PROVIDER) else prefs[KEY_PROVIDER] = id
        }
    }

    val streakDays: Flow<Int> = dataStore.data.map { it[KEY_STREAK] ?: 0 }
    suspend fun setStreak(days: Int) { dataStore.edit { it[KEY_STREAK] = days } }

    companion object {
        private val KEY_THEME = stringPreferencesKey("theme_mode")
        private val KEY_PROVIDER = stringPreferencesKey("preferred_provider")
        private val KEY_STREAK = intPreferencesKey("streak_days")
    }
}
