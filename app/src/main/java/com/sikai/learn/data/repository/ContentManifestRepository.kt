package com.sikai.learn.data.repository

import android.content.Context
import com.sikai.learn.data.db.dao.ContentManifestDao
import com.sikai.learn.data.db.entity.ContentManifestEntity
import com.sikai.learn.data.remote.ManifestApi
import com.sikai.learn.data.remote.ManifestDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentManifestRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: ContentManifestDao,
    private val api: ManifestApi,
    private val json: Json,
) {
    fun observeAll(): Flow<List<ContentManifestEntity>> = dao.observeAll()
    fun observeForClass(classLevel: Int): Flow<List<ContentManifestEntity>> = dao.observeForClass(classLevel)
    suspend fun byId(id: String): ContentManifestEntity? = dao.byId(id)

    suspend fun refreshFromBackend(classLevel: Int? = null): Result<Int> = runCatching {
        val manifest = api.fetchManifest(classLevel)
        store(manifest)
        manifest.entries.size
    }

    suspend fun seedFromAssetsIfEmpty() {
        // Fall back to local sample manifest the first time, so the Downloads tab is never empty.
        val anything = dao.observeAll() // we don't need to collect; just use direct query
        runCatching {
            val raw = context.assets.open("seed_manifest.json").bufferedReader().use { it.readText() }
            val manifest = json.decodeFromString(ManifestDto.serializer(), raw)
            store(manifest)
        }
    }

    private suspend fun store(manifest: ManifestDto) {
        dao.insertAll(
            manifest.entries.map {
                ContentManifestEntity(
                    id = it.id,
                    title = it.title,
                    type = it.type,
                    classLevel = it.classLevel,
                    subject = it.subject,
                    year = it.year,
                    fileUrl = it.fileUrl,
                    fileKey = it.fileKey,
                    sizeBytes = it.sizeBytes,
                    checksumSha256 = it.checksumSha256,
                    version = it.version,
                    updatedAt = it.updatedAt,
                    language = it.language,
                    tagsCsv = it.tags.joinToString(","),
                    description = it.description,
                )
            },
        )
    }
}
