package com.sikai.learn.data.repository

import com.sikai.learn.data.local.ContentManifestDao
import com.sikai.learn.data.local.ContentManifestEntity
import com.sikai.learn.data.local.DownloadedFileDao
import com.sikai.learn.data.local.QuestionDao
import com.sikai.learn.data.local.QuestionEntity
import com.sikai.learn.data.local.SubjectDao
import com.sikai.learn.data.local.SubjectEntity
import com.sikai.learn.data.remote.BackendApi
import com.sikai.learn.data.remote.RemoteQuestion
import com.sikai.learn.data.remote.RemoteSubject
import com.sikai.learn.domain.model.ContentManifestEntry
import com.sikai.learn.domain.model.ContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentManifestRepository @Inject constructor(
    private val api: BackendApi,
    private val manifestDao: ContentManifestDao,
    private val downloadedDao: DownloadedFileDao,
) {

    fun observeAll(): Flow<List<ManifestRow>> =
        combine(manifestDao.observeAll(), downloadedDao.observeAll()) { manifest, downloaded ->
            val downloadedById = downloaded.associateBy { it.manifestId }
            manifest.map { it.toRow(downloadedById[it.id] != null) }
        }

    fun observeForClass(classLevel: Int): Flow<List<ManifestRow>> =
        combine(manifestDao.observeForClass(classLevel), downloadedDao.observeAll()) { manifest, downloaded ->
            val downloadedById = downloaded.associateBy { it.manifestId }
            manifest.map { it.toRow(downloadedById[it.id] != null) }
        }

    suspend fun refreshAll(): Result<Int> = runCatching {
        val remote = api.manifest()
        val entities = remote.items.map { it.toEntity() }
        manifestDao.upsertAll(entities)
        manifestDao.deleteAllExcept(entities.map { it.id })
        entities.size
    }

    suspend fun refreshForClass(classLevel: Int): Result<Int> = runCatching {
        val remote = api.manifestForClass(classLevel)
        val entities = remote.items.map { it.toEntity() }
        manifestDao.upsertAll(entities)
        entities.size
    }
}

data class ManifestRow(
    val id: String,
    val title: String,
    val type: String,
    val classLevel: Int,
    val subject: String,
    val year: Int?,
    val sizeBytes: Long,
    val fileUrl: String?,
    val checksumSha256: String?,
    val isDownloaded: Boolean,
)

private fun ContentManifestEntity.toRow(downloaded: Boolean) = ManifestRow(
    id = id,
    title = title,
    type = type,
    classLevel = classLevel,
    subject = subject,
    year = year,
    sizeBytes = sizeBytes,
    fileUrl = fileUrl,
    checksumSha256 = checksumSha256,
    isDownloaded = downloaded,
)

private fun ContentManifestEntry.toEntity() = ContentManifestEntity(
    id = id,
    title = title,
    type = when (type) {
        ContentType.TEXTBOOK -> "textbook"
        ContentType.PAST_PAPER -> "past_paper"
        ContentType.MCQ_PACK -> "mcq_pack"
        ContentType.SYLLABUS -> "syllabus"
        ContentType.NOTES -> "notes"
    },
    classLevel = classLevel,
    subject = subject,
    year = year,
    fileUrl = fileUrl,
    sizeBytes = sizeBytes,
    checksumSha256 = checksumSha256,
    version = version,
    updatedAt = updatedAt,
    language = language,
    tagsCsv = tags.joinToString(","),
)