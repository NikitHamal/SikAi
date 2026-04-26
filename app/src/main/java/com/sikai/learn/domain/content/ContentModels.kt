package com.sikai.learn.domain.content

import kotlinx.serialization.Serializable

@Serializable
data class ContentManifest(
    val id: String,
    val title: String,
    val type: String,
    val classLevel: Int,
    val subject: String,
    val year: Int? = null,
    val fileUrl: String? = null,
    val fileKey: String? = null,
    val sizeBytes: Long,
    val checksumSha256: String,
    val version: Int,
    val updatedAt: String,
    val language: String,
    val tags: List<String> = emptyList()
)

interface ContentRepository {
    suspend fun refreshManifest(): Result<List<ContentManifest>>
    suspend fun download(item: ContentManifest): Result<String>
    suspend fun deleteDownload(id: String): Result<Unit>
}
