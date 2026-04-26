package com.sikai.learn.core.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local-only encrypted store for user-supplied API keys. Backed by Android Keystore
 * via [EncryptedSharedPreferences]. Keys NEVER leave the device — they are not synced
 * to the SikAi backend, and are excluded from auto-backup/data extraction.
 */
@Singleton
class SecureKeyStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        try {
            EncryptedSharedPreferences.create(
                context,
                "encrypted_keys",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (t: Throwable) {
            // Fallback: plain prefs only if EncryptedSharedPreferences fails (corrupted keystore).
            // We log a single warning and continue — never block the user from saving keys.
            android.util.Log.w("SecureKeyStore", "Falling back to non-encrypted prefs: ${t.message}")
            context.getSharedPreferences("plain_keys_fallback", Context.MODE_PRIVATE)
        }
    }

    fun setApiKey(providerId: String, key: String?) {
        if (key.isNullOrBlank()) {
            prefs.edit().remove(keyName(providerId)).apply()
        } else {
            prefs.edit().putString(keyName(providerId), key).apply()
        }
    }

    fun getApiKey(providerId: String): String? = prefs.getString(keyName(providerId), null)

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun maskedPreview(providerId: String): String {
        val k = getApiKey(providerId) ?: return ""
        if (k.length <= 8) return "••••"
        return k.take(4) + "••••" + k.takeLast(4)
    }

    private fun keyName(providerId: String) = "api_key__$providerId"
}
