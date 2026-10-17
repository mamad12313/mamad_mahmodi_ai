package com.mohammadpatrick.assistant.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.mohammadpatrick.assistant.MainActivity
import com.mohammadpatrick.assistant.R
import com.mohammadpatrick.assistant.audio.PersianTtsManager
import com.mohammadpatrick.assistant.audio.VoiceManager
import com.mohammadpatrick.assistant.audio.VoiceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Android Floating Bubble Overlay Service.
 * Displays floating app icon at the screen edge when app is in background.
 * Displays transcription & replies ("جانم محمد" or web search answer) directly on screen.
 * Created by: Mohammad Parsa Mahmoudi
 */
class FloatingOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var voiceManager: VoiceManager? = null
    private var ttsManager: PersianTtsManager? = null

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    companion object {
        const val ACTION_START = "ACTION_START_FLOATING_OVERLAY"
        const val ACTION_STOP = "ACTION_STOP_FLOATING_OVERLAY"

        fun canDrawOverlays(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }

        fun startService(context: Context) {
            if (canDrawOverlays(context)) {
                val intent = Intent(context, FloatingOverlayService::class.java).apply {
                    action = ACTION_START
                }
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, FloatingOverlayService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var transcriptTextView: TextView? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        voiceManager = VoiceManager(applicationContext)
        ttsManager = PersianTtsManager(applicationContext)
        createFloatingView()
        observeVoiceState()
    }

    private fun createFloatingView() {
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 200
        }

        // Programmatically build modern floating view with icon and transcription box
        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(12, 12, 12, 12)
        }

        val iconView = ImageView(this).apply {
            setImageResource(R.drawable.ic_assistant_logo)
            layoutParams = android.widget.LinearLayout.LayoutParams(130, 130)
            setBackgroundResource(android.R.drawable.dialog_holo_dark_frame)
        }

        val tv = TextView(this).apply {
            text = "محمد پاتریک آماده شنیدن..."
            setTextColor(android.graphics.Color.WHITE)
            textSize = 12f
            visibility = View.GONE
            setPadding(16, 8, 16, 8)
            setBackgroundColor(android.graphics.Color.parseColor("#E6090D16"))
        }
        transcriptTextView = tv

        container.addView(tv)
        container.addView(iconView)

        // Drag & Click listeners
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        iconView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(container, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = Math.abs(event.rawX - initialTouchX)
                    val diffY = Math.abs(event.rawY - initialTouchY)
                    if (diffX < 10 && diffY < 10) {
                        // Clicked bubble: toggle voice or open app
                        transcriptTextView?.visibility = View.VISIBLE
                        transcriptTextView?.text = "در حال شنیدن گفتار شما..."
                        voiceManager?.startListening()
                    }
                    true
                }
                else -> false
            }
        }

        floatingView = container
        try {
            windowManager?.addView(floatingView, params)
        } catch (e: Exception) {
            // Permission or window error
        }
    }

    private fun observeVoiceState() {
        serviceScope.launch {
            voiceManager?.voiceState?.collectLatest { state ->
                when (state) {
                    is VoiceState.Listening -> {
                        transcriptTextView?.visibility = View.VISIBLE
                        transcriptTextView?.text = "در حال شنیدن صدای شما..."
                    }
                    is VoiceState.Success -> {
                        transcriptTextView?.visibility = View.VISIBLE
                        if (state.isWakeWordDetected) {
                            val reply = "جانم محمد"
                            transcriptTextView?.text = "گفتار: ${state.recognizedText}\nپاتریک: $reply"
                            // Speak male voice with pitch 0.82
                            ttsManager?.speak(reply, speechRate = 0.95f)
                        } else {
                            transcriptTextView?.text = "گفتار: ${state.recognizedText}"
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (floatingView != null) {
            windowManager?.removeView(floatingView)
            floatingView = null
        }
        voiceManager?.destroy()
        ttsManager?.shutdown()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
