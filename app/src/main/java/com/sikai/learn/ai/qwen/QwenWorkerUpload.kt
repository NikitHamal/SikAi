package com.sikai.learn.ai.qwen

import android.content.Context
import android.net.Uri
import android.util.Log
import com.sikai.learn.domain.model.AiAttachment
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

internal object QwenWorkerUpload {

    private const val TAG = "QwenUpload"

    fun upload(
        context: Context,
        client: OkHttpClient,
        backendUrl: String,
        attachment: AiAttachment,
    ): JsonObject? {
        val bytes = runCatching {
            context.contentResolver.openInputStream(Uri.parse(attachment.uri))?.use { it.readBytes() }
        }.getOrNull()
        if (bytes == null) {
            Log.e(TAG, "Failed to read file: ${attachment.displayName} from ${attachment.uri}")
            return null
        }

        val mimeType = attachment.mimeType.toMediaType()
        val fileBody = bytes.toRequestBody(mimeType)

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", attachment.displayName, fileBody)
            .build()

        val req = Request.Builder()
            .url("$backendUrl/v1/qwen/upload")
            .post(multipartBody)
            .build()

        return try {
            client.newCall(req).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    Log.e(TAG, "Upload failed HTTP ${resp.code}: ${text.take(300)}")
                    return null
                }
                val parsed = runCatching {
                    kotlinx.serialization.json.Json.parseToJsonElement(text) as? JsonObject
                }.getOrNull()
                if (parsed == null) {
                    Log.e(TAG, "Upload response not JSON: ${text.take(300)}")
                }
                parsed
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload exception: ${e.message}", e)
            null
        }
    }
}