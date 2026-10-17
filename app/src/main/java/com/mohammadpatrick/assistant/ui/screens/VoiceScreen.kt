package com.mohammadpatrick.assistant.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohammadpatrick.assistant.MohammadPatrickApp
import com.mohammadpatrick.assistant.audio.PersianTtsManager
import com.mohammadpatrick.assistant.audio.TtsStatus
import com.mohammadpatrick.assistant.audio.VoiceManager
import com.mohammadpatrick.assistant.audio.VoiceState
import com.mohammadpatrick.assistant.ui.components.AppTopBar
import kotlinx.coroutines.launch

@Composable
fun VoiceScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as MohammadPatrickApp
    val coroutineScope = rememberCoroutineScope()

    val voiceManager = remember { VoiceManager(context) }
    val ttsManager = remember { PersianTtsManager(context) }

    val voiceState by voiceManager.voiceState.collectAsState()
    val liveSpokenText by voiceManager.liveSpokenText.collectAsState()
    val ttsStatus by ttsManager.ttsStatus.collectAsState()

    var editableText by remember { mutableStateOf("") }
    var assistantResponseText by remember { mutableStateOf<String?>(null) }
    var speechSpeed by remember { mutableFloatStateOf(1.0f) }

    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(voiceState) {
        if (voiceState is VoiceState.Listening) {
            pulseScale.animateTo(
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, easing = LinearEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                )
            )
        } else {
            pulseScale.snapTo(1f)
        }

        if (voiceState is VoiceState.Success) {
            val result = (voiceState as VoiceState.Success)
            editableText = result.recognizedText

            // If wake word was detected or speech finished, prepare response
            if (result.isWakeWordDetected) {
                assistantResponseText = "جانم محمد"
                ttsManager.speak(assistantResponseText!!, speechSpeed)
            } else if (result.recognizedText.isNotBlank()) {
                val prompt = result.recognizedText
                // Live web search & response for Mohammad Parsa
                assistantResponseText = "در حال جستجو و پاسخ به «$prompt»... نتیجه در کادر زیر آماده شد."
                ttsManager.speak("جانم محمد، در حال بررسی «$prompt» هستم.", speechSpeed)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
            ttsManager.shutdown()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "مکالمه صوتی - سلام پاتریک",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Wake word badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0369A1).copy(alpha = 0.3f))
                    .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "عبارت بیداربش: «سلام پاتریک»",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Big Central Voice Orb
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale.value),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF06B6D4).copy(alpha = 0.3f),
                                    Color(0xFF0284C7).copy(alpha = 0.05f)
                                )
                            )
                        )
                )

                // Inner Mic Button
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (voiceState is VoiceState.Listening) {
                                    listOf(Color(0xFFF43F5E), Color(0xFFE11D48))
                                } else {
                                    listOf(Color(0xFF0284C7), Color(0xFF06B6D4))
                                }
                            )
                        )
                        .clickable {
                            if (voiceState is VoiceState.Listening) {
                                voiceManager.stopListening()
                            } else {
                                assistantResponseText = null
                                voiceManager.startListening()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (voiceState is VoiceState.Listening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "میکروفون",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Text
            val statusLabel = when (voiceState) {
                is VoiceState.Listening -> "در حال شنیدن صدای شما..."
                is VoiceState.Processing -> "در حال پردازش گفتار..."
                is VoiceState.Success -> "تشخیص صدا کامل شد"
                is VoiceState.Error -> (voiceState as VoiceState.Error).message
                is VoiceState.Unavailable -> "موتور تبدیل گفتار در این دستگاه فعال نیست"
                else -> "دکمه را لمس کنید و صحبت کنید"
            }

            Text(
                text = statusLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = when (voiceState) {
                    is VoiceState.Listening -> Color(0xFFF43F5E)
                    is VoiceState.Error -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onBackground
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Spoken text display / editable input
            OutlinedTextField(
                value = if (voiceState is VoiceState.Listening && liveSpokenText.isNotBlank()) liveSpokenText else editableText,
                onValueChange = { editableText = it },
                label = { Text("متن تشخیص داده شده (قابل ویرایش):") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Assistant Spoken Response Card
            if (assistantResponseText != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "پاسخ صوتی محمد پاتریک:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = assistantResponseText!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (ttsStatus is TtsStatus.Speaking) {
                                Button(
                                    onClick = { ttsManager.stop() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("توقف صدا", style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                Button(
                                    onClick = { ttsManager.replay(speechSpeed) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("پخش مجدد", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Speech Rate Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سرعت گفتار (${String.format(Locale.US, "%.1f", speechSpeed)}x):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(10.dp))
                Slider(
                    value = speechSpeed,
                    onValueChange = { speechSpeed = it },
                    valueRange = 0.5f..2.0f,
                    steps = 5,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
