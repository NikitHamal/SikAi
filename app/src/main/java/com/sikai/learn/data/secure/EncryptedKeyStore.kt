package com.sikai.learn.data.secure

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android-Keystore backed key/value store for sensitive material — specifically,
 * user-supplied AI provider API keys. Values never leave the device and are
 * never logged. Keys are referenced by alias from [AiProviderConfig].
 */
@Singleton
class EncryptedKeyStore @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun put(alias: String, value: String) {
        prefs.edit().putString(alias, value).apply()
    }

    fun get(alias: String): String? = prefs.getString(alias, null)

    fun remove(alias: String) {
        prefs.edit().remove(alias).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun has(alias: String): Boolean = prefs.contains(alias)

    /** Returns a masked preview suitable for UI: shows first 3 + last 4 characters. */
    fun masked(alias: String): String? {
        val v = get(alias) ?: return null
        if (v.length <= 8) return "•".repeat(v.length)
        return v.take(3) + "•".repeat((v.length - 7).coerceAtLeast(3)) + v.takeLast(4)
    }

    companion object {
        const val FILE_NAME = "sikai_secure_keys"
    }
}
