package com.sikai.learn.core.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "sikai_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val themeKey = stringPreferencesKey("theme_mode")
    private val languageKey = stringPreferencesKey("language")
    private val classKey = intPreferencesKey("class_level")
    private val subjectsKey = stringPreferencesKey("subjects_csv")
    private val examDateKey = longPreferencesKey("exam_date")
    private val onboardedKey = booleanPreferencesKey("onboarded")
    private val defaultProviderKey = stringPreferencesKey("default_provider_id")
    private val defaultMultimodalProviderKey = stringPreferencesKey("default_multimodal_provider_id")
    private val streakKey = intPreferencesKey("streak_days")
    private val xpKey = intPreferencesKey("xp")
    private val lastActiveKey = longPreferencesKey("last_active_at")
    private val nepaliModeKey = booleanPreferencesKey("nepali_mode")

    val themeMode: Flow<String> = context.dataStore.data.map { it[themeKey] ?: "system" }
    val language: Flow<String> = context.dataStore.data.map { it[languageKey] ?: "en" }
    val classLevel: Flow<Int> = context.dataStore.data.map { it[classKey] ?: 10 }
    val subjects: Flow<List<String>> = context.dataStore.data.map { (it[subjectsKey] ?: "").split(",").filter { s -> s.isNotBlank() } }
    val examDate: Flow<Long?> = context.dataStore.data.map { it[examDateKey] }
    val onboarded: Flow<Boolean> = context.dataStore.data.map { it[onboardedKey] ?: false }
    val defaultProviderId: Flow<String> = context.dataStore.data.map { it[defaultProviderKey] ?: "qwen-builtin" }
    val defaultMultimodalProviderId: Flow<String> = context.dataStore.data.map { it[defaultMultimodalProviderKey] ?: "qwen-builtin" }
    val streakDays: Flow<Int> = context.dataStore.data.map { it[streakKey] ?: 0 }
    val xp: Flow<Int> = context.dataStore.data.map { it[xpKey] ?: 0 }
    val lastActiveAt: Flow<Long> = context.dataStore.data.map { it[lastActiveKey] ?: 0L }
    val nepaliMode: Flow<Boolean> = context.dataStore.data.map { it[nepaliModeKey] ?: false }

    suspend fun setThemeMode(mode: String) = context.dataStore.edit { it[themeKey] = mode }
    suspend fun setLanguage(lang: String) = context.dataStore.edit { it[languageKey] = lang }
    suspend fun setClassLevel(level: Int) = context.dataStore.edit { it[classKey] = level }
    suspend fun setSubjects(subjects: List<String>) = context.dataStore.edit { it[subjectsKey] = subjects.joinToString(",") }
    suspend fun setExamDate(date: Long?) = context.dataStore.edit {
        if (date == null) it.remove(examDateKey) else it[examDateKey] = date
    }
    suspend fun setOnboarded(value: Boolean) = context.dataStore.edit { it[onboardedKey] = value }
    suspend fun setDefaultProviderId(id: String) = context.dataStore.edit { it[defaultProviderKey] = id }
    suspend fun setDefaultMultimodalProviderId(id: String) = context.dataStore.edit { it[defaultMultimodalProviderKey] = id }
    suspend fun setStreak(days: Int) = context.dataStore.edit { it[streakKey] = days }
    suspend fun setXp(xp: Int) = context.dataStore.edit { it[xpKey] = xp }
    suspend fun setLastActiveAt(at: Long) = context.dataStore.edit { it[lastActiveKey] = at }
    suspend fun setNepaliMode(value: Boolean) = context.dataStore.edit { it[nepaliModeKey] = value }
}
