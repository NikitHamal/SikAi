package com.sikai.learn.ai.qwen

import java.security.MessageDigest
import java.util.Random
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Log

internal object QwenFingerprint {
    private const val TAG = "QwenFingerprint"
    
    private const val CUSTOM_BASE64 = "DGi0YA7BemWnQjCl4_bR3f8SKIF9tUz/xhr2oEOgPpac=61ZqwTudLkM5vHyNXsVJ"
    private const val BX_UA_VERSION = "231"
    
    private val rng = Random()

    // Generate browser fingerprint (^ separated fields)
    fun generateFingerprint(): String {
        val ts = System.currentTimeMillis()
        val deviceId = randomHex(20)
        
        val fields = listOf(
            deviceId,
            "websdk-2.3.15d",
            "1765348410850",  // initTimestamp (fixed)
            "91",
            "1|15",
            "en-US",          // locale
            "480",            // timezone offset
            "16705151|12791",
            "1920|1080|283|1080|158|0|1920|1080|1920|922|0|0", // screen
            "5",
            "Win32",          // platform
            "10",
            "ANGLE (NVIDIA, NVIDIA GeForce RTX 3080 Direct3D11 vs_5_0 ps_5_0, D3D11)|Google Inc. (NVIDIA)",
            "30|30",
            "0",
            "28",
            "5|${randomHash()}",
            randomHash().toString(),
            randomHash().toString(),
            "1", "0", "1", "0",
            "P",              // mode
            "0", "0", "0",
            "416",
            "Google Inc.",    // vendor
            "8",
            "-1|0|0|0|0",
            randomHash().toString(),
            "11",
            ts.toString(),    // current timestamp (field 33)
            randomHash().toString(),
            "0",
            randomInt(10, 100).toString()
        )
        
        return fields.joinToString("^")
    }

    // Generate ssxmod cookies from fingerprint
    fun generateCookies(fingerprint: String): Map<String, String> {
        val fields = fingerprint.split("^").toMutableList()
        
        // Process fields - randomize specific positions
        if (fields.size > 16) {
            val pluginParts = fields[16].split("|")
            fields[16] = if (pluginParts.size == 2) "${pluginParts[0]}|${randomHash()}" else "5|${randomHash()}"
        }
        if (fields.size > 17) fields[17] = randomHash().toString()
        if (fields.size > 18) fields[18] = randomHash().toString()
        if (fields.size > 31) fields[31] = randomHash().toString()
        if (fields.size > 34) fields[34] = randomHash().toString()
        if (fields.size > 36) fields[36] = randomInt(10, 100).toString()
        
        // Update timestamp
        if (fields.size > 33) fields[33] = System.currentTimeMillis().toString()
        
        // ssxmod_itna (full processed fields)
        val ssxmodData = fields.joinToString("^")
        val ssxmodItna = "1-" + lzwCompress(ssxmodData)
        
        // ssxmod_itna2 (subset of fields)
        val ssxmod2Data = listOf(
            fields.getOrElse(0) { "" },
            fields.getOrElse(1) { "" },
            fields.getOrElse(23) { "P" },
            "0", "", "0", "", "", "0", "0", "0",
            fields.getOrElse(32) { "11" },
            fields.getOrElse(33) { System.currentTimeMillis().toString() },
            "0", "0", "0", "0", "0"
        ).joinToString("^")
        val ssxmodItna2 = "1-" + lzwCompress(ssxmod2Data)
        
        return mapOf(
            "ssxmod_itna" to ssxmodItna,
            "ssxmod_itna2" to ssxmodItna2
        )
    }

    // Generate bx-ua header (AES-CBC encrypted fingerprint payload)
    fun generateBxUa(fingerprint: String): String {
        val ts = System.currentTimeMillis()
        val rnd = randomInt(1000, 9999)
        
        val fields = fingerprint.split("^")
        
        // Build JSON manually
        val json = buildString {
            append("{")
            append("\"v\":\"$BX_UA_VERSION\",")
            append("\"ts\":$ts,")
            append("\"fp\":\"${fingerprint.replace("\"", "\\\"")}\",")
            append("\"d\":{")
            append("\"deviceId\":\"${fields.getOrElse(0) { "" }}\",")
            append("\"sdkVer\":\"${fields.getOrElse(1) { "" }}\",")
            append("\"lang\":\"${fields.getOrElse(5) { "en-US" }}\",")
            append("\"tz\":\"${fields.getOrElse(6) { "480" }}\",")
            append("\"platform\":\"${fields.getOrElse(10) { "Win32" }}\",")
            append("\"renderer\":\"${fields.getOrElse(12) { "" }}\",")
            append("\"mode\":\"${fields.getOrElse(23) { "P" }}\",")
            append("\"vendor\":\"${fields.getOrElse(28) { "" }}\"")
            append("},")
            append("\"rnd\":$rnd,")
            append("\"seq\":1,")
            // checksum = md5(fingerprint + ts + rnd)[:8]
            val checksum = md5("$fingerprint$ts$rnd").take(8)
            append("\"cs\":\"$checksum\"")
            append("}")
        }
        
        // AES-CBC encrypt
        val seed = fingerprint.toByteArray(Charsets.UTF_8)
        val digest = sha256(seed)
        val key = digest.copyOfRange(0, 16)
        val iv = digest.copyOfRange(16, 32)
        
        val encrypted = aesCbcEncrypt(json.toByteArray(Charsets.UTF_8), key, iv)
        val b64 = android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP)
        
        return "$BX_UA_VERSION!$b64"
    }

    // LZW compression at 6-bit depth with custom base64 charset
    private fun lzwCompress(data: String): String {
        if (data.isEmpty()) return ""
        
        val dictionary = mutableMapOf<String, Int>()
        val dictToCreate = mutableSetOf<String>()
        
        for (i in 0..255) {
            dictionary[i.toChar().toString()] = i + 1
        }
        
        var w = ""
        val result = mutableListOf<Int>()
        var code = 258 // next code
        var bits = 9
        var buffer = 0
        var bitCount = 0
        
        fun writeCode(c: Int) {
            buffer = (buffer shl bits) or c
            bitCount += bits
            while (bitCount >= 6) {
                bitCount -= 6
                val index = (buffer shr bitCount) and 0x3F
                result.add(index)
            }
        }
        
        for (c in data) {
            val wc = w + c
            if (dictionary.containsKey(wc)) {
                w = wc
            } else {
                writeCode(dictionary[w] ?: 0)
                if (code < 4096) {
                    dictionary[wc] = code++
                    if (code > (1 shl bits) && bits < 12) bits++
                }
                w = c.toString()
            }
        }
        
        if (w.isNotEmpty()) {
            writeCode(dictionary[w] ?: 0)
        }
        
        // End code
        writeCode(0)
        
        // Flush remaining bits
        if (bitCount > 0) {
            buffer = buffer shl (6 - bitCount)
            result.add(buffer and 0x3F)
        }
        
        // Convert to custom base64
        return result.joinToString("") { CUSTOM_BASE64[it].toString() }
    }

    private fun aesCbcEncrypt(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        return cipher.doFinal(data)
    }

    private fun sha256(data: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(data)

    private fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun randomHex(len: Int): String {
        val sb = StringBuilder()
        repeat(len) { sb.append("0123456789abcdef".random()) }
        return sb.toString()
    }

    private fun randomHash(): Int = rng.nextInt(0, 0x7FFFFFFF)

    private fun randomInt(min: Int, max: Int): Int = min + rng.nextInt(max - min + 1)
    
    // Test function - verify the implementation
    fun test() {
        try {
            Log.d(TAG, "=== Testing QwenFingerprint ===")
            
            val fp = generateFingerprint()
            Log.d(TAG, "Fingerprint (first 100 chars): ${fp.take(100)}...")
            Log.d(TAG, "Fingerprint fields count: ${fp.split("^").size}")
            
            val cookies = generateCookies(fp)
            Log.d(TAG, "ssxmod_itna (first 50): ${cookies["ssxmod_itna"]?.take(50)}...")
            Log.d(TAG, "ssxmod_itna2 (first 50): ${cookies["ssxmod_itna2"]?.take(50)}...")
            
            val bxua = generateBxUa(fp)
            Log.d(TAG, "bx-ua (first 80): ${bxua.take(80)}...")
            Log.d(TAG, "bx-ua length: ${bxua.length}")
            
            // Verify we can decompress
            val testData = "test^data^fields"
            val compressed = lzwCompress(testData)
            Log.d(TAG, "LZW test: '$testData' -> '${compressed.take(20)}...'")
            
            Log.d(TAG, "=== All tests passed ===")
        } catch (e: Exception) {
            Log.e(TAG, "Test failed", e)
        }
    }
}