package com.sikai.learn.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sikai.learn.ai.model.AiProviderConfig
import com.sikai.learn.core.storage.SecureKeyStore
import com.sikai.learn.core.storage.UserPreferences
import com.sikai.learn.data.repository.ProviderConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProviderListViewModel @Inject constructor(
    private val repo: ProviderConfigRepository,
    private val keyStore: SecureKeyStore,
    private val prefs: UserPreferences,
) : ViewModel() {

    val providers: Flow<List<AiProviderConfig>> = repo.observeAll()
    val defaultId: Flow<String> = prefs.defaultProviderId
    val defaultMmId: Flow<String> = prefs.defaultMultimodalProviderId

    fun maskedKey(providerId: String): String = keyStore.maskedPreview(providerId)

    fun setEnabled(provider: AiProviderConfig, enabled: Boolean) {
        viewModelScope.launch { repo.upsert(provider.copy(enabled = enabled)) }
    }

    fun setDefault(providerId: String) {
        viewModelScope.launch { prefs.setDefaultProviderId(providerId) }
    }

    fun setDefaultMm(providerId: String) {
        viewModelScope.launch { prefs.setDefaultMultimodalProviderId(providerId) }
    }
}
