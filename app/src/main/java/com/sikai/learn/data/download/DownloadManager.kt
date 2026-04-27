package com.sikai.learn.data.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.sikai.learn.data.local.DownloadedFileDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin facade over [WorkManager] for queueing/cancelling content downloads.
 * Anything that touches the actual filesystem lives in [DownloadWorker].
 */
@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadedDao: DownloadedFileDao,
) {

    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    fun enqueue(manifestId: String, requireUnmetered: Boolean = false) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (requireUnmetered) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(
                Data.Builder()
                    .putString(DownloadWorker.KEY_MANIFEST_ID, manifestId)
                    .build()
            )
            .addTag(workTag(manifestId))
            .build()
        workManager.enqueueUniqueWork(
            workTag(manifestId),
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun cancel(manifestId: String) {
        workManager.cancelUniqueWork(workTag(manifestId))
    }

    fun observe(manifestId: String): Flow<DownloadProgress> =
        workManager.getWorkInfosForUniqueWorkFlow(workTag(manifestId)).map { infos ->
            val info = infos.firstOrNull() ?: return@map DownloadProgress.Idle
            when (info.state) {
                WorkInfo.State.ENQUEUED -> DownloadProgress.Queued
                WorkInfo.State.RUNNING -> DownloadProgress.Running
                WorkInfo.State.SUCCEEDED -> DownloadProgress.Completed
                WorkInfo.State.FAILED -> DownloadProgress.Failed
                WorkInfo.State.CANCELLED -> DownloadProgress.Cancelled
                WorkInfo.State.BLOCKED -> DownloadProgress.Queued
            }
        }

    suspend fun isDownloaded(manifestId: String): Boolean =
        downloadedDao.byManifestId(manifestId) != null

    private fun workTag(manifestId: String) = "download/$manifestId"
}

enum class DownloadProgress { Idle, Queued, Running, Completed, Failed, Cancelled }
