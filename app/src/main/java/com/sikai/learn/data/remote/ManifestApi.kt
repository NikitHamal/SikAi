package com.sikai.learn.data.remote

import com.sikai.learn.BuildConfig
import com.sikai.learn.core.di.ContentHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ManifestEntryDto(
    val id: String,
    val title: String,
    val type: String,
    val classLevel: Int,
    val subject: String,
    val year: Int? = null,
    val fileUrl: String,
    val fileKey: String? = null,
    val sizeBytes: Long,
    val checksumSha256: String,
    val version: Int = 1,
    val updatedAt: Long,
    val language: String = "en",
    val tags: List<String> = emptyList(),
    val description: String = "",
)

@Serializable
data class ManifestDto(
    val version: Int,
    val updatedAt: Long,
    val entries: List<ManifestEntryDto>,
)

@Serializable
data class QuestionPackDto(
    val id: String,
    val classLevel: Int,
    val subject: String,
    val topic: String,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val difficulty: Int = 2,
)

@Singleton
class ManifestApi @Inject constructor(
    @ContentHttp private val client: OkHttpClient,
    private val json: Json,
) {
    private val base = BuildConfig.CONTENT_API_BASE.trimEnd('/')

    suspend fun fetchManifest(classLevel: Int? = null): ManifestDto = withContext(Dispatchers.IO) {
        val path = if (classLevel != null) "/manifest/$classLevel" else "/manifest"
        val req = Request.Builder()
            .url("$base$path")
            .header("Accept", "application/json")
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("manifest http ${resp.code}")
            val body = resp.body?.string().orEmpty()
            json.decodeFromString(ManifestDto.serializer(), body)
        }
    }

    suspend fun fetchQuestionPack(classLevel: Int, subject: String): List<QuestionPackDto> =
        withContext(Dispatchers.IO) {
            val req = Request.Builder()
                .url("$base/questions?class=$classLevel&subject=$subject")
                .header("Accept", "application/json")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use emptyList()
                val body = resp.body?.string().orEmpty()
                runCatching {
                    json.decodeFromString(
                        kotlinx.serialization.builtins.ListSerializer(QuestionPackDto.serializer()),
                        body,
                    )
                }.getOrDefault(emptyList())
            }
        }
}
