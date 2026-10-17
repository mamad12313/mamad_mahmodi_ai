package com.mohammadpatrick.assistant.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mohammadpatrick.assistant.MohammadPatrickApp
import com.mohammadpatrick.assistant.ui.components.AppTopBar
import kotlinx.coroutines.launch

@Composable
fun PermissionWizardScreen(
    onNavigateBack: () -> Unit,
    onCompleteWizard: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as MohammadPatrickApp
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasMicPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var isBatteryIgnored by remember {
        mutableStateOf(app.samsungA23Optimizer.isBatteryOptimizationIgnored())
    }

    fun recheckPermissions() {
        hasMicPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
        isBatteryIgnored = app.samsungA23Optimizer.isBatteryOptimizationIgnored()
    }

    // Automatically recheck whenever app returns to foreground (Requirement 8)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                recheckPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Permission Request Launchers
    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "راهنمای راه‌اندازی و دسترسی‌ها",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "تنظیم دقیق مجوزها برای اندروید ۱۴ و گلکسی A23",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "برای کارکرد صحیح دستیار صوتی و یادآورها، دسترسی‌های زیر را بررسی و فعال نمایید. شما می‌توانید موارد اختیاری را رد کرده و بعداً فعال کنید.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // 1. Microphone Permission
            item {
                PermissionItemCard(
                    title = "۱. دسترسی به میکروفون",
                    description = "برای تبدیل گفتار به نوشتار و تشخیص عبارت «سلام پاتریک» در گوشی.",
                    deniedConsequence = "در صورت عدم اعطا، فقط گفتگوی متنی فعال خواهد بود و بخش صوتی کار نمی‌کند.",
                    isGranted = hasMicPermission,
                    icon = Icons.Default.Mic,
                    onRequest = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onOpenSettings = { app.systemActionsManager.openAppSettings() }
                )
            }

            // 2. Notification Permission
            item {
                PermissionItemCard(
                    title = "۲. اعلان‌ها و هشدارها (اندروید ۱۴)",
                    description = "برای نمایش یادآورها در زمان مقرر و نمایش وضعیت سرویس صوتی در نوار بالای گوشی.",
                    deniedConsequence = "در صورت عدم اعطا، یادآورها و آیکون شناور پس‌زمینه نمایش داده نمی‌شوند.",
                    isGranted = hasNotificationPermission,
                    icon = Icons.Default.Notifications,
                    onRequest = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onOpenSettings = { app.systemActionsManager.openAppSettings() }
                )
            }

            // 3. Samsung Galaxy A23 Battery Optimization
            item {
                PermissionItemCard(
                    title = "۳. بهینه‌سازی باتری سامسونگ (حالت نامحدود)",
                    description = "سیستم‌عامل One UI سامسونگ برنامه‌های پس‌زمینه را می‌بندد. با خروج از حالت بهینه‌سازی، سرویس قطع نخواهد شد.",
                    deniedConsequence = "ممکن است پس از قفل شدن صفحه نمایش، عبارت «سلام پاتریک» بلافاصله پاسخ ندهد.",
                    isGranted = isBatteryIgnored,
                    icon = Icons.Default.BatteryAlert,
                    onRequest = { app.samsungA23Optimizer.openIgnoreBatteryOptimizationRequest() },
                    onOpenSettings = { app.samsungA23Optimizer.openSamsungBackgroundLimits() }
                )
            }

            // Completion Action Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            app.settingsRepository.updateSetupWizardCompleted(true)
                            onCompleteWizard()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (hasMicPermission && hasNotificationPermission) "تأیید و ورود به برنامه" else "ادامه با دسترسی‌های فعلی",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionItemCard(
    title: String,
    description: String,
    deniedConsequence: String,
    isGranted: Boolean,
    icon: ImageVector,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isGranted) Color(0xFF10B981).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isGranted) "وضعیت: مجاز شده ✓" else "وضعیت: نیازمند اقدام",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isGranted) Color(0xFF10B981) else Color(0xFFF59E0B)
                    )
                }

                if (isGranted) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "مجاز", tint = Color(0xFF10B981))
                } else {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "غیرمجاز", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "پیامد عدم تأیید: $deniedConsequence",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFF87171)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRequest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("اعطای مجوز", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تنظیمات گوشی", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
