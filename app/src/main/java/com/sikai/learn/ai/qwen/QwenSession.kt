package com.sikai.learn.ai.qwen

import okhttp3.Headers.Companion.headersOf
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

/**
 * Holds per-session state for the Qwen public API: midtoken, cookies, identity.
 * Lives for the lifetime of one [QwenProvider] instance and is refreshed on
 * 403/429/captcha responses. This is the Kotlin analogue of Flashy's
 * `prepare_cookies` + `get_midtoken` helpers.
 */
internal class QwenSession(
    private val client: OkHttpClient,
) {
    val identity: BrowserIdentity = QwenIdentities.random()
    val cookieJar: MutableMap<String, String> = mutableMapOf()
    val midToken: AtomicReference<String?> = AtomicReference(null)
    private var midTokenUses: Int = 0

    /** Generate a synthetic browser cookie set so the first request looks plausible. */
    fun seedSyntheticCookies() {
        cookieJar.clear()
        cookieJar["acw_tc"] = randomHex(40)
        cookieJar["xlly_s"] = "1"
        cookieJar["cna"] = randomBase64ish(28)
        cookieJar["_bl_uid"] = randomBase64ish(20)
        cookieJar["x-ap"] = "ap-southeast-1"
        cookieJar["sca"] = randomHex(8)
        cookieJar["isg"] = randomHex(40)
    }

    /** Fetch a midtoken from sg-wum.alibaba.com (matches Flashy's behaviour). */
    fun refreshMidTokenSync(): String? {
        if (midToken.get() != null && midTokenUses < 50) {
            midTokenUses += 1
            return midToken.get()
        }
        val req = Request.Builder()
            .url("https://sg-wum.alibaba.com/w/wu.json")
            .header("User-Agent", identity.userAgent)
            .header("Accept", "*/*")
            .build()
        return runCatching {
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                val body = resp.body?.string().orEmpty()
                val match = Regex("(?:umx\\.wu|__fycb)\\('([^']+)'\\)").find(body)
                match?.groupValues?.get(1)?.also {
                    midToken.set(it)
                    midTokenUses = 1
                }
            }
        }.getOrNull()
    }

    fun cookieHeader(): String = cookieJar.entries.joinToString("; ") { "${it.key}=${it.value}" }

    fun mergeFromSetCookies(setCookieHeaders: List<String>) {
        for (raw in setCookieHeaders) {
            val parts = raw.split(";")
            val first = parts.firstOrNull() ?: continue
            val eq = first.indexOf('=')
            if (eq <= 0) continue
            val k = first.substring(0, eq).trim()
            val v = first.substring(eq + 1).trim()
            if (k.isNotBlank()) cookieJar[k] = v
        }
    }

    fun reset() {
        cookieJar.clear()
        midToken.set(null)
        midTokenUses = 0
    }

    private fun randomHex(len: Int): String =
        UUID.randomUUID().toString().replace("-", "").take(len).padEnd(len, 'a')

    private fun randomBase64ish(len: Int): String =
        UUID.randomUUID().toString().replace("-", "").take(len)
}
