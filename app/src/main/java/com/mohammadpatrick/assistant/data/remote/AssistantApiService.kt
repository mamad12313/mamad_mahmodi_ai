package com.mohammadpatrick.assistant.data.remote

import com.mohammadpatrick.assistant.data.remote.dto.AssistantRequest
import com.mohammadpatrick.assistant.data.remote.dto.AssistantResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit Service Interface for the MohammadPatrick Flask backend on Pufferland.
 * Base URL: https://mamad.pfmc.ir/
 * Chat Endpoint: POST /chat with body {"message": "..."}
 * Health Endpoint: GET /health
 */
interface AssistantApiService {

    @POST("chat")
    suspend fun getCompletion(
        @Body request: AssistantRequest,
        @Header("Authorization") authorization: String? = null
    ): Response<AssistantResponse>

    @GET("health")
    suspend fun checkHealth(): Response<Map<String, Any>>
}
