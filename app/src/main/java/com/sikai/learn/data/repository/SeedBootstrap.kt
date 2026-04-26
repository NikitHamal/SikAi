package com.sikai.learn.data.repository

import android.content.Context
import com.sikai.learn.data.local.ContentManifestDao
import com.sikai.learn.data.local.ContentManifestEntity
import com.sikai.learn.data.local.QuestionDao
import com.sikai.learn.data.local.QuestionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-shot seeder that populates the question bank and an offline content
 * manifest from bundled assets the first time the app launches. Once the DB
 * has rows the seeder becomes a no-op so the on-device data isn't clobbered.
 */
@Singleton
class SeedBootstrap @Inject constructor(
    @ApplicationContext private val context: Context,
    private val questionDao: QuestionDao,
    private val manifestDao: ContentManifestDao,
    private val json: Json,
) {

    suspend fun ensureSeeded() {
        seedQuestionsIfEmpty()
        seedManifestIfEmpty()
    }

    private suspend fun seedQuestionsIfEmpty() {
        if (questionDao.count() != 0) return
        val raw = readAsset("seed/questions.json") ?: return
        val items = runCatching { json.decodeFromString<List<SeedQuestion>>(raw) }
            .getOrNull() ?: return
        if (items.isEmpty()) return
        questionDao.upsertAll(items.mapIndexed { idx, q ->
            QuestionEntity(
                id = "seed-$idx-${q.subject}-${q.classLevel}",
                classLevel = q.classLevel,
                subject = q.subject,
                topic = q.topic,
                prompt = q.prompt,
                optionsJson = q.options.joinToString("\u0001"),
                correctIndex = q.correctIndex.coerceIn(0, q.options.lastIndex),
                explanation = q.explanation,
                source = "seed",
            )
        })
    }

    private suspend fun seedManifestIfEmpty() {
        // Only seed if the local cache is empty. The repository's refresh path
        // replaces these rows the first time the worker is reachable.
        if (manifestDao.count() != 0) return
        val raw = readAsset("seed/manifest.json") ?: return
        val pack = runCatching { json.decodeFromString<SeedManifestPack>(raw) }.getOrNull() ?: return
        if (pack.items.isEmpty()) return
        manifestDao.upsertAll(pack.items.map { it.toEntity() })
    }

    private fun readAsset(path: String): String? = runCatching {
        context.assets.open(path).bufferedReader().use { it.readText() }
    }.getOrNull()
}

@Serializable
private data class SeedQuestion(
    val classLevel: Int,
    val subject: String,
    val topic: String,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String? = null,
)

@Serializable
private data class SeedManifestPack(val items: List<SeedManifestItem>)

@Serializable
private data class SeedManifestItem(
    val id: String,
    val title: String,
    val type: String,
    val classLevel: Int,
    val subject: String,
    val year: Int? = null,
    val fileUrl: String? = null,
    val fileKey: String? = null,
    val sizeBytes: Long = 0,
    val checksumSha256: String? = null,
    val version: Int = 1,
    val updatedAt: Long = 0,
    val language: String = "en",
    val tags: List<String> = emptyList(),
) {
    fun toEntity() = ContentManifestEntity(
        id = id,
        title = title,
        type = type,
        classLevel = classLevel,
        subject = subject,
        year = year,
        fileUrl = fileUrl,
        fileKey = fileKey,
        sizeBytes = sizeBytes,
        checksumSha256 = checksumSha256,
        version = version,
        updatedAt = updatedAt,
        language = language,
        tagsCsv = tags.joinToString(","),
    )
}
