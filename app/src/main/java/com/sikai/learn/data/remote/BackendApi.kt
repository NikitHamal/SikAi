package com.sikai.learn.data.remote

import com.sikai.learn.domain.model.ContentManifest
import retrofit2.http.GET
import retrofit2.http.Query

interface BackendApi {

    @GET("v1/manifest")
    suspend fun manifest(
        @Query("classLevel") classLevel: Int? = null,
        @Query("subject") subject: String? = null,
    ): ContentManifest

    @GET("v1/manifest")
    suspend fun manifestForClass(
        @Query("classLevel") classLevel: Int,
    ): ContentManifest

    @GET("v1/questions")
    suspend fun questions(
        @Query("classLevel") classLevel: Int,
        @Query("subject") subject: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
    ): QuestionsResponse

    @GET("v1/subjects")
    suspend fun subjects(
        @Query("classLevel") classLevel: Int? = null,
    ): SubjectsResponse

    @GET("v1/config")
    suspend fun config(): AppConfigResponse

    @GET("health")
    suspend fun health(): HealthResponse
}

@kotlinx.serialization.Serializable
data class QuestionsResponse(
    val items: List<RemoteQuestion> = emptyList(),
)

@kotlinx.serialization.Serializable
data class RemoteQuestion(
    val id: String,
    val classLevel: Int,
    val subject: String,
    val topic: String = "general",
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String? = null,
    val source: String = "remote",
    val language: String = "en",
    val difficulty: String = "medium",
)

@kotlinx.serialization.Serializable
data class SubjectsResponse(
    val items: List<RemoteSubject> = emptyList(),
)

@kotlinx.serialization.Serializable
data class RemoteSubject(
    val id: String,
    val displayName: String,
    val classLevel: Int,
    val icon: String? = null,
    val sortOrder: Int = 0,
)

@kotlinx.serialization.Serializable
data class AppConfigResponse(
    val min_app_version: String = "1",
    val maintenance_mode: String = "false",
    val announcement_text: String = "",
    val announcement_active: String = "false",
)

@kotlinx.serialization.Serializable
data class HealthResponse(
    val ok: Boolean = true,
    val service: String = "sikai-content",
)