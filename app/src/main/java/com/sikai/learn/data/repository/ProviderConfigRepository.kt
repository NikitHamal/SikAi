package com.sikai.learn.data.repository

import com.sikai.learn.ai.model.AiCapability
import com.sikai.learn.ai.model.AiProviderConfig
import com.sikai.learn.ai.model.AiProviderType
import com.sikai.learn.ai.model.AiRequestFormat
import com.sikai.learn.ai.providers.deepinfra.DeepInfraProvider
import com.sikai.learn.ai.providers.deepseek.DeepSeekProvider
import com.sikai.learn.ai.providers.gemini.GeminiProvider
import com.sikai.learn.ai.providers.nvidia.NvidiaProvider
import com.sikai.learn.ai.providers.openrouter.OpenRouterProvider
import com.sikai.learn.ai.providers.qwen.QwenDefaults
import com.sikai.learn.data.db.dao.AiProviderConfigDao
import com.sikai.learn.data.db.entity.AiProviderConfigEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderConfigRepository @Inject constructor(
    private val dao: AiProviderConfigDao,
) {
    fun observeAll(): Flow<List<AiProviderConfig>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    suspend fun upsert(config: AiProviderConfig) = dao.upsert(config.toEntity())
    suspend fun delete(id: String) = dao.delete(id)

    suspend fun findById(id: String): AiProviderConfig? = dao.byId(id)?.toDomain()

    /**
     * Synchronous variant used by [com.sikai.learn.ai.provider.AiProviderRegistry] when
     * resolving providers in non-suspending contexts. Backed by Room's blocking executor.
     */
    fun allActiveConfigsBlocking(): List<AiProviderConfig> = runBlocking { dao.all().map { it.toDomain() } }
    fun findByIdBlocking(id: String): AiProviderConfig? = runBlocking { dao.byId(id)?.toDomain() }

    suspend fun seedBuiltInsIfMissing() {
        val existing = dao.all().map { it.id }.toSet()
        val builtIns = listOf(
            QwenDefaults.config(),
            DeepInfraProvider.defaultConfig(),
            GeminiProvider.defaultConfig(),
            OpenRouterProvider.defaultConfig(),
            NvidiaProvider.defaultConfig(),
            DeepSeekProvider.defaultConfig(),
        )
        builtIns.filter { it.id !in existing }.forEach { dao.upsert(it.toEntity()) }
    }
}

internal fun AiProviderConfig.toEntity() = AiProviderConfigEntity(
    id = id,
    type = type.name,
    displayName = displayName,
    baseUrl = baseUrl,
    requestFormat = requestFormat.name,
    textModel = textModel,
    multimodalModel = multimodalModel,
    supportsFileUpload = supportsFileUpload,
    capabilitiesCsv = capabilities.joinToString(",") { it.name },
    priority = priority,
    enabled = enabled,
    isBuiltIn = isBuiltIn,
    needsApiKey = needsApiKey,
    notes = notes,
)

internal fun AiProviderConfigEntity.toDomain() = AiProviderConfig(
    id = id,
    type = runCatching { AiProviderType.valueOf(type) }.getOrDefault(AiProviderType.CUSTOM),
    displayName = displayName,
    baseUrl = baseUrl,
    requestFormat = runCatching { AiRequestFormat.valueOf(requestFormat) }.getOrDefault(AiRequestFormat.OPENAI_COMPATIBLE),
    textModel = textModel,
    multimodalModel = multimodalModel,
    supportsFileUpload = supportsFileUpload,
    capabilities = capabilitiesCsv.split(",").mapNotNull { name -> runCatching { AiCapability.valueOf(name) }.getOrNull() }.toSet(),
    priority = priority,
    enabled = enabled,
    isBuiltIn = isBuiltIn,
    needsApiKey = needsApiKey,
    notes = notes,
)
