package com.mohammadpatrick.assistant.domain.repository

import com.mohammadpatrick.assistant.domain.model.ApiConfig
import com.mohammadpatrick.assistant.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settingsFlow: Flow<AppSettings>
    suspend fun updateDarkTheme(isDark: Boolean)
    suspend fun updateSpeechRate(rate: Float)
    suspend fun updateTtsEnabled(enabled: Boolean)
    suspend fun updateWakeWordEnabled(enabled: Boolean)
    suspend fun updateAutoLockEnabled(enabled: Boolean)
    suspend fun updateAutoLockTimeout(minutes: Int)
    suspend fun updateBiometricEnabled(enabled: Boolean)
    suspend fun updateLocalMemoryEnabled(enabled: Boolean)
    suspend fun updateSetupWizardCompleted(completed: Boolean)
    suspend fun updateApiConfig(apiConfig: ApiConfig)
}
