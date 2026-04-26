package com.sikai.learn.data.worker

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.sikai.learn.data.local.AppDatabase
import com.sikai.learn.data.repository.ContentRepositoryImpl
import com.sikai.learn.domain.content.ContentManifest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

class DownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val payload = inputData.getString(KEY_MANIFEST_ITEM) ?: return Result.failure(workDataOf("reason" to "Missing manifest item"))
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        val item = runCatching { json.decodeFromString<ContentManifest>(payload) }.getOrElse {
            return Result.failure(workDataOf("reason" to "Invalid manifest item"))
        }
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "sikai.db").fallbackToDestructiveMigration().build()
        val repository = ContentRepositoryImpl(applicationContext, db.contentDao(), OkHttpClient(), json)
        return repository.download(item).fold(
            onSuccess = { Result.success(workDataOf("path" to it)) },
            onFailure = { if (runAttemptCount < 3) Result.retry() else Result.failure(workDataOf("reason" to (it.message ?: "Download failed"))) }
        )
    }

    companion object {
        const val KEY_MANIFEST_ITEM = "manifest_item_json"
    }
}
