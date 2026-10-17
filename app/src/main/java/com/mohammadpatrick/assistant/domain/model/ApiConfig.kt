package com.mohammadpatrick.assistant.domain.model

/**
 * Configuration for connecting to the M.H.O.I AI backend.
 * Base URL: https://mamad.pfmc.ir
 */
data class ApiConfig(
    val baseUrl: String = "https://mamad.pfmc.ir",
    val apiKey: String = "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",
    val timeoutSeconds: Long = 30L,
    val isEnabled: Boolean = true,
    val isStreamingEnabled: Boolean = false
) {
    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && (baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))
}
