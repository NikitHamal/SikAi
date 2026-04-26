package com.sikai.learn.data.repository

import android.content.Context
import com.sikai.learn.core.di.DownloadHttp
import com.sikai.learn.data.db.dao.ContentManifestDao
import com.sikai.learn.data.db.dao.DownloadedFileDao
import com.sikai.learn.data.db.entity.ContentManifestEntity
import com.sikai.learn.data.db.entity.DownloadedFileEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

sealed class DownloadProgress {
    data object Queued : DownloadProgress()
    data class InProgress(val percent: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadProgress()
    data class Done(val file: DownloadedFileEntity) : DownloadProgress()
    data class Error(val message: String) : DownloadProgress()
}

@Singleton
class DownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @DownloadHttp private val client: OkHttpClient,
    private val dao: DownloadedFileDao,
    private val manifestDao: ContentManifestDao,
) {
    fun observeAll(): Flow<List<DownloadedFileEntity>> = dao.observeAll()
    suspend fun forManifest(manifestId: String): DownloadedFileEntity? = dao.forManifest(manifestId)

    suspend fun deleteDownload(manifestId: String) {
        val existing = dao.forManifest(manifestId)
        if (existing != null) {
            runCatching { File(existing.localPath).delete() }
            dao.delete(manifestId)
        }
    }

    suspend fun download(manifest: ContentManifestEntity, onProgress: (DownloadProgress) -> Unit) {
        onProgress(DownloadProgress.Queued)
        withContext(Dispatchers.IO) {
            try {
                val dir = File(context.filesDir, "downloads").apply { mkdirs() }
                val target = File(dir, "${manifest.id}.bin")

                val req = Request.Builder().url(manifest.fileUrl).build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        onProgress(DownloadProgress.Error("HTTP ${resp.code}"))
                        return@use
                    }
                    val body = resp.body ?: run {
                        onProgress(DownloadProgress.Error("Empty body"))
                        return@use
                    }
                    val total = body.contentLength().takeIf { it > 0 } ?: manifest.sizeBytes
                    val sha = MessageDigest.getInstance("SHA-256")
                    var copied = 0L

                    body.byteStream().use { input ->
                        target.outputStream().use { output ->
                            val buf = ByteArray(8 * 1024)
                            while (true) {
                                val n = input.read(buf)
                                if (n <= 0) break
                                output.write(buf, 0, n)
                                sha.update(buf, 0, n)
                                copied += n
                                if (total > 0) {
                                    onProgress(
                                        DownloadProgress.InProgress(
                                            percent = ((copied * 100) / total).toInt().coerceIn(0, 100),
                                            bytesDownloaded = copied,
                                            totalBytes = total,
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    val computed = sha.digest().joinToString("") { "%02x".format(it) }
                    val verified = manifest.checksumSha256.isBlank() ||
                        manifest.checksumSha256.equals(computed, ignoreCase = true)

                    if (!verified) {
                        runCatching { target.delete() }
                        onProgress(DownloadProgress.Error("Checksum mismatch"))
                        return@use
                    }

                    val record = DownloadedFileEntity(
                        manifestId = manifest.id,
                        localPath = target.absolutePath,
                        sizeBytes = copied,
                        checksumSha256 = computed,
                        downloadedAt = System.currentTimeMillis(),
                        verified = verified,
                    )
                    dao.upsert(record)
                    onProgress(DownloadProgress.Done(record))
                }
            } catch (t: Throwable) {
                onProgress(DownloadProgress.Error(t.message ?: t::class.java.simpleName))
            }
        }
    }
}
