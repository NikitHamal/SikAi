package com.sikai.learn.domain.ai

interface AiProvider {
    val config: AiProviderConfig
    suspend fun complete(request: AiRequest, apiKey: String?): AiProviderResult
    suspend fun uploadAttachment(attachment: AiAttachment, apiKey: String?): Result<AiAttachment> = Result.success(attachment)
    suspend fun listModels(apiKey: String?): Result<List<AiModel>> = Result.success(emptyList())
    suspend fun health(apiKey: String?): ProviderHealthState = ProviderHealthState.UNKNOWN
}

interface AiRepository {
    suspend fun complete(request: AiRequest): AiProviderResult
    suspend fun testProvider(config: AiProviderConfig, apiKey: String?): ProviderHealthState
    suspend fun listModels(config: AiProviderConfig, apiKey: String?): List<AiModel>
}
