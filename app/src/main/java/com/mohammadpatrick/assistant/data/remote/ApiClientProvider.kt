package com.mohammadpatrick.assistant.data.remote

import com.mohammadpatrick.assistant.data.local.pref.PreferencesManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Provides dynamic, thread-safe Retrofit & OkHttpClient instances
 * created dynamically from user's custom Base URL configuration.
 */
class ApiClientProvider(private val preferencesManager: PreferencesManager) {

    private var cachedBaseUrl: String? = null
    private var cachedService: AssistantApiService? = null

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    suspend fun getApiService(): AssistantApiService? {
        val settings = preferencesManager.appSettingsFlow.first()
        val config = settings.apiConfig

        if (!config.isConfigured) {
            return null
        }

        // Normalize base URL: strip accidental /chat suffix or trailing slash, then ensure single trailing slash
        val cleanBase = config.baseUrl.trim().removeSuffix("/chat").removeSuffix("/chat/").removeSuffix("/")
        val normalizedUrl = "$cleanBase/"

        if (cachedService != null && cachedBaseUrl == normalizedUrl) {
            return cachedService
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(config.timeoutSeconds, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .retryOnConnectionFailure(true)
            .build()

        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl(normalizedUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val service = retrofit.create(AssistantApiService::class.java)
            cachedBaseUrl = normalizedUrl
            cachedService = service
            service
        } catch (e: Exception) {
            null
        }
    }

    fun clearCache() {
        cachedBaseUrl = null
        cachedService = null
    }
}
