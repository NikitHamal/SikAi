package com.sikai.learn.ai.qwen

import java.security.MessageDigest
import java.util.Random
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal object QwenFingerprint {

    private const val CUSTOM_BASE64 = "DGi0YA7BemWnQjCl4_bR3f8SKIF9tUz/xhr2oEOgPpac=61ZqwTudLkM5vHyNXsVJ"
    private const val BX_UA_VERSION = "231"

    private val PLATFORM_PRESETS = mapOf(
        "win64" to mapOf(
            "platform" to "Win32",
            "webglRenderer" to "ANGLE (NVIDIA, NVIDIA GeForce RTX 3080 Direct3D11 vs_5_0 ps_5_0, D3D11)|Google Inc. (NVIDIA)",
            "vendor" to "Google Inc.",
        ),
        "macIntel" to mapOf(
            "platform" to "MacIntel",
            "webglRenderer" to "ANGLE (Apple, ANGLE Metal Renderer: Apple M4, Unspecified Version)|Google Inc. (Apple)",
            "vendor" to "Google Inc.",
        ),
    )

    private val SCREEN_PRESETS = mapOf(
        "1920x1080" to "1920|1080|283|1080|158|0|1920|1080|1920|922|0|0",
        "1536x864" to "1536|864|283|864|158|0|1536|864|1536|706|0|0",
    )

    private val rng = Random()

    fun generate(): String {
        val plat = PLATFORM_PRESETS["win64"]!!
        val screen = SCREEN_PRESETS["1920x1080"]!!
        val deviceId = randomHex(20)
        val ts = System.currentTimeMillis()
        val fields = listOf(
            deviceId,
            "websdk-2.3.15d",
            ts.toString(),
            "91",
            "1|15",
            "en-US",
            "480",
            "16705151|12791",
            screen,
            "5",
            plat["platform"]!!,
            "10",
            plat["webglRenderer"]!!,
            "30|30",
            "0",
            "28",
            "5|${randomHash()}",
            randomHash().toString(),
            randomHash().toString(),
            "1", "0", "1", "0",
            "P",
            "0", "0", "0",
            "416",
            plat["vendor"]!!,
            "8",
            "-1|0|0|0|0",
            randomHash().toString(),
            "11",
            ts.toString(),
            randomHash().toString(),
            "0",
            randomInt(10, 100).toString(),
        )
        return fields.joinToString("^")
    }

    fun generateCookies(fingerprint: String): Map<String, String> {
        val fields = fingerprint.split("^")
        val processed = fields.toMutableList()
        processed[17] = randomHash().toString()
        processed[18] = randomHash().toString()
        processed[31] = randomHash().toString()
        processed[34] = randomHash().toString()
        processed[36] = randomInt(10, 100).toString()
        processed[33] = System.currentTimeMillis().toString()

        val ssxmodData = processed.joinToString("^")
        val ssxmod = "1-" + lzwEncode(ssxmodData)

        val ssxmod2Data = listOf(
            processed[0], processed[1], processed[23],
            "0", "", "0", "", "", "0", "0", "0",
            processed[32], processed[33],
            "0", "0", "0", "0", "0"
        ).joinToString("^")
        val ssxmod2 = "1-" + lzwEncode(ssxmod2Data)

        return mapOf(
            "ssxmod_itna" to ssxmod,
            "ssxmod_itna2" to ssxmod2,
        )
    }

    fun generateBxUa(fingerprint: String): String {
        val ts = System.currentTimeMillis()
        val rnd = randomInt(1000, 9999)
        val fields = fingerprint.split("^")
        val payload = mapOf(
            "v" to BX_UA_VERSION,
            "ts" to ts,
            "fp" to fingerprint,
            "d" to mapOf(
                "deviceId" to fields.getOrElse(0) { "" },
                "sdkVer" to fields.getOrElse(1) { "" },
                "lang" to fields.getOrElse(5) { "" },
                "tz" to fields.getOrElse(6) { "" },
                "platform" to fields.getOrElse(10) { "" },
                "renderer" to fields.getOrElse(12) { "" },
                "mode" to fields.getOrElse(23) { "" },
                "vendor" to fields.getOrElse(28) { "" },
            ),
            "rnd" to rnd,
            "seq" to 1,
        )
        val checksumStr = "$fingerprint$ts$rnd"
        val cs = md5(checksumStr).take(8)
        val fullPayload = payload + ("cs" to cs)
        val json = toJsonStr(fullPayload)

        val seed = fingerprint.toByteArray(Charsets.UTF_8)
        val digest = sha256(seed)
        val key = digest.copyOfRange(0, 16)
        val iv = digest.copyOfRange(16, 32)
        val encrypted = aesCbcEncrypt(json.toByteArray(Charsets.UTF_8), key, iv)
        val b64 = base64Encode(encrypted)
        return "$BX_UA_VERSION!$b64"
    }

    private fun lzwEncode(data: String): String {
        if (data.isEmpty()) return ""
        val dictionary = mutableMapOf<String, Int>()
        val dictToCreate = mutableSetOf<String>()
        var w = ""
        val result = mutableListOf<Char>()
        var value = 0
        var position = 0
        var dictSize = 3
        var numBits = 2
        var enlargeIn = 2

        fun pushBits(code: Int) {
            for (i in 0 until numBits) {
                value = (value shl 1) or ((code shr i) and 1)
                position++
                if (position == 6) {
                    result.add(CUSTOM_BASE64[value])
                    value = 0
                    position = 0
                }
            }
        }

        fun pushRawChar(ch: Int) {
            val isSmall = ch < 256
            if (isSmall) {
                for (i in 0 until numBits) { value = value shl 1; position++; if (position == 6) { result.add(CUSTOM_BASE64[value]); value = 0; position = 0 } }
                pushBits(ch and 0xFF)
            } else {
                for (i in 0 until numBits) { value = (value shl 1) or 1; position++; if (position == 6) { result.add(CUSTOM_BASE64[value]); value = 0; position = 0 } }
                val hi = (ch shr 8) and 0xFF
                val lo = ch and 0xFF
                pushBits(lo)
                pushBits(hi)
            }
        }

        for (c in data) {
            val wc = w + c
            if (wc in dictionary) {
                w = wc
            } else {
                if (w in dictToCreate) {
                    pushBits(0)
                    enlargeIn--
                    if (enlargeIn == 0) { enlargeIn = 1 shl numBits; numBits++ }
                    pushRawChar(w[0].code)
                    dictToCreate.remove(w)
                } else {
                    pushBits(dictionary[w]!!)
                }
                enlargeIn--
                if (enlargeIn == 0) { enlargeIn = 1 shl numBits; numBits++ }
                dictionary[wc] = dictSize++
                dictToCreate.add(wc)
                w = c.toString()
            }
        }

        if (w.isNotEmpty()) {
            if (w in dictToCreate) {
                pushBits(0)
                enlargeIn--
                if (enlargeIn == 0) { enlargeIn = 1 shl numBits; numBits++ }
                pushRawChar(w[0].code)
            } else {
                pushBits(dictionary[w]!!)
            }
        }

        pushBits(2)
        while (position > 0) {
            value = value shl 1
            position++
            if (position == 6) {
                result.add(CUSTOM_BASE64[value])
                value = 0
                position = 0
                break
            }
        }

        return result.joinToString("")
    }

    private fun aesCbcEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        return cipher.doFinal(data)
    }

    private fun base64Encode(data: ByteArray): String =
        java.util.Base64.getEncoder().encodeToString(data)

    private fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest(input.toByteArray())
        val sb = StringBuilder()
        for (b in bytes) sb.append("%02x".format(b))
        return sb.toString()
    }

    private fun sha256(data: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(data)

    private fun randomHex(len: Int): String {
        val bytes = ByteArray(len)
        rng.nextBytes(bytes)
        val sb = StringBuilder()
        for (b in bytes) sb.append("%02x".format(b))
        return sb.toString().take(len)
    }

    private fun randomHash(): Int = rng.nextInt(Int.MAX_VALUE)

    private fun randomInt(min: Int, max: Int): Int = rng.nextInt(min, max + 1)

    private fun toJsonStr(map: Map<String, Any?>): String {
        val sb = StringBuilder("{")
        var first = true
        for ((k, v) in map) {
            if (!first) sb.append(",")
            first = false
            sb.append("\"$k\":")
            when (v) {
                is String -> sb.append("\"${v.replace("\"", "\\\"")}\"")
                is Int -> sb.append(v)
                is Long -> sb.append(v)
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    sb.append(toJsonStr(v as Map<String, Any?>))
                }
                null -> sb.append("null")
                else -> sb.append("\"${v.toString().replace("\"", "\\\"")}\"")
            }
        }
        sb.append("}")
        return sb.toString()
    }
}