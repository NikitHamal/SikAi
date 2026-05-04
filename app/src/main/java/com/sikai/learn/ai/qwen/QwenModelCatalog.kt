package com.sikai.learn.ai.qwen

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sikai.learn.domain.model.AiCapability
import com.sikai.learn.domain.model.AiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QwenModelCatalog @Inject constructor(
    private val client: OkHttpClient,
    private val dataStore: DataStore<Preferences>,
) {
    @Volatile private var memoryCache: List<AiModel> = emptyList()

    suspend fun get(forceRefresh: Boolean = false): List<AiModel> = withContext(Dispatchers.IO) {
        val cachedAt = getCachedAt()
        val isExpired = (System.currentTimeMillis() - cachedAt) > TTL_MS

        if (!forceRefresh && memoryCache.isNotEmpty() && !isExpired) {
            return@withContext memoryCache
        }

        val persisted = if (!forceRefresh && memoryCache.isEmpty()) loadPersisted() else null
        if (persisted != null && !isExpired) {
            memoryCache = persisted
            return@withContext persisted
        }

        val models = fetchFromQwen()
        if (models.isNotEmpty()) {
            memoryCache = models
            savePersisted(models)
        }
        models.ifEmpty { FALLBACK }
    }

    private fun fetchFromQwen(): List<AiModel> {
        val req = Request.Builder()
            .url("https://chat.qwen.ai/api/v2/models")
            .header("Accept", "application/json")
            .build()
        return runCatching {
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use emptyList()
                val text = resp.body?.string().orEmpty()
                val json = Json.parseToJsonElement(text) as? JsonObject
                mapResponse(json)
            }
        }.getOrDefault(emptyList())
    }

    private fun mapResponse(root: JsonObject): List<AiModel> {
        val data = root["data"] as? JsonObject
        val list = (data?.get("data") as? JsonArray) ?: return emptyList()
        return list.mapNotNull { el ->
            val m = el as? JsonObject ?: return@mapNotNull null
            val info = m["info"] as? JsonObject ?: return@mapNotNull null
            val active = (info["is_active"] as? JsonPrimitive)?.content?.toBoolean() ?: false
            val visitor = (info["is_visitor_active"] as? JsonPrimitive)?.content?.toBoolean() ?: false
            if (!active && !visitor) return@mapNotNull null
            val mid = (m["id"] as? JsonPrimitive)?.content ?: return@mapNotNull null
            val name = (m["name"] as? JsonPrimitive)?.content ?: mid
            val meta = info["meta"] as? JsonObject
            val caps = (meta?.get("capabilities") as? JsonObject)
            val capabilities = mutableSetOf<AiCapability>(AiCapability.TEXT, AiCapability.STREAMING)
            if ((caps?.get("vision") as? JsonPrimitive)?.content?.toBoolean() == true) capabilities += AiCapability.VISION
            if ((caps?.get("document") as? JsonPrimitive)?.content?.toBoolean() == true) {
                capabilities += AiCapability.PDF
                capabilities += AiCapability.FILE_UPLOAD
            }
            if ((caps?.get("thinking") as? JsonPrimitive)?.content?.toBoolean() == true) capabilities += AiCapability.THINKING
            if ((caps?.get("search") as? JsonPrimitive)?.content?.toBoolean() == true) capabilities += AiCapability.SEARCH
            AiModel(
                id = mid,
                displayName = name,
                providerId = "qwen",
                capabilities = capabilities,
                maxContext = (meta?.get("max_context_length") as? JsonPrimitive)?.content?.toIntOrNull() ?: 1_000_000,
                description = (meta?.get("description") as? JsonPrimitive)?.content.orEmpty(),
                supportsThinking = AiCapability.THINKING in capabilities,
                supportsSearch = AiCapability.SEARCH in capabilities,
            )
        }
    }

    private suspend fun getCachedAt(): Long = dataStore.data.first()[KEY_CACHED_AT] ?: 0L

    private suspend fun loadPersisted(): List<AiModel>? = dataStore.data.first()[KEY_MODELS]?.let { jsonStr ->
        try {
            Json.decodeFromString<List<AiModel>>(jsonStr)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun savePersisted(models: List<AiModel>) {
        dataStore.edit { prefs ->
            prefs[KEY_MODELS] = Json.encodeToString(models)
            prefs[KEY_CACHED_AT] = System.currentTimeMillis()
        }
    }

    companion object {
        private const val TTL_MS = 24 * 60 * 60 * 1000L
        private val KEY_MODELS = stringPreferencesKey("qwen_models_cache")
        private val KEY_CACHED_AT = longPreferencesKey("qwen_models_cached_at")

        val FALLBACK = listOf(
            AiModel(
                id = "qwen3.6-plus",
                displayName = "Qwen3.6-Plus",
                providerId = "qwen",
                capabilities = setOf(
                    AiCapability.TEXT, AiCapability.VISION,
                    AiCapability.PDF, AiCapability.STREAMING,
                    AiCapability.THINKING, AiCapability.SEARCH,
                    AiCapability.FILE_UPLOAD,
                ),
                maxContext = 1_000_000,
                description = "Latest Qwen series; multimodal + thinking + search",
                supportsThinking = true,
                supportsSearch = true,
            ),
            AiModel(
                id = "qwen3.6-max-preview",
                displayName = "Qwen3.6-Max-Preview",
                providerId = "qwen",
                capabilities = setOf(
                    AiCapability.TEXT, AiCapability.PDF,
                    AiCapability.STREAMING, AiCapability.THINKING,
                ),
                maxContext = 262_144,
                description = "Flagship Qwen3.6 model; best text reasoning, no vision yet",
                supportsThinking = true,
            ),
            AiModel(
                id = "qwen3.6-27b",
                displayName = "Qwen3.6-27B",
                providerId = "qwen",
                capabilities = setOf(
                    AiCapability.TEXT, AiCapability.VISION,
                    AiCapability.PDF, AiCapability.STREAMING,
                    AiCapability.THINKING, AiCapability.FILE_UPLOAD,
                ),
                maxContext = 262_144,
                description = "Open-source 27B dense model; multimodal + thinking",
                supportsThinking = true,
            ),
        )
    }
}
