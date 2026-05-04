package com.sikai.learn.ai.qwen

import android.content.Context
import android.net.Uri
import com.sikai.learn.domain.model.AiAttachment
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

internal object QwenWorkerUpload {

    fun upload(
        context: Context,
        client: OkHttpClient,
        backendUrl: String,
        attachment: AiAttachment,
    ): JsonObject? {
        val bytes = runCatching {
            context.contentResolver.openInputStream(Uri.parse(attachment.uri))!!.use { it.readBytes() }
        }.getOrNull() ?: return null

        // Call the worker's upload endpoint
        val boundary = "----SikAiUpload${UUID.randomUUID()}"
        val multipartBody = buildMultipart(boundary, attachment.displayName, attachment.mimeType, bytes)

        val req = Request.Builder()
            .url("$backendUrl/v1/qwen/upload")
            .post(multipartBody.toRequestBody("multipart/form-data; boundary=$boundary".toMediaType()))
            .build()

        return try {
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val text = resp.body?.string().orEmpty()
                runCatching { kotlinx.serialization.json.Json.parseToJsonElement(text) as? JsonObject }.getOrNull()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun buildMultipart(boundary: String, filename: String, mimeType: String, bytes: ByteArray): ByteArray {
        val sb = StringBuilder()
        sb.append("--$boundary\r\n")
        sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"$filename\"\r\n")
        sb.append("Content-Type: $mimeType\r\n\r\n")
        val headerBytes = sb.toString().toByteArray(Charsets.UTF_8)
        val footerBytes = "\r\n--$boundary--\r\n".toByteArray(Charsets.UTF_8)
        return headerBytes + bytes + footerBytes
    }
}