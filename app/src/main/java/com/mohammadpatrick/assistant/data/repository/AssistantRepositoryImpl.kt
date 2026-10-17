package com.mohammadpatrick.assistant.data.repository

import com.mohammadpatrick.assistant.data.local.pref.PreferencesManager
import com.mohammadpatrick.assistant.data.remote.ApiClientProvider
import com.mohammadpatrick.assistant.data.remote.dto.AssistantRequest
import com.mohammadpatrick.assistant.data.remote.dto.AssistantResponse
import com.mohammadpatrick.assistant.domain.repository.AssistantRepository
import com.mohammadpatrick.assistant.domain.repository.AssistantResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Repository implementation communicating with the Python Flask AI backend.
 * Base URL: https://mamad.pfmc.ir
 * Chat: POST /chat with body {"message": "..."}
 * Health: GET /health
 */
class AssistantRepositoryImpl(
    private val apiClientProvider: ApiClientProvider,
    private val preferencesManager: PreferencesManager
) : AssistantRepository {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    override suspend fun sendMessage(
        conversationId: String,
        userPrompt: String,
        attachedImageBase64: String?,
        attachedFileText: String?
    ): AssistantResult = withContext(Dispatchers.IO) {
        val settings = preferencesManager.appSettingsFlow.first()
        val config = settings.apiConfig

        if (!config.isConfigured || !config.isEnabled) {
            return@withContext AssistantResult.ApiNotConfigured(
                "اتصال به سرور هوش مصنوعی برقرار نیست. لطفاً تنظیمات را بررسی کنید."
            )
        }

        val service = apiClientProvider.getApiService()
            ?: return@withContext AssistantResult.Error("آدرس سرور https://mamad.pfmc.ir در دسترس نیست.")

        try {
            val authHeader = if (config.apiKey.isNotBlank()) "Bearer ${config.apiKey}" else null
            val request = AssistantRequest(message = userPrompt)

            val response = service.getCompletion(request, authHeader)
            if (response.isSuccessful) {
                val body = response.body()
                val reply = body?.reply
                if (!reply.isNullOrBlank()) {
                    AssistantResult.Success(reply.trim())
                } else if (!body?.error.isNullOrBlank()) {
                    AssistantResult.Error("پیام سرور: ${body.error}")
                } else {
                    AssistantResult.Error("سرور پاسخ خالی ارسال کرد.")
                }
            } else {
                // Parse error body JSON if present (e.g. {"error": "API key تنظیم نشده است"})
                val errorString = try {
                    response.errorBody()?.string()
                } catch (e: Exception) {
                    null
                }

                val serverErrorMsg = parseServerError(errorString) ?: response.message()
                AssistantResult.Error("خطای سرور (${response.code()}): $serverErrorMsg")
            }
        } catch (e: java.net.SocketTimeoutException) {
            AssistantResult.Error("مهلت زمانی ارتباط با سرور به پایان رسید (Timeout). لطفا دوباره تلاش کنید.")
        } catch (e: java.net.UnknownHostException) {
            AssistantResult.Error("آدرس سرور mamad.pfmc.ir یافت نشد. لطفاً اتصال اینترنت خود را بررسی کنید.")
        } catch (e: java.net.ConnectException) {
            AssistantResult.Error("امکان اتصال به سرور پافرلند وجود ندارد. لطفاً روشن بودن سرور را بررسی کنید.")
        } catch (e: Exception) {
            AssistantResult.Error("خطا در برقراری ارتباط: ${e.localizedMessage ?: "خطای ناشناخته"}", e)
        }
    }

    override fun streamMessage(
        conversationId: String,
        userPrompt: String
    ): Flow<AssistantResult> = flow {
        emit(AssistantResult.Loading)
        val result = sendMessage(conversationId, userPrompt, null, null)
        emit(result)
    }.flowOn(Dispatchers.IO)

    override suspend fun testApiConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        val service = apiClientProvider.getApiService()
            ?: return@withContext Result.failure(Exception("آدرس سرور تنظیم نشده است."))

        try {
            val response = service.checkHealth()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("کد پاسخ سلامت سرور: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseServerError(errorJson: String?): String? {
        if (errorJson.isNullOrBlank()) return null
        return try {
            val adapter = moshi.adapter(AssistantResponse::class.java)
            val parsed = adapter.fromJson(errorJson)
            parsed?.error ?: errorJson
        } catch (e: Exception) {
            errorJson
        }
    }
}
