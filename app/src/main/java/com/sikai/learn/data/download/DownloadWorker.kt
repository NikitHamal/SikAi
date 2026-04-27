package com.sikai.learn.data.download

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sikai.learn.BuildConfig
import com.sikai.learn.data.local.ContentManifestDao
import com.sikai.learn.data.local.DownloadedFileDao
import com.sikai.learn.data.local.DownloadedFileEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Background download worker. Streams a manifest entry's fileUrl (or fileKey
 * relative to the backend) to local app-private storage and verifies SHA-256
 * if the manifest provided one. Idempotent — if the file already exists with
 * the matching checksum it short-circuits.
 */
@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val client: OkHttpClient,
    private val manifestDao: ContentManifestDao,
    private val downloadedDao: DownloadedFileDao,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val manifestId = inputData.getString(KEY_MANIFEST_ID)
            ?: return@withContext Result.failure()
        val entry = manifestDao.byId(manifestId) ?: return@withContext Result.failure()

        val targetDir = File(applicationContext.filesDir, "downloads").apply { mkdirs() }
        val target = File(targetDir, "${entry.id}.bin")

        // If the file already exists with the right checksum, skip the network round-trip.
        if (target.exists() && entry.checksumSha256 != null) {
            val existing = sha256(target)
            if (existing.equals(entry.checksumSha256, ignoreCase = true)) {
                downloadedDao.upsert(downloadEntry(entry.id, target, target.length(), existing))
                return@withContext Result.success()
            }
        }

        val url = entry.fileUrl
            ?: entry.fileKey?.let { resolveBackendUrl("files/$it") }
            ?: return@withContext Result.failure()

        return@withContext try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Result.retry()
                val body = response.body ?: return@withContext Result.retry()
                FileOutputStream(target).use { out ->
                    body.byteStream().copyTo(out, bufferSize = 32 * 1024)
                }
            }

            val digest = sha256(target)
            if (entry.checksumSha256 != null && !digest.equals(entry.checksumSha256, ignoreCase = true)) {
                target.delete()
                return@withContext Result.failure()
            }
            downloadedDao.upsert(downloadEntry(entry.id, target, target.length(), digest))
            Result.success()
        } catch (t: Throwable) {
            if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
        }
    }

    private fun downloadEntry(id: String, file: File, size: Long, sha: String) = DownloadedFileEntity(
        manifestId = id,
        localPath = file.absolutePath,
        sizeBytes = size,
        checksumSha256 = sha,
        downloadedAtMillis = System.currentTimeMillis(),
    )

    private fun resolveBackendUrl(path: String): String {
        val base = BuildConfig.BACKEND_BASE_URL.trimEnd('/')
        return "$base/$path"
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buf = ByteArray(32 * 1024)
            while (true) {
                val read = input.read(buf)
                if (read <= 0) break
                digest.update(buf, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val KEY_MANIFEST_ID = "manifest_id"
        private const val MAX_ATTEMPTS = 3
    }
}
