package com.sikai.learn.data.repository

import android.content.Context
import com.sikai.learn.BuildConfig
import com.sikai.learn.data.local.ContentDao
import com.sikai.learn.data.local.DownloadedFileEntity
import com.sikai.learn.data.local.PastPaperEntity
import com.sikai.learn.data.local.toDomain
import com.sikai.learn.data.local.toEntity
import com.sikai.learn.domain.content.ContentManifest
import com.sikai.learn.domain.content.ContentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: ContentDao,
    private val client: OkHttpClient,
    private val json: Json
) : ContentRepository {
    override suspend fun refreshManifest(): Result<List<ContentManifest>> = withContext(Dispatchers.IO) {
        runCatching {
            val remote = fetchRemoteManifest().getOrNull()
            val items = remote ?: loadAssetManifest()
            dao.upsertManifest(items.map { it.toEntity() })
            dao.upsertPastPapers(items.filter { it.type == "past_paper" && it.year != null }.map {
                PastPaperEntity(it.id, it.id, it.classLevel, it.subject, it.year ?: 0, it.title)
            })
            items
        }
    }

    override suspend fun download(item: ContentManifest): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val downloads = File(context.filesDir, "downloads").apply { mkdirs() }
            val target = File(downloads, safeFileName(item.id, item.fileUrl ?: item.fileKey ?: item.title))
            if (item.fileUrl?.startsWith("asset://") == true) {
                val assetPath = item.fileUrl.removePrefix("asset://")
                context.assets.open(assetPath).use { input -> target.outputStream().use { input.copyTo(it) } }
            } else {
                val url = item.fileUrl ?: (BuildConfig.DEFAULT_MANIFEST_URL.removeSuffix("/manifest") + "/files/${item.fileKey}")
                val req = Request.Builder().url(url).get().build()
                client.newCall(req).execute().use { response ->
                    check(response.isSuccessful) { "Download failed: HTTP ${response.code}" }
                    response.body?.byteStream()?.use { input -> target.outputStream().use { input.copyTo(it) } } ?: error("Empty file")
                }
            }
            val hash = sha256(target)
            check(item.checksumSha256.isBlank() || hash.equals(item.checksumSha256, ignoreCase = true)) { "Checksum mismatch" }
            dao.upsertDownload(DownloadedFileEntity(item.id, target.absolutePath, target.length(), hash, System.currentTimeMillis()))
            target.absolutePath
        }
    }

    override suspend fun deleteDownload(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            dao.download(id)?.localPath?.let { File(it).delete() }
            dao.deleteDownload(id)
        }
    }

    private fun fetchRemoteManifest(): Result<List<ContentManifest>> = runCatching {
        val req = Request.Builder().url(BuildConfig.DEFAULT_MANIFEST_URL).header("Accept", "application/json").build()
        client.newCall(req).execute().use { response ->
            check(response.isSuccessful) { "HTTP ${response.code}" }
            json.decodeFromString(response.body?.string().orEmpty())
        }
    }

    private fun loadAssetManifest(): List<ContentManifest> = context.assets.open("manifest_seed.json").bufferedReader().use { json.decodeFromString(it.readText()) }

    private fun safeFileName(id: String, source: String): String {
        val ext = source.substringAfterLast('.', "dat").take(8)
        return id.replace(Regex("[^A-Za-z0-9_-]"), "_") + "." + ext
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
