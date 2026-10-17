package com.mohammadpatrick.assistant.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class TtsStatus {
    object Idle : TtsStatus()
    object Speaking : TtsStatus()
    data class Error(val message: String) : TtsStatus()
    object UnsupportedLanguage : TtsStatus()
}

class PersianTtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var lastSpokenText: String = ""

    private val _ttsStatus = MutableStateFlow<TtsStatus>(TtsStatus.Idle)
    val ttsStatus = _ttsStatus.asStateFlow()

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val faLocale = Locale("fa", "IR")
            val langResult = tts?.setLanguage(faLocale)

            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Try generic Persian or notify user
                val fallbackLocale = Locale("fa")
                val fallbackResult = tts?.setLanguage(fallbackLocale)
                if (fallbackResult == TextToSpeech.LANG_MISSING_DATA || fallbackResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    _ttsStatus.value = TtsStatus.UnsupportedLanguage
                    isInitialized = false
                    return
                }
            }

            // Configure deep male voice tone requested by Mohammad Parsa
            tts?.setPitch(0.82f)

            // Select male voice if available in voice pack
            try {
                val voices = tts?.voices
                val maleVoice = voices?.firstOrNull { v ->
                    (v.name.contains("male", ignoreCase = true) || v.name.contains("man", ignoreCase = true)) &&
                    (v.locale.language == "fa" || v.locale.language == "ir")
                } ?: voices?.firstOrNull { it.locale.language == "fa" }
                if (maleVoice != null) {
                    tts?.voice = maleVoice
                }
            } catch (e: Exception) {
                // Fallback to default Persian voice with 0.82f male pitch
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _ttsStatus.value = TtsStatus.Speaking
                }

                override fun onDone(utteranceId: String?) {
                    _ttsStatus.value = TtsStatus.Idle
                }

                override fun onError(utteranceId: String?) {
                    _ttsStatus.value = TtsStatus.Error("خطا در ادای صوتی متن")
                }
            })

            isInitialized = true
            _ttsStatus.value = TtsStatus.Idle
        } else {
            _ttsStatus.value = TtsStatus.Error("راه‌اندازی موتور TTS با خطا مواجه شد.")
            isInitialized = false
        }
    }

    fun speak(text: String, speechRate: Float = 1.0f) {
        if (!isInitialized || tts == null) {
            _ttsStatus.value = TtsStatus.Error("موتور صوتی آماده نیست یا زبان فارسی نصب نشده است.")
            return
        }

        lastSpokenText = text
        tts?.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_${System.currentTimeMillis()}")
    }

    fun replay(speechRate: Float = 1.0f) {
        if (lastSpokenText.isNotBlank()) {
            speak(lastSpokenText, speechRate)
        }
    }

    fun stop() {
        tts?.stop()
        _ttsStatus.value = TtsStatus.Idle
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
