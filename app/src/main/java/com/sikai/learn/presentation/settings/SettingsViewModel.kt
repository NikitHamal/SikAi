package com.sikai.learn.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.core.security.AppPreferences
import com.sikai.learn.core.security.SecureApiKeyStore
import com.sikai.learn.data.local.AiProviderDao
import com.sikai.learn.data.local.UserDao
import com.sikai.learn.data.local.toDomain
import com.sikai.learn.data.local.toEntity
import com.sikai.learn.domain.ai.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val providerDao: AiProviderDao,
    private val userDao: UserDao,
    private val prefs: AppPreferences,
    private val keys: SecureApiKeyStore,
    private val ai: AiRepository
) : ViewModel() {
    val providers = providerDao.providers().map { it.map { entity -> entity.toDomain() } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val profile = userDao.profile().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val preferences = prefs.flow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    var status = androidx.compose.runtime.mutableStateOf<String?>(null)
    fun saveTheme(theme: String) = viewModelScope.launch { prefs.setTheme(theme) }
    fun setDefaultProvider(id: String) = viewModelScope.launch { prefs.setDefaultProvider(id) }
    fun saveKey(alias: String, key: String) { if (key.isNotBlank()) { keys.save(alias, key); status.value = "Saved key as ${keys.mask(alias)}" } }
    fun clearKeys() { keys.clearAll(); status.value = "All local API keys cleared" }
    fun masked(alias: String?) = keys.mask(alias)
    fun test(config: AiProviderConfig) = viewModelScope.launch { status.value = "${config.name}: ${ai.testProvider(config, keys.get(config.apiKeyAlias))}" }
    fun saveProfile(classLevel: Int, subjects: String) = viewModelScope.launch { profile.value?.let { userDao.upsert(it.copy(classLevel = classLevel, subjectsCsv = subjects)) } }
    fun addCustom(name: String, baseUrl: String, key: String, textModel: String, multimodalModel: String, format: AiRequestFormat, fileUpload: Boolean) = viewModelScope.launch {
        val id = "custom-" + name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-').ifBlank { System.currentTimeMillis().toString() }
        val alias = "$id-key"
        if (key.isNotBlank()) keys.save(alias, key)
        val capabilities = buildSet { add(AiCapability.TEXT); add(AiCapability.VISION); if (fileUpload) add(AiCapability.FILE_UPLOAD); if (fileUpload) add(AiCapability.PDF) }
        providerDao.upsertProvider(AiProviderConfig(id, name.ifBlank { "Custom provider" }, if (format == AiRequestFormat.GEMINI_COMPATIBLE) AiProviderType.CUSTOM_GEMINI else AiProviderType.CUSTOM_OPENAI, baseUrl.trimEnd('/'), alias, textModel, multimodalModel, capabilities, providers.value.size + 10, true, format).toEntity())
        status.value = "Added $name"
    }
}
