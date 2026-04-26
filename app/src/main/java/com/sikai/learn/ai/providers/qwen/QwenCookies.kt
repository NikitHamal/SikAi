package com.sikai.learn.ai.providers.qwen

import java.security.MessageDigest
import kotlin.random.Random

/**
 * Generates anonymous-visitor cookies that chat.qwen.ai sets when a fresh browser
 * lands on the page. We emulate the values closely enough that the WAF treats us
 * like a real browser session. This is the same approach Flashy uses.
 *
 * Re-rolled per session (or when a 403/captcha is observed).
 */
object QwenCookies {

    /** Stable-looking randomized cookie set with the fields Qwen WAF actually checks. */
    fun generate(): Map<String, String> {
        val now = System.currentTimeMillis()
        val rand = Random(now)
        val cnaPart = randomHex(rand, 16)
        val xlly = randomHex(rand, 32)
        val tfstkVal = (1..70).map { hexAlphabet.random(rand) }.joinToString("")
        val acwTc = (0..2).joinToString("") { randomHex(rand, 16) }

        val rawData = mapOf(
            "screenWidth" to (1280..1920).random(rand),
            "screenHeight" to (720..1080).random(rand),
            "timezone" to "Asia/Kolkata",
            "language" to "en-US",
            "userAgent" to USER_AGENT,
            "platform" to "Win32",
            "deviceMemory" to listOf(4, 8, 16).random(rand),
            "hardwareConcurrency" to listOf(4, 8, 12, 16).random(rand),
        ).entries.joinToString(separator = "|") { (k, v) -> "$k=$v" }

        return mapOf(
            "cna" to cnaPart,
            "_bl_uid" to randomHex(rand, 26),
            "x-ap" to "ap-southeast-1",
            "isg" to (1..40).map { hexAlphabet.random(rand) }.joinToString(""),
            "xlly_s" to xlly,
            "acw_tc" to acwTc,
            "tfstk" to tfstkVal,
            "ssxmod_itna" to ssxmodItna(rand),
            "rawData" to rawData,
        )
    }

    /** Imitates the giant "ssxmod_itna" cookie value. Format isn't strictly validated. */
    private fun ssxmodItna(rand: Random): String {
        val parts = (0..14).joinToString("=") {
            (1..(8..18).random(rand)).map { hexAlphabet.random(rand) }.joinToString("")
        }
        return "QqAxgi=_GqA0qjxYK0ToQAxQiK4iN20Q=05+G=DbqGXQ4eqDl=xYS=GeYY03Y$parts"
    }

    private val hexAlphabet = ('0'..'9').toList() + ('a'..'f').toList()

    private fun randomHex(rand: Random, len: Int) =
        (1..len).map { hexAlphabet.random(rand) }.joinToString("")

    /** Synthesizes a bx-ua header from rawData fingerprint. */
    fun bxUa(rawFingerprint: String): String {
        val md = MessageDigest.getInstance("SHA-256").digest(rawFingerprint.toByteArray()).toHex()
        return "v=2.5.31&t=${System.currentTimeMillis()}&s=${md.take(40)}"
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36"
}
