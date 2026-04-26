package com.sikai.learn.core.storage

import com.sikai.learn.data.db.dao.ProviderLogDao
import com.sikai.learn.data.db.entity.ProviderLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tiny scoped logger that records provider success/failure outcomes locally.
 * It intentionally never persists API keys, request bodies, or PII — only the
 * provider id, model id, and the failure category. Used by the orchestrator and
 * surfaced in Settings → Provider logs.
 */
@Singleton
class ProviderLogger @Inject constructor(
    private val dao: ProviderLogDao,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun logSuccess(providerId: String, modelId: String?) = log(providerId, "success", modelId = modelId)

    fun logFailure(providerId: String, reason: String, message: String?) =
        log(providerId, "failure", reason = reason, message = message?.take(200))

    private fun log(providerId: String, outcome: String, reason: String? = null, message: String? = null, modelId: String? = null) {
        scope.launch {
            dao.insert(
                ProviderLogEntity(
                    providerId = providerId,
                    outcome = outcome,
                    reason = reason,
                    message = message,
                    modelId = modelId,
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
    }
}
