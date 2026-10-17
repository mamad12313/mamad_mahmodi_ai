package com.mohammadpatrick.assistant.domain.model

data class AppSettings(
    val isDarkTheme: Boolean = true,
    val fontScale: Float = 1.0f,
    val speechRate: Float = 1.0f,
    val isTtsEnabled: Boolean = true,
    val isWakeWordEnabled: Boolean = false,
    val wakeWordPhrase: String = "سلام پاتریک",
    val isAutoLockEnabled: Boolean = true,
    val autoLockTimeoutMinutes: Int = 1,
    val isBiometricEnabled: Boolean = false,
    val isLocalMemoryEnabled: Boolean = true,
    val isSetupWizardCompleted: Boolean = false,
    val apiConfig: ApiConfig = ApiConfig()
)
