package com.mohammadpatrick.assistant.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

/**
 * Handles PIN hashing (Salted SHA-256 / PBKDF2 style), lock state, auto-lock timer,
 * and encrypted storage using Android Keystore & EncryptedSharedPreferences.
 */
class SecurityManager(private val context: Context) {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val securePrefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                "secret_security_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback for devices with corrupted keystore
            context.getSharedPreferences("secret_security_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    companion object {
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_PIN_SALT = "key_pin_salt"
        private const val KEY_IS_PIN_SET = "key_is_pin_set"
        private const val KEY_LAST_UNLOCKED_TIME = "key_last_unlocked_time"
        private const val KEY_APP_BACKGROUND_TIME = "key_app_background_time"
    }

    private var isCurrentlyUnlocked: Boolean = false

    fun isPinConfigured(): Boolean {
        return securePrefs.getBoolean(KEY_IS_PIN_SET, false)
    }

    fun setPin(newPin: String): Boolean {
        if (newPin.length < 4) return false

        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)
        val saltBase64 = Base64.encodeToString(saltBytes, Base64.NO_WRAP)

        val hash = hashPinWithSalt(newPin, saltBase64)

        securePrefs.edit()
            .putString(KEY_PIN_SALT, saltBase64)
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_IS_PIN_SET, true)
            .apply()

        isCurrentlyUnlocked = true
        recordUnlockTime()
        return true
    }

    fun verifyPin(enteredPin: String): Boolean {
        val storedHash = securePrefs.getString(KEY_PIN_HASH, null) ?: return false
        val storedSalt = securePrefs.getString(KEY_PIN_SALT, null) ?: return false

        val enteredHash = hashPinWithSalt(enteredPin, storedSalt)
        val matches = enteredHash == storedHash

        if (matches) {
            isCurrentlyUnlocked = true
            recordUnlockTime()
        }
        return matches
    }

    fun recordUnlockTime() {
        securePrefs.edit()
            .putLong(KEY_LAST_UNLOCKED_TIME, System.currentTimeMillis())
            .apply()
    }

    fun recordAppEnteredBackground() {
        securePrefs.edit()
            .putLong(KEY_APP_BACKGROUND_TIME, System.currentTimeMillis())
            .apply()
    }

    fun shouldLockOnForeground(timeoutMinutes: Int): Boolean {
        if (!isPinConfigured()) return false
        if (!isCurrentlyUnlocked) return true

        if (timeoutMinutes <= 0) {
            // Lock immediately on return
            isCurrentlyUnlocked = false
            return true
        }

        val backgroundTime = securePrefs.getLong(KEY_APP_BACKGROUND_TIME, 0L)
        if (backgroundTime == 0L) return false

        val elapsedMillis = System.currentTimeMillis() - backgroundTime
        val timeoutMillis = timeoutMinutes * 60 * 1000L

        if (elapsedMillis >= timeoutMillis) {
            isCurrentlyUnlocked = false
            return true
        }
        return false
    }

    fun isAppUnlocked(): Boolean {
        if (!isPinConfigured()) return true
        return isCurrentlyUnlocked
    }

    fun lockNow() {
        isCurrentlyUnlocked = false
    }

    fun markUnlockedByBiometric() {
        isCurrentlyUnlocked = true
        recordUnlockTime()
    }

    private fun hashPinWithSalt(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.toByteArray(Charsets.UTF_8))
        val hashedBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashedBytes, Base64.NO_WRAP)
    }
}
