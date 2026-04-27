package com.sikai.learn.ai.qwen

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * SSE parser for Qwen's streaming chat response. Mirrors the relevant parts of
 * Flashy's `parse_stream_chunks`: it accumulates partial deltas, separates
 * reasoning ("think" phase) from the answer phase, and yields finished events.
 */
internal class QwenStreamParser {
    private val sb = StringBuilder()
    private val answer = StringBuilder()
    private val reasoning = StringBuilder()
    var finishReason: String? = null
        private set
    var pendingParentId: String? = null
        private set

    /** Returns true if the stream has signalled completion. */
    fun feed(chunk: String): Boolean {
        sb.append(chunk)
        var idx = sb.indexOf('\n')
        var done = false
        while (idx >= 0) {
            val line = sb.substring(0, idx).trim()
            sb.delete(0, idx + 1)
            if (line.isNotEmpty() && line.startsWith("data: ")) {
                val payload = line.substring(6).trim()
                if (payload == "[DONE]") {
                    done = true
                } else {
                    runCatching { handle(Json.parseToJsonElement(payload) as? JsonObject) }
                }
            }
            idx = sb.indexOf('\n')
        }
        return done
    }

    fun finalAnswer(): String = answer.toString()
    fun finalReasoning(): String? = reasoning.takeIf { it.isNotEmpty() }?.toString()

    private fun handle(obj: JsonObject?) {
        obj ?: return
        val createdId = (obj["response.created"] as? JsonObject)?.let {
            (it["response_id"] as? JsonPrimitive)?.content
        }
        if (createdId != null) pendingParentId = createdId

        val choices = obj["choices"] as? JsonArray ?: return
        val first = choices.firstOrNull() as? JsonObject ?: return
        val delta = first["delta"] as? JsonObject ?: return
        val phase = (delta["phase"] as? JsonPrimitive)?.content
        val content = (delta["content"] as? JsonPrimitive)?.content
        val finish = (first["finish_reason"] as? JsonPrimitive)?.content

        if (!content.isNullOrEmpty()) {
            if (phase == "think" || phase == "web_search") {
                reasoning.append(content)
            } else {
                answer.append(content)
            }
        }
        if (!finish.isNullOrEmpty()) finishReason = finish
    }
}
