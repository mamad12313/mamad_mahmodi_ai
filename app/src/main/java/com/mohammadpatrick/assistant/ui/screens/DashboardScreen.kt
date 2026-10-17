package com.mohammadpatrick.assistant.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohammadpatrick.assistant.MohammadPatrickApp
import com.mohammadpatrick.assistant.domain.model.SystemAction
import com.mohammadpatrick.assistant.service.WakeWordForegroundService
import com.mohammadpatrick.assistant.ui.components.StatusCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToVoice: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToWizard: () -> Unit,
    onNavigateToSamsungGuide: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLockApp: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as MohammadPatrickApp

    val settings by app.settingsRepository.settingsFlow.collectAsState(initial = null)
    val isServiceRunning by WakeWordForegroundService.isRunningFlow.collectAsState()

    val systemActionsManager = remember { app.systemActionsManager }
    val dateTimeInfo = remember { systemActionsManager.getFormattedPersianDateTime() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "محمد پاتریک",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "سامسونگ گلکسی A23 - اندروید ۱۴",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLockApp) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "قفل کردن برنامه",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "تنظیمات",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            // Live Status Card Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "وضعیت واقعی سرویس‌ها",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Mic Status
                        StatusCard(
                            title = "وضعیت میکروفون",
                            statusText = if (isServiceRunning) "سرویس میکروفون آماده تشخیص صوتی" else "میکروفون در حالت آماده‌باش محلی",
                            isActive = isServiceRunning
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Wake-word service
                        StatusCard(
                            title = "سرویس پس‌زمینه «سلام پاتریک»",
                            statusText = if (isServiceRunning) "فعال در نوار اعلان سامسونگ (پیش‌زمینه)" else "متوقف (صرفه‌جویی در مصرف باتری)",
                            isActive = isServiceRunning,
                            activeColor = Color(0xFF10B981),
                            inactiveColor = Color(0xFFF59E0B)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // API Connection state
                        val apiConfig = settings?.apiConfig
                        val isApiConnected = apiConfig?.isConfigured == true && apiConfig.isEnabled
                        StatusCard(
                            title = "اتصال هوش مصنوعی (API)",
                            statusText = if (isApiConnected) "آدرس سرور تنظیم شده: ${apiConfig?.baseUrl}" else "تنظیم‌نشده (در این فاز سرور متصل نیست)",
                            isActive = isApiConnected,
                            activeColor = Color(0xFF10B981),
                            inactiveColor = Color(0xFFF43F5E)
                        )
                    }
                }
            }

            // Quick Voice Action Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0284C7), Color(0xFF06B6D4))
                            )
                        )
                        .clickable { onNavigateToVoice() }
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "شروع مکالمه صوتی («سلام پاتریک»)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "برای صدور فرمان صوتی و تبدیل گفتار لمس کنید",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Quick Service Toggle Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (isServiceRunning) {
                                WakeWordForegroundService.stopService(context)
                                Toast.makeText(context, "سرویس پس‌زمینه صوتی متوقف شد", Toast.LENGTH_SHORT).show()
                            } else {
                                WakeWordForegroundService.startService(context)
                                Toast.makeText(context, "سرویس پس‌زمینه صوتی آغاز شد", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isServiceRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isServiceRunning) "توقف سرویس شنیدن" else "فعال‌سازی سرویس شنیدن",
                            color = if (isServiceRunning) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Button(
                        onClick = onNavigateToSamsungGuide,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "راهنمای گلکسی A23",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // Main Application Modules Grid
            item {
                Text(
                    text = "بخش‌های اصلی برنامه",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DashboardModuleCard(
                            title = "گفتگوی متنی",
                            subtitle = "تاریخچه محلی پیام‌ها",
                            icon = Icons.Default.Chat,
                            onClick = onNavigateToChat,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardModuleCard(
                            title = "حافظه محلی",
                            subtitle = "یادداشت‌ها و اولویت‌ها",
                            icon = Icons.Default.Psychology,
                            onClick = onNavigateToMemory,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DashboardModuleCard(
                            title = "یادآورها",
                            subtitle = "زمان‌بندی هوشمند کارها",
                            icon = Icons.Default.Alarm,
                            onClick = onNavigateToReminders,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardModuleCard(
                            title = "دسترسی‌ها",
                            subtitle = "راهنمای گام‌به‌گام مجوزها",
                            icon = Icons.Default.Security,
                            onClick = onNavigateToWizard,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick Phone Tools & Actions (Requirement 14)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "عملیات محلی گوشی (گلکسی A23)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Date & Time Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "تاریخ امروز: ${dateTimeInfo.persianDate}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "ساعت جاری: ${dateTimeInfo.time} | میلادی: ${dateTimeInfo.gregorianDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Shortcuts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            QuickActionChip(
                                label = "وای‌فای",
                                icon = Icons.Default.Wifi,
                                onClick = { systemActionsManager.openWifiSettings() }
                            )
                            QuickActionChip(
                                label = "بلوتوث",
                                icon = Icons.Default.Bluetooth,
                                onClick = { systemActionsManager.openBluetoothSettings() }
                            )
                            QuickActionChip(
                                label = "تنظیم صدا",
                                icon = Icons.Default.VolumeUp,
                                onClick = { systemActionsManager.openSoundSettings() }
                            )
                            QuickActionChip(
                                label = "درباره",
                                icon = Icons.Default.Info,
                                onClick = onNavigateToAbout
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun QuickActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
