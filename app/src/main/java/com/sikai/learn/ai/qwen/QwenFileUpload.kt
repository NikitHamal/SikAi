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
import okhttp3.RequestBody
import okio.BufferedSink
import java.security.MessageDigest
import java.util.UUID

/**
 * Uploads a single attachment to Qwen's STS-backed Aliyun OSS bucket and
 * returns the file descriptor object Qwen expects in chat payloads. This is a
 * direct port of Flashy's `qwen_utils/file_upload.py` minus the OSS4-HMAC
 * signing — Qwen's STS endpoint hands back a signed URL that already permits
 * a direct PUT, so a raw upload with the returned headers is enough.
 */
internal object QwenFileUpload {

    fun upload(
        context: Context,
        client: OkHttpClient,
        session: QwenSession,
        attachment: AiAttachment,
    ): JsonObject? {
        val bytes = runCatching {
            context.contentResolver.openInputStream(Uri.parse(attachment.uri))!!.use { it.readBytes() }
        }.getOrNull() ?: return null
        val mime = attachment.mimeType.ifBlank { "application/octet-stream" }
        val (fileType, showType, fileClass) = classify(attachment.displayName, mime)

        val stsBody = buildJsonObject {
            put("filename", attachment.displayName)
            put("filesize", bytes.size)
            put("filetype", mime)
        }.toString().toRequestBody(JSON)

        val stsReq = Request.Builder()
            .url("https://chat.qwen.ai/api/v2/files/getstsToken")
            .post(stsBody)
            .also { addBaseHeaders(it, session) }
            .build()

        val stsObj = client.newCall(stsReq).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val text = resp.body?.string().orEmpty()
            val parsed = runCatching { kotlinx.serialization.json.Json.parseToJsonElement(text) as? JsonObject }
                .getOrNull() ?: return null
            val ok = (parsed["success"] as? JsonPrimitive)?.content?.toBoolean() ?: false
            if (!ok) return null
            parsed["data"] as? JsonObject ?: return null
        }

        val fileUrl = (stsObj["file_url"] as? JsonPrimitive)?.content ?: return null
        val fileId = (stsObj["file_id"] as? JsonPrimitive)?.content ?: return null

        val putReq = Request.Builder()
            .url(fileUrl)
            .put(rawBody(bytes, mime))
            .header("Content-Type", mime)
            .build()
        val ok = runCatching {
            client.newCall(putReq).execute().use { it.isSuccessful || it.code == 204 }
        }.getOrDefault(false)
        if (!ok) return null

        val now = System.currentTimeMillis()
        return buildJsonObject {
            put("type", fileType)
            putJsonObject("file") {
                put("created_at", now)
                putJsonObject("data") { /* empty */ }
                put("filename", attachment.displayName)
                put("hash", JsonPrimitive(null as String?))
                put("id", fileId)
                putJsonObject("meta") {
                    put("name", attachment.displayName)
                    put("size", bytes.size)
                    put("content_type", mime)
                }
                put("update_at", now)
            }
            put("id", fileId)
            put("url", fileUrl)
            put("name", attachment.displayName)
            put("collection_name", "")
            put("progress", 0)
            put("status", "uploaded")
            put("greenNet", "success")
            put("size", bytes.size)
            put("error", "")
            put("itemId", UUID.randomUUID().toString())
            put("file_type", mime)
            put("showType", showType)
            put("file_class", fileClass)
            put("uploadTaskId", UUID.randomUUID().toString())
        }
    }

    private fun classify(filename: String, mime: String): Triple<String, String, String> {
        val ext = filename.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "png", "jpg", "jpeg", "gif", "webp", "bmp", "svg" -> Triple("image", "image", "vision")
            "mp4", "avi", "mov", "mkv", "webm" -> Triple("video", "video", "video")
            "mp3", "wav", "ogg", "flac", "aac", "m4a" -> Triple("audio", "audio", "audio")
            else -> Triple(mime, "file", "document")
        }
    }

    private fun addBaseHeaders(b: Request.Builder, session: QwenSession) {
        val ident = session.identity
        b.header("Accept", "*/*")
        b.header("Origin", "https://chat.qwen.ai")
        b.header("Referer", "https://chat.qwen.ai/")
        b.header("User-Agent", ident.userAgent)
        b.header("sec-ch-ua", ident.secChUa)
        b.header("sec-ch-ua-mobile", ident.secChUaMobile)
        b.header("sec-ch-ua-platform", ident.secChUaPlatform)
        val cookies = session.cookieHeader()
        if (cookies.isNotEmpty()) b.header("Cookie", cookies)
        session.midToken.get()?.let {
            b.header("bx-umidtoken", it)
            b.header("bx-v", "2.5.31")
        }
    }

    private fun rawBody(bytes: ByteArray, mime: String): RequestBody = object : RequestBody() {
        override fun contentType() = mime.toMediaType()
        override fun contentLength(): Long = bytes.size.toLong()
        override fun writeTo(sink: BufferedSink) { sink.write(bytes) }
    }

    private val JSON = "application/json".toMediaType()
    private fun sha256(b: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(b).joinToString("") { "%02x".format(it) }
}
