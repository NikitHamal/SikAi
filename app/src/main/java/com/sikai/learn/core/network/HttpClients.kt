package com.sikai.learn.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object HttpClients {
    /**
     * Long-timeout client for AI streaming/generation. Reads can run for a couple of
     * minutes when a model is "thinking".
     */
    fun aiClient(): OkHttpClient = baseClient(connectTimeoutSec = 20, readTimeoutSec = 180, writeTimeoutSec = 60)

    /** Short-timeout client for the SikAi backend (manifest, file metadata). */
    fun contentClient(): OkHttpClient = baseClient(connectTimeoutSec = 10, readTimeoutSec = 30, writeTimeoutSec = 30)

    /** Download client tuned for big files. */
    fun downloadClient(): OkHttpClient = baseClient(connectTimeoutSec = 15, readTimeoutSec = 600, writeTimeoutSec = 60)

    private fun baseClient(connectTimeoutSec: Long, readTimeoutSec: Long, writeTimeoutSec: Long): OkHttpClient {
        // Logging interceptor: BASIC level — never logs request bodies (so API keys
        // submitted in headers/body never reach logs).
        val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        return OkHttpClient.Builder()
            .connectTimeout(connectTimeoutSec, TimeUnit.SECONDS)
            .readTimeout(readTimeoutSec, TimeUnit.SECONDS)
            .writeTimeout(writeTimeoutSec, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(log)
            .build()
    }
}
