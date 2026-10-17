package com.mohammadpatrick.assistant

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.mohammadpatrick.assistant.domain.model.AppSettings
import com.mohammadpatrick.assistant.ui.navigation.AppNavHost
import com.mohammadpatrick.assistant.ui.navigation.Screen
import com.mohammadpatrick.assistant.ui.theme.MohammadPatrickTheme

class MainActivity : FragmentActivity() {

    private lateinit var app: MohammadPatrickApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = applicationContext as MohammadPatrickApp

        setContent {
            val settings by app.settingsRepository.settingsFlow.collectAsState(initial = AppSettings())
            val navController = rememberNavController()

            MohammadPatrickTheme(darkTheme = settings?.isDarkTheme ?: true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(
                        navController = navController,
                        onBiometricRequest = { showBiometricPrompt() }
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        app.securityManager.recordAppEnteredBackground()
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    app.securityManager.markUnlockedByBiometric()
                    Toast.makeText(this@MainActivity, "احراز هویت بیومتریک با موفقیت انجام شد", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Inform user without crashing
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.lock_biometric_title))
            .setSubtitle(getString(R.string.lock_biometric_subtitle))
            .setNegativeButtonText("ورود با رمز عبور")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
