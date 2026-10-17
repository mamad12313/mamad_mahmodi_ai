package com.mohammadpatrick.assistant.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    object Processing : VoiceState()
    data class Success(val recognizedText: String, val isWakeWordDetected: Boolean) : VoiceState()
    data class Error(val message: String, val errorCode: Int) : VoiceState()
    object Unavailable : VoiceState()
}

class VoiceManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState = _voiceState.asStateFlow()

    private val _liveSpokenText = MutableStateFlow("")
    val liveSpokenText = _liveSpokenText.asStateFlow()

    private val wakePhraseTarget = "M.H.O.I"

    init {
        initializeRecognizer()
    }

    fun isSpeechRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    private fun initializeRecognizer() {
        if (!isSpeechRecognitionAvailable()) {
            _voiceState.value = VoiceState.Unavailable
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }
        } catch (e: Exception) {
            _voiceState.value = VoiceState.Error("خطا در راه‌اندازی موتور تشخیص گفتار: ${e.message}", -1)
        }
    }

    fun startListening() {
        if (!isSpeechRecognitionAvailable()) {
            _voiceState.value = VoiceState.Unavailable
            return
        }

        if (speechRecognizer == null) {
            initializeRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fa-IR")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "fa-IR")
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            _liveSpokenText.value = ""
            _voiceState.value = VoiceState.Listening
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _voiceState.value = VoiceState.Error("امکان شروع شنیدن صدا وجود ندارد: ${e.message}", -1)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            if (_voiceState.value is VoiceState.Listening) {
                _voiceState.value = VoiceState.Processing
            }
        } catch (e: Exception) {
            _voiceState.value = VoiceState.Idle
        }
    }

    fun cancel() {
        try {
            speechRecognizer?.cancel()
            _voiceState.value = VoiceState.Idle
            _liveSpokenText.value = ""
        } catch (e: Exception) {
            // Ignore cancel error
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            _voiceState.value = VoiceState.Idle
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _voiceState.value = VoiceState.Listening
            }

            override fun onBeginningOfSpeech() {
                _voiceState.value = VoiceState.Listening
            }

            override fun onRmsChanged(rmsdB: Float) {
                // RMS audio volume change
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _voiceState.value = VoiceState.Processing
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "خطای ضبط صدا از میکروفون"
                    SpeechRecognizer.ERROR_CLIENT -> "خطای کلاینت صوتی دستگاه"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "دسترسی به میکروفون داده نشده است"
                    SpeechRecognizer.ERROR_NETWORK -> "خطای شبکه در ارتباط با سرور تبدیل صوت"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "مهلت زمانی شبکه به پایان رسید"
                    SpeechRecognizer.ERROR_NO_MATCH -> "صدایی با مفهوم فارسی تشخیص داده نشد"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "سرویس گفتار در حال حاضر مشغول است"
                    SpeechRecognizer.ERROR_SERVER -> "خطای سرور سرویس گفتار"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "هیچ گفتاری شنیده نشد"
                    else -> "خطای نامشخص صوتی (کد $error)"
                }
                _voiceState.value = VoiceState.Error(errorMsg, error)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull() ?: ""
                _liveSpokenText.value = recognizedText

                val isWakeWord = checkWakeWord(recognizedText)
                _voiceState.value = VoiceState.Success(recognizedText, isWakeWord)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialText = matches?.firstOrNull() ?: ""
                if (partialText.isNotBlank()) {
                    _liveSpokenText.value = partialText
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun checkWakeWord(text: String): Boolean {
        val normalized = text.trim()
            .replace("ي", "ی")
            .replace("ك", "ک")
            .replace("  ", " ")
            .lowercase()
        return normalized.contains("m.h.o.i") ||
                normalized.contains("mhoi") ||
                normalized.contains("ام اچ او آی") ||
                normalized.contains("ام‌اچ‌او‌آی") ||
                normalized.contains("ام اچ او ای") ||
                normalized.contains("ام‌اچ‌او‌ای") ||
                normalized.contains("سلام پاتریک") ||
                normalized.contains("پاتریک")
    }
}
