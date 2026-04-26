package com.sikai.learn.core.security

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("sikai_preferences")

data class UserPreferences(val theme: String = "system", val defaultProviderId: String = "qwen", val language: String = "en")

@Singleton
class AppPreferences @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val defaultProviderId = stringPreferencesKey("default_provider_id")
        val language = stringPreferencesKey("language")
    }

    val flow: Flow<UserPreferences> = context.dataStore.data.map { prefs: Preferences ->
        UserPreferences(
            theme = prefs[Keys.theme] ?: "system",
            defaultProviderId = prefs[Keys.defaultProviderId] ?: "qwen",
            language = prefs[Keys.language] ?: "en"
        )
    }

    suspend fun setTheme(theme: String) = context.dataStore.edit { it[Keys.theme] = theme }.let { Unit }
    suspend fun setDefaultProvider(id: String) = context.dataStore.edit { it[Keys.defaultProviderId] = id }.let { Unit }
    suspend fun setLanguage(language: String) = context.dataStore.edit { it[Keys.language] = language }.let { Unit }
}
