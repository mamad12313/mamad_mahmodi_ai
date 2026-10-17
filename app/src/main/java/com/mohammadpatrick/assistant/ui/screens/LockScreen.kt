package com.mohammadpatrick.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohammadpatrick.assistant.data.security.SecurityManager

@Composable
fun LockScreen(
    securityManager: SecurityManager,
    isSettingNewPin: Boolean = false,
    onUnlocked: () -> Unit,
    onBiometricRequest: (() -> Unit)? = null
) {
    val isPinConfigured = remember { securityManager.isPinConfigured() }
    var enteredPin by remember { mutableStateOf("") }
    var confirmationPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun handleDigitPress(digit: String) {
        if (enteredPin.length < 6) {
            enteredPin += digit
            errorMessage = null

            // If user reaches 4-6 digits, check validation
            if (enteredPin.length >= 4) {
                if (!isPinConfigured || isSettingNewPin) {
                    if (!isConfirming) {
                        // Wait for submit or 6 digits
                    }
                } else {
                    // Verifying existing PIN
                    if (securityManager.verifyPin(enteredPin)) {
                        onUnlocked()
                    } else if (enteredPin.length == 6) {
                        errorMessage = "رمز عبور اشتباه است."
                        enteredPin = ""
                    }
                }
            }
        }
    }

    fun handleDeletePress() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    fun handleSetPinSubmit() {
        if (!isConfirming) {
            if (enteredPin.length < 4) {
                errorMessage = "رمز عبور باید حداقل ۴ رقم باشد."
                return
            }
            confirmationPin = enteredPin
            enteredPin = ""
            isConfirming = true
        } else {
            if (enteredPin == confirmationPin) {
                securityManager.setPin(enteredPin)
                onUnlocked()
            } else {
                errorMessage = "رمزهای وارد شده همخوانی ندارند. مجدداً امتحان کنید."
                enteredPin = ""
                confirmationPin = ""
                isConfirming = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            val titleText = when {
                !isPinConfigured || isSettingNewPin -> {
                    if (isConfirming) "تکرار رمز عبور را وارد کنید" else "یک رمز عبور جدید تعیین کنید"
                }
                else -> "ورود امن به محمد پاتریک"
            }

            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "اطلاعات صوتی و حافظه شما به صورت رمزنگاری‌شده محافظت می‌شود.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(28.dp))

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(6) { index ->
                    val isFilled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .border(
                                width = 2.dp,
                                color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Numeric Keypad
            val digits = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIOMETRIC", "0", "DEL")
            )

            for (row in digits) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (item in row) {
                        when (item) {
                            "BIOMETRIC" -> {
                                if (onBiometricRequest != null && isPinConfigured && !isSettingNewPin) {
                                    IconButton(
                                        onClick = onBiometricRequest,
                                        modifier = Modifier.size(64.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "اثر انگشت",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(64.dp))
                                }
                            }
                            "DEL" -> {
                                IconButton(
                                    onClick = { handleDeletePress() },
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "حذف",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { handleDigitPress(item) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (!isPinConfigured || isSettingNewPin) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { handleSetPinSubmit() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (!isConfirming) "مرحله بعد (تأیید رمز)" else "ثبت نهایی رمز عبور",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
