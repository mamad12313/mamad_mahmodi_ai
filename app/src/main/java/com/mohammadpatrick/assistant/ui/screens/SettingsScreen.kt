package com.mohammadpatrick.assistant.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohammadpatrick.assistant.MohammadPatrickApp
import com.mohammadpatrick.assistant.domain.model.ApiConfig
import com.mohammadpatrick.assistant.ui.components.AppTopBar
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWizard: () -> Unit,
    onNavigateToSamsungGuide: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onChangePassword: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as MohammadPatrickApp
    val coroutineScope = rememberCoroutineScope()

    val settings by app.settingsRepository.settingsFlow.collectAsState(initial = null)

    var apiUrl by remember(settings) { mutableStateOf(settings?.apiConfig?.baseUrl ?: "") }
    var apiKey by remember(settings) { mutableStateOf(settings?.apiConfig?.apiKey ?: "") }
    var isApiTesting by remember { mutableStateOf(false) }
    var apiTestResult by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "تنظیمات برنامه",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Future API Connection Settings Section (Requirement 5)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Api, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تنظیمات اتصال API آینده (سرور اختصاصی)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "در این فاز سرور اختصاصی متصل نیست. می‌توانید پس از آماده‌سازی API خود، آدرس Base URL و کلید را در اینجا وارد کنید.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = apiUrl,
                            onValueChange = { apiUrl = it },
                            label = { Text("Base URL سرور") },
                            placeholder = { Text("https://api.yourserver.com/v1") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("کلید API (اختیاری)") },
                            placeholder = { Text("Bearer Token یا API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val newConfig = ApiConfig(
                                            baseUrl = apiUrl.trim(),
                                            apiKey = apiKey.trim(),
                                            isEnabled = apiUrl.isNotBlank()
                                        )
                                        app.settingsRepository.updateApiConfig(newConfig)
                                        Toast.makeText(context, "تنظیمات API ذخیره شد", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("ذخیره تنظیمات")
                            }

                            Button(
                                onClick = {
                                    isApiTesting = true
                                    apiTestResult = null
                                    coroutineScope.launch {
                                        val result = app.assistantRepository.testApiConnection()
                                        isApiTesting = false
                                        apiTestResult = if (result.isSuccess) {
                                            "اتصال موفقیت‌آمیز بود!"
                                        } else {
                                            "خطا: ${result.exceptionOrNull()?.message}"
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                enabled = !isApiTesting,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isApiTesting) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("تست اتصال", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        if (apiTestResult != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = apiTestResult!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (apiTestResult!!.contains("موفقیت")) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Voice & Speech Settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تنظیمات گفتار و صدا",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // TTS toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("خواندن صوتی پاسخ‌ها (TTS)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("پاسخ‌های متنی به زبان فارسی خوانده شود", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                            Switch(
                                checked = settings?.isTtsEnabled ?: true,
                                onCheckedChange = { enabled ->
                                    coroutineScope.launch { app.settingsRepository.updateTtsEnabled(enabled) }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Wake-word toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("حالت عبارت بیداربش («سلام پاتریک»)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("شنیدن عبارت در پس‌زمینه (نیازمند مجوز اعلان و عدم بهینه‌سازی باتری)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                            Switch(
                                checked = settings?.isWakeWordEnabled ?: false,
                                onCheckedChange = { enabled ->
                                    coroutineScope.launch { app.settingsRepository.updateWakeWordEnabled(enabled) }
                                }
                            )
                        }
                    }
                }
            }

            // Security & Lock Settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "امنیت و قفل برنامه",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onChangePassword,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تغییر رمز عبور برنامه", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("قفل خودکار", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("هنگام خروج از برنامه مجدداً رمز درخواست شود", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                            Switch(
                                checked = settings?.isAutoLockEnabled ?: true,
                                onCheckedChange = { enabled ->
                                    coroutineScope.launch { app.settingsRepository.updateAutoLockEnabled(enabled) }
                                }
                            )
                        }
                    }
                }
            }

            // System Guides Navigation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        SettingsNavRow(
                            title = "راهنمای دسترسی‌ها و مجوزها",
                            subtitle = "بررسی وضعیت مجوزهای میکروفون، اعلان و باتری",
                            icon = Icons.Default.Security,
                            onClick = onNavigateToWizard
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        SettingsNavRow(
                            title = "بهینه‌سازی باتری سامسونگ A23",
                            subtitle = "راهنمای حالت Unrestricted و Never sleeping apps در One UI",
                            icon = Icons.Default.Smartphone,
                            onClick = onNavigateToSamsungGuide
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        SettingsNavRow(
                            title = "درباره محمد پاتریک",
                            subtitle = "اطلاعات نگارش، معماری کاتلین و لایسنس",
                            icon = Icons.Default.Info,
                            onClick = onNavigateToAbout
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsNavRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp)
        )
    }
}
