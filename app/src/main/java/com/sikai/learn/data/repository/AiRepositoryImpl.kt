package com.sikai.learn.data.repository

import com.sikai.learn.core.security.AppPreferences
import com.sikai.learn.core.security.SecureApiKeyStore
import com.sikai.learn.data.local.AiProviderDao
import com.sikai.learn.data.local.ProviderLogEntity
import com.sikai.learn.data.local.toDomain
import com.sikai.learn.domain.ai.*
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val providerDao: AiProviderDao,
    private val keys: SecureApiKeyStore,
    private val prefs: AppPreferences,
    private val promptBuilder: PromptBuilder,
    private val client: OkHttpClient
) : AiRepository {
    override suspend fun complete(request: AiRequest): AiProviderResult {
        val providers = providerDao.providers().first().map { it.toDomain() }.filter { it.enabled }.sortedBy { it.priority }
        if (providers.isEmpty()) return AiProviderResult.Failure("none", AiFailureReason.UNKNOWN, "No AI providers configured", retryable = false)
        val defaultId = request.preferredProviderId ?: prefs.flow.first().defaultProviderId
        val ordered = providers.sortedWith(compareByDescending<AiProviderConfig> { it.id == defaultId }.thenBy { it.priority })
        val compatible = ordered.filter { supportsRequest(it, request) }
        if (compatible.isEmpty()) return AiProviderResult.Failure("none", AiFailureReason.UNSUPPORTED_CAPABILITY, "No provider supports this request", retryable = false)

        val system = AiMessage(AiRole.SYSTEM, promptBuilder.systemPrompt(request.classLevel, request.subject, request.mode))
        val enriched = request.copy(messages = listOf(system) + request.messages)
        var lastFailure: AiProviderResult.Failure? = null
        for (config in compatible) {
            val provider = providerFor(config)
            val apiKey = keys.get(config.apiKeyAlias)
            val uploadPrepared = prepareAttachments(provider, enriched, apiKey)
            val result = provider.complete(uploadPrepared, apiKey)
            when (result) {
                is AiProviderResult.Success -> return result.copy(response = result.response.copy(usedFallback = config.id != defaultId))
                is AiProviderResult.Failure -> {
                    providerDao.insertLog(ProviderLogEntity(UUID.randomUUID().toString(), config.id, result.reason.name, result.message.take(500), System.currentTimeMillis()))
                    lastFailure = result
                    if (!result.retryable || result.reason == AiFailureReason.AUTH_REQUIRED) continue
                }
            }
        }
        return lastFailure ?: AiProviderResult.Failure("none", AiFailureReason.UNKNOWN, "All providers failed")
    }

    override suspend fun testProvider(config: AiProviderConfig, apiKey: String?): ProviderHealthState = providerFor(config).health(apiKey)

    override suspend fun listModels(config: AiProviderConfig, apiKey: String?): List<AiModel> = providerFor(config).listModels(apiKey).getOrDefault(emptyList())

    private suspend fun prepareAttachments(provider: AiProvider, request: AiRequest, apiKey: String?): AiRequest {
        val messages = request.messages.map { message ->
            if (message.attachments.isEmpty()) message else message.copy(attachments = message.attachments.map { attachment ->
                if (provider.config.capabilities.contains(AiCapability.FILE_UPLOAD) || attachment.mimeType == "application/pdf") provider.uploadAttachment(attachment, apiKey).getOrDefault(attachment) else attachment
            })
        }
        return request.copy(messages = messages)
    }

    private fun supportsRequest(config: AiProviderConfig, request: AiRequest): Boolean {
        val attachments = request.messages.flatMap { it.attachments }
        if (attachments.isEmpty()) return config.capabilities.contains(AiCapability.TEXT)
        return attachments.all { attachment ->
            when {
                attachment.mimeType == "application/pdf" -> config.capabilities.any { it == AiCapability.PDF || it == AiCapability.FILE_UPLOAD }
                attachment.mimeType.startsWith("image") -> config.capabilities.any { it == AiCapability.VISION || it == AiCapability.FILE_UPLOAD }
                else -> config.capabilities.contains(AiCapability.FILE_UPLOAD)
            }
        }
    }

    private fun providerFor(config: AiProviderConfig): AiProvider = when (config.requestFormat) {
        AiRequestFormat.GEMINI_COMPATIBLE -> GeminiProvider(config, client)
        else -> OpenAiCompatibleProvider(config, client)
    }
}
