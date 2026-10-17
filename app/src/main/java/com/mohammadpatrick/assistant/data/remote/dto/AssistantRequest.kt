package com.mohammadpatrick.assistant.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data Transfer Objects for the MohammadPatrick Python Flask Backend.
 * Endpoint: POST https://mamad.pfmc.ir/chat
 * Request: {"message": "سلام"}
 * Response: {"reply": "متن پاسخ"} or {"error": "..."}
 */

@JsonClass(generateAdapter = true)
data class AssistantRequest(
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class AssistantResponse(
    @Json(name = "reply") val reply: String? = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "status") val status: String? = null
)
