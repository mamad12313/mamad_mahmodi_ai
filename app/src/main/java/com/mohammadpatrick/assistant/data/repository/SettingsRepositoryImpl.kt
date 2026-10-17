package com.mohammadpatrick.assistant.data.repository

import com.mohammadpatrick.assistant.data.local.pref.PreferencesManager
import com.mohammadpatrick.assistant.domain.model.ApiConfig
import com.mohammadpatrick.assistant.domain.model.AppSettings
import com.mohammadpatrick.assistant.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(
    private val preferencesManager: PreferencesManager
) : SettingsRepository {

    override val settingsFlow: Flow<AppSettings> = preferencesManager.appSettingsFlow

    override suspend fun updateDarkTheme(isDark: Boolean) {
        preferencesManager.setDarkTheme(isDark)
    }

    override suspend fun updateSpeechRate(rate: Float) {
        preferencesManager.setSpeechRate(rate)
    }

    override suspend fun updateTtsEnabled(enabled: Boolean) {
        preferencesManager.setTtsEnabled(enabled)
    }

    override suspend fun updateWakeWordEnabled(enabled: Boolean) {
        preferencesManager.setWakeWordEnabled(enabled)
    }

    override suspend fun updateAutoLockEnabled(enabled: Boolean) {
        preferencesManager.setAutoLockEnabled(enabled)
    }

    override suspend fun updateAutoLockTimeout(minutes: Int) {
        preferencesManager.setAutoLockTimeout(minutes)
    }

    override suspend fun updateBiometricEnabled(enabled: Boolean) {
        preferencesManager.setBiometricEnabled(enabled)
    }

    override suspend fun updateLocalMemoryEnabled(enabled: Boolean) {
        preferencesManager.setLocalMemoryEnabled(enabled)
    }

    override suspend fun updateSetupWizardCompleted(completed: Boolean) {
        preferencesManager.setSetupWizardCompleted(completed)
    }

    override suspend fun updateApiConfig(apiConfig: ApiConfig) {
        preferencesManager.setApiConfig(apiConfig)
    }
}
