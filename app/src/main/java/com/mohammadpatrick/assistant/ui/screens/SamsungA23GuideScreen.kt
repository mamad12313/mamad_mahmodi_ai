package com.mohammadpatrick.assistant.ui.screens

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohammadpatrick.assistant.MohammadPatrickApp
import com.mohammadpatrick.assistant.ui.components.AppTopBar

@Composable
fun SamsungA23GuideScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as MohammadPatrickApp
    val optimizer = remember { app.samsungA23Optimizer }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "راهنمای بهینه‌سازی سامسونگ A23",
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
            // Device Detection Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "دستگاه شما: ${optimizer.deviceModel}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "رابط کاربری Samsung One UI 6 (اندروید ۱۴)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Notice
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF451A03).copy(alpha = 0.5f))
                        .border(1.dp, Color(0xFFD97706), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFFFBBF24))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "توجه: سامسونگ و اندروید به دلایل امنیتی و مصرف باتری ممکن است در حالت خواب عمیق سرویس‌ها را متوقف کنند. مراحل زیر را برای جلوگیری از قطعی عبارت «سلام پاتریک» دنبال نمایید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFEF3C7)
                    )
                }
            }

            // Step 1: Unrestricted Battery
            item {
                GuideStepCard(
                    stepNumber = "۱",
                    title = "تنظیم باتری در حالت Unrestricted (نامحدود)",
                    explanation = "به بخش تنظیمات باتری برنامه بروید و حالت باتری را از Optimized (بهینه‌سازی شده) به Unrestricted تغییر دهید.",
                    buttonText = "درخواست معافیت باتری",
                    onAction = { optimizer.openIgnoreBatteryOptimizationRequest() }
                )
            }

            // Step 2: Never sleeping apps
            item {
                GuideStepCard(
                    stepNumber = "۲",
                    title = "افزودن به برنامه‌های هرگز به خواب نرود (Never sleeping apps)",
                    explanation = "در تنظیمات سامسونگ: مسیر مراقبت از دستگاه (Device Care) > باتری (Battery) > محدودیت‌های استفاده در پس‌زمینه (Background usage limits) را باز کنید و محمد پاتریک را به Never sleeping apps اضافه کنید.",
                    buttonText = "باز کردن مدیریت باتری سامسونگ",
                    onAction = { optimizer.openSamsungBackgroundLimits() }
                )
            }

            // Step 3: Notification permission
            item {
                GuideStepCard(
                    stepNumber = "۳",
                    title = "فعال نگه‌داشتن اعلان‌های پس‌زمینه",
                    explanation = "در اندروید ۱۴، سرویس‌های میکروفون پس‌زمینه باید اعلان ماندگار داشته باشند تا سیستم آن‌ها را از بین نبرد.",
                    buttonText = "تنظیمات اعلان برنامه",
                    onAction = { app.systemActionsManager.openAppSettings() }
                )
            }

            // Step 4: Troubleshooting
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "عیب‌یابی توقف عبارت بیداربش («سلام پاتریک»)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• اگر برنامه Force Stop شود، طبق قوانین اندروید ۱۴ هیچ کدی تا اجرای دستی بعدی کاربر اجرا نخواهد شد.\n• در صورت استفاده از برنامه‌های Task Killer یا پاک‌کننده‌های رم، محمد پاتریک را به لیست استثنا اضافه کنید.\n• در مواقعی که میکروفون توسط برنامه دیگری مانند ضبط مکالمه یا واتساپ اشغال باشد، سیستم اولویت را به تماس می‌دهد.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GuideStepCard(
    stepNumber: String,
    title: String,
    explanation: String,
    buttonText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(buttonText, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
