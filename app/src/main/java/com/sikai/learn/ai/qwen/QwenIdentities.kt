package com.sikai.learn.ai.qwen

/**
 * Pool of plausible browser identities. We rotate through these per Qwen
 * request to make the client traffic look like normal web users rather than
 * one fixed bot. Each entry combines the User-Agent, sec-ch-ua headers, and
 * platform hints — they MUST be self-consistent to avoid WAF flags.
 */
internal data class BrowserIdentity(
    val userAgent: String,
    val secChUa: String,
    val secChUaPlatform: String,
    val secChUaMobile: String,
)

internal object QwenIdentities {
    val pool: List<BrowserIdentity> = listOf(
        BrowserIdentity(
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36",
            secChUa = "\"Google Chrome\";v=\"136\", \"Chromium\";v=\"136\", \"Not.A/Brand\";v=\"99\"",
            secChUaPlatform = "\"Windows\"",
            secChUaMobile = "?0",
        ),
        BrowserIdentity(
            userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 " +
                "(KHTML, like Gecko) Version/17.4 Safari/605.1.15",
            secChUa = "\"Safari\";v=\"17\", \"Not.A/Brand\";v=\"99\"",
            secChUaPlatform = "\"macOS\"",
            secChUaMobile = "?0",
        ),
        BrowserIdentity(
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:136.0) Gecko/20100101 Firefox/136.0",
            secChUa = "\"Firefox\";v=\"136\", \"Chromium\";v=\"136\", \"Not.A/Brand\";v=\"99\"",
            secChUaPlatform = "\"Windows\"",
            secChUaMobile = "?0",
        ),
        BrowserIdentity(
            userAgent = "Mozilla/5.0 (Linux; Android 13; SM-A546B) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/132.0.0.0 Mobile Safari/537.36",
            secChUa = "\"Google Chrome\";v=\"132\", \"Chromium\";v=\"132\", \"Not.A/Brand\";v=\"99\"",
            secChUaPlatform = "\"Android\"",
            secChUaMobile = "?1",
        ),
        BrowserIdentity(
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36 Edg/132.0.0.0",
            secChUa = "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"132\", \"Microsoft Edge\";v=\"132\"",
            secChUaPlatform = "\"Windows\"",
            secChUaMobile = "?0",
        ),
    )

    fun random(): BrowserIdentity = pool.random()
}
