package com.sikai.learn.data.repository

import com.sikai.learn.data.local.AiProviderConfigDao
import com.sikai.learn.data.local.AiProviderConfigEntity
import com.sikai.learn.data.secure.EncryptedKeyStore
import com.sikai.learn.domain.model.AiCapability
import com.sikai.learn.domain.model.AiProviderConfig
import com.sikai.learn.domain.model.AiRequestFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiProviderRepository @Inject constructor(
    private val dao: AiProviderConfigDao,
    private val keyStore: EncryptedKeyStore,
) {

    fun observeAll(): Flow<List<AiProviderConfig>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeEnabled(): Flow<List<AiProviderConfig>> =
        dao.observeEnabled().map { list -> list.map { it.toDomain() } }

    suspend fun enabledOrdered(): List<AiProviderConfig> =
        dao.observeEnabled().first().map { it.toDomain() }

    suspend fun byId(id: String): AiProviderConfig? = dao.byId(id)?.toDomain()

    suspend fun upsert(config: AiProviderConfig) = dao.upsert(config.toEntity())

    suspend fun delete(id: String) {
        val cfg = dao.byId(id) ?: return
        cfg.apiKeyAlias?.let { keyStore.remove(it) }
        dao.delete(id)
    }

    suspend fun setApiKey(configId: String, apiKey: String) {
        val cfg = dao.byId(configId) ?: return
        val alias = cfg.apiKeyAlias ?: "provider_key_${configId}"
        keyStore.put(alias, apiKey)
        if (cfg.apiKeyAlias == null) dao.upsert(cfg.copy(apiKeyAlias = alias))
    }

    suspend fun ensureBootstrap() {
        if (dao.count() != 0) return
        val defaults = DefaultAiProviders.builtIns()
        dao.upsertAll(defaults.map { it.toEntity() })
    }

    suspend fun apiKeyMasked(configId: String): String? {
        val cfg = dao.byId(configId) ?: return null
        val alias = cfg.apiKeyAlias ?: return null
        return keyStore.masked(alias)
    }

    suspend fun setEnabled(id: String, enabled: Boolean) {
        val cfg = dao.byId(id) ?: return
        dao.upsert(cfg.copy(enabled = enabled))
    }

    suspend fun reorder(idsInOrder: List<String>) {
        val configs = dao.byIds(idsInOrder).associateBy { it.id }
        val toUpdate = idsInOrder.mapIndexedNotNull { index, id ->
            val cfg = configs[id] ?: return@mapIndexedNotNull null
            if (cfg.priority != index) cfg.copy(priority = index) else null
        }
        if (toUpdate.isNotEmpty()) {
            dao.upsertAll(toUpdate)
        }
    }
}

private fun AiProviderConfigEntity.toDomain() = AiProviderConfig(
    id = id,
    type = type,
    displayName = displayName,
    baseUrl = baseUrl,
    apiKeyAlias = apiKeyAlias,
    defaultTextModel = defaultTextModel,
    defaultMultimodalModel = defaultMultimodalModel,
    requestFormat = runCatching { AiRequestFormat.valueOf(requestFormat) }.getOrDefault(AiRequestFormat.OPENAI_COMPATIBLE),
    supportsFileUpload = supportsFileUpload,
    capabilities = capabilitiesCsv.split(",")
        .filter { it.isNotBlank() }
        .mapNotNull { runCatching { AiCapability.valueOf(it) }.getOrNull() }
        .toSet(),
    priority = priority,
    enabled = enabled,
    isBuiltIn = isBuiltIn,
)

private fun AiProviderConfig.toEntity() = AiProviderConfigEntity(
    id = id,
    type = type,
    displayName = displayName,
    baseUrl = baseUrl,
    apiKeyAlias = apiKeyAlias,
    defaultTextModel = defaultTextModel,
    defaultMultimodalModel = defaultMultimodalModel,
    requestFormat = requestFormat.name,
    supportsFileUpload = supportsFileUpload,
    capabilitiesCsv = capabilities.joinToString(",") { it.name },
    priority = priority,
    enabled = enabled,
    isBuiltIn = isBuiltIn,
)
