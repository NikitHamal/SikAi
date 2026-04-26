package com.sikai.learn.data.remote

import com.sikai.learn.domain.model.ContentManifest
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Cloudflare Worker backend. Hosts only the content manifest and signed file
 * download URLs — never any user data, never any API keys. Defined as a thin
 * Retrofit interface so we can swap base URLs per build flavor.
 */
interface BackendApi {

    @GET("manifest")
    suspend fun manifest(): ContentManifest

    @GET("manifest/class/{classLevel}")
    suspend fun manifestForClass(@Path("classLevel") classLevel: Int): ContentManifest

    @GET("health")
    suspend fun health(): HealthResponse
}

@kotlinx.serialization.Serializable
data class HealthResponse(
    val ok: Boolean = true,
    val version: Int = 1,
)
