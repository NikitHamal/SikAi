package com.sikai.learn.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureApiKeyStore @Inject constructor(@ApplicationContext context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "sikai_secure_keys",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun save(alias: String, apiKey: String) {
        prefs.edit().putString(alias, apiKey).apply()
    }

    fun get(alias: String?): String? = alias?.let { prefs.getString(it, null) }?.takeIf { it.isNotBlank() }

    fun clear(alias: String) {
        prefs.edit().remove(alias).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun mask(alias: String?): String = get(alias)?.let { key ->
        if (key.length <= 8) "••••" else "${key.take(4)}••••${key.takeLast(4)}"
    } ?: "Not set"
}
