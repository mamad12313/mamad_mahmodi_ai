package com.mohammadpatrick.assistant.data.local.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mohammadpatrick.assistant.domain.model.ApiConfig
import com.mohammadpatrick.assistant.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mohammad_patrick_prefs")

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val WAKE_WORD_ENABLED = booleanPreferencesKey("wake_word_enabled")
        val WAKE_WORD_PHRASE = stringPreferencesKey("wake_word_phrase")
        val AUTO_LOCK_ENABLED = booleanPreferencesKey("auto_lock_enabled")
        val AUTO_LOCK_TIMEOUT_MINUTES = intPreferencesKey("auto_lock_timeout_minutes")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val LOCAL_MEMORY_ENABLED = booleanPreferencesKey("local_memory_enabled")
        val SETUP_WIZARD_COMPLETED = booleanPreferencesKey("setup_wizard_completed")

        // API Configurations for future integration
        val API_BASE_URL = stringPreferencesKey("api_base_url")
        val API_KEY = stringPreferencesKey("api_key")
        val API_TIMEOUT_SECONDS = longPreferencesKey("api_timeout_seconds")
        val API_ENABLED = booleanPreferencesKey("api_enabled")
        val API_STREAMING_ENABLED = booleanPreferencesKey("api_streaming_enabled")
    }

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val apiConfig = ApiConfig(
            baseUrl = prefs[PreferencesKeys.API_BASE_URL] ?: "https://mamad.pfmc.ir",
            apiKey = prefs[PreferencesKeys.API_KEY] ?: "",
            timeoutSeconds = prefs[PreferencesKeys.API_TIMEOUT_SECONDS] ?: 30L,
            isEnabled = prefs[PreferencesKeys.API_ENABLED] ?: true,
            isStreamingEnabled = prefs[PreferencesKeys.API_STREAMING_ENABLED] ?: false
        )

        AppSettings(
            isDarkTheme = prefs[PreferencesKeys.DARK_THEME] ?: true,
            fontScale = prefs[PreferencesKeys.FONT_SCALE] ?: 1.0f,
            speechRate = prefs[PreferencesKeys.SPEECH_RATE] ?: 1.0f,
            isTtsEnabled = prefs[PreferencesKeys.TTS_ENABLED] ?: true,
            isWakeWordEnabled = prefs[PreferencesKeys.WAKE_WORD_ENABLED] ?: false,
            wakeWordPhrase = prefs[PreferencesKeys.WAKE_WORD_PHRASE] ?: "M.H.O.I",
            isAutoLockEnabled = prefs[PreferencesKeys.AUTO_LOCK_ENABLED] ?: true,
            autoLockTimeoutMinutes = prefs[PreferencesKeys.AUTO_LOCK_TIMEOUT_MINUTES] ?: 1,
            isBiometricEnabled = prefs[PreferencesKeys.BIOMETRIC_ENABLED] ?: false,
            isLocalMemoryEnabled = prefs[PreferencesKeys.LOCAL_MEMORY_ENABLED] ?: true,
            isSetupWizardCompleted = prefs[PreferencesKeys.SETUP_WIZARD_COMPLETED] ?: false,
            apiConfig = apiConfig
        )
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DARK_THEME] = enabled }
    }

    suspend fun setSpeechRate(rate: Float) {
        context.dataStore.edit { it[PreferencesKeys.SPEECH_RATE] = rate }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TTS_ENABLED] = enabled }
    }

    suspend fun setWakeWordEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.WAKE_WORD_ENABLED] = enabled }
    }

    suspend fun setAutoLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_LOCK_ENABLED] = enabled }
    }

    suspend fun setAutoLockTimeout(minutes: Int) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_LOCK_TIMEOUT_MINUTES] = minutes }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setLocalMemoryEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.LOCAL_MEMORY_ENABLED] = enabled }
    }

    suspend fun setSetupWizardCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SETUP_WIZARD_COMPLETED] = completed }
    }

    suspend fun setApiConfig(config: ApiConfig) {
        context.dataStore.edit {
            it[PreferencesKeys.API_BASE_URL] = config.baseUrl
            it[PreferencesKeys.API_KEY] = config.apiKey
            it[PreferencesKeys.API_TIMEOUT_SECONDS] = config.timeoutSeconds
            it[PreferencesKeys.API_ENABLED] = config.isEnabled
            it[PreferencesKeys.API_STREAMING_ENABLED] = config.isStreamingEnabled
        }
    }
}
