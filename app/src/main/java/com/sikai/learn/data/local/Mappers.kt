package com.sikai.learn.data.local

import com.sikai.learn.domain.ai.*
import com.sikai.learn.domain.content.ContentManifest
import com.sikai.learn.domain.learning.Question

private fun csv(value: Iterable<String>) = value.joinToString("|") { it.replace("|", "/") }
private fun split(value: String) = value.split("|").map { it.trim() }.filter { it.isNotEmpty() }

fun ContentManifestEntity.toDomain() = ContentManifest(id, title, type, classLevel, subject, year, fileUrl, fileKey, sizeBytes, checksumSha256, version, updatedAt, language, split(tagsCsv))
fun ContentManifest.toEntity() = ContentManifestEntity(id, title, type, classLevel, subject, year, fileUrl, fileKey, sizeBytes, checksumSha256, version, updatedAt, language, csv(tags))
fun QuestionEntity.toDomain() = Question(id, classLevel, subject, topic, prompt, split(optionsCsv), answerIndex, explanation)
fun Question.toEntity() = QuestionEntity(id, classLevel, subject, topic, prompt, csv(options), answerIndex, explanation)

fun AiProviderConfigEntity.toDomain() = AiProviderConfig(
    id = id,
    name = name,
    type = AiProviderType.valueOf(type),
    baseUrl = baseUrl,
    apiKeyAlias = apiKeyAlias,
    textModel = textModel,
    multimodalModel = multimodalModel,
    capabilities = split(capabilitiesCsv).map { AiCapability.valueOf(it) }.toSet(),
    priority = priority,
    enabled = enabled,
    requestFormat = AiRequestFormat.valueOf(requestFormat),
    supportsSearch = supportsSearch,
    supportsThinking = supportsThinking
)

fun AiProviderConfig.toEntity() = AiProviderConfigEntity(
    id = id,
    name = name,
    type = type.name,
    baseUrl = baseUrl,
    apiKeyAlias = apiKeyAlias,
    textModel = textModel,
    multimodalModel = multimodalModel,
    capabilitiesCsv = csv(capabilities.map { it.name }),
    priority = priority,
    enabled = enabled,
    requestFormat = requestFormat.name,
    supportsSearch = supportsSearch,
    supportsThinking = supportsThinking
)
