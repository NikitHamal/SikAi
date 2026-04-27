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
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

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
            session.mergeFromSetCookies(resp.headers("Set-Cookie"))
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

        val uploadUrl = fileUrl.substringBefore("?")
        val ossHeaders = buildOssHeaders("PUT", stsObj, mime, bytes)
        val putReq = Request.Builder()
            .url(uploadUrl)
            .put(rawBody(bytes, mime))
            .apply { ossHeaders.forEach { (k, v) -> header(k, v) } }
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

    private fun buildOssHeaders(
        method: String,
        stsObj: JsonObject,
        contentType: String,
        body: ByteArray,
    ): Map<String, String> {
        val bucketName = (stsObj["bucketname"] as? JsonPrimitive)?.content ?: "qwen-webui-prod"
        val filePath = (stsObj["file_path"] as? JsonPrimitive)?.content ?: ""
        val accessKeyId = (stsObj["access_key_id"] as? JsonPrimitive)?.content ?: return emptyMap()
        val accessKeySecret = (stsObj["access_key_secret"] as? JsonPrimitive)?.content ?: return emptyMap()
        val securityToken = (stsObj["security_token"] as? JsonPrimitive)?.content ?: ""

        val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val dateStr = sdf.format(System.currentTimeMillis())
        val datePart = dateStr.substringBefore("T")

        val contentSha256 = sha256Hex(body)
        val canonicalUri = "/$bucketName/${encodeOssPath(filePath)}"

        val headers = linkedMapOf(
            "content-type" to contentType,
            "x-oss-content-sha256" to "UNSIGNED-PAYLOAD",
            "x-oss-date" to dateStr,
            "x-oss-security-token" to securityToken,
            "x-oss-user-agent" to "aliyun-sdk-js/6.23.0 Chrome 132.0.0.0 on Windows 10 64-bit",
        )

        val signedHeaderKeys = headers.keys.sorted()
        val canonicalHeaders = signedHeaderKeys.joinToString("\n") { k ->
            "$k:${headers[k]}"
        } + "\n"
        val signedHeadersStr = signedHeaderKeys.joinToString(";")

        val canonicalRequest = "$method\n$canonicalUri\n\n$canonicalHeaders\n$signedHeadersStr\nUNSIGNED-PAYLOAD"
        val scope = "$datePart/ap-southeast-1/oss/aliyun_v4_request"
        val stringToSign = "OSS4-HMAC-SHA256\n$dateStr\n$scope\n${sha256Hex(canonicalRequest.toByteArray())}"

        val signingKey = deriveSigningKey(accessKeySecret, datePart, "ap-southeast-1", "oss")
        val signature = hmacSha256Hex(signingKey, stringToSign.toByteArray())

        val authHeader = "OSS4-HMAC-SHA256 Credential=$accessKeyId/$scope,Signature=$signature"

        return mapOf(
            "Content-Type" to contentType,
            "x-oss-content-sha256" to "UNSIGNED-PAYLOAD",
            "x-oss-date" to dateStr,
            "x-oss-security-token" to securityToken,
            "x-oss-user-agent" to "aliyun-sdk-js/6.23.0 Chrome 132.0.0.0 on Windows 10 64-bit",
            "Authorization" to authHeader,
        )
    }

    private fun deriveSigningKey(secret: String, date: String, region: String, service: String): ByteArray {
        var key = hmacSha256("aliyun_v4$secret".toByteArray(), date)
        key = hmacSha256(key, region.toByteArray())
        key = hmacSha256(key, service.toByteArray())
        key = hmacSha256(key, "aliyun_v4_request".toByteArray())
        return key
    }

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }

    private fun hmacSha256Hex(key: ByteArray, data: ByteArray): String {
        return hmacSha256(key, data).joinToString("") { "%02x".format(it) }
    }

    private fun sha256Hex(data: ByteArray): String {
        return MessageDigest.getInstance("SHA-256").digest(data).joinToString("") { "%02x".format(it) }
    }

    private fun encodeOssPath(path: String): String {
        return path.split("/").joinToString("/") { segment ->
            URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
                .replace("%7E", "~").replace("*", "%2A")
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
}