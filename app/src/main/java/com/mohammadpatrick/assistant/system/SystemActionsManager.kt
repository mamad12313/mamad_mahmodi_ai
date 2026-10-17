package com.mohammadpatrick.assistant.system

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import com.mohammadpatrick.assistant.domain.model.SystemAction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SystemActionsManager(private val context: Context) {

    fun executeAction(action: SystemAction): Boolean {
        return try {
            when (action) {
                is SystemAction.ShowDateTime -> true // Handled by UI dialog/toast
                is SystemAction.OpenWifiSettings -> openWifiSettings()
                is SystemAction.OpenBluetoothSettings -> openBluetoothSettings()
                is SystemAction.OpenSoundSettings -> openSoundSettings()
                is SystemAction.OpenAppSettings -> openAppSettings()
                is SystemAction.OpenSamsungBatteryGuide -> true // UI navigation
                is SystemAction.OpenWebLink -> openWebLink(action.url)
                is SystemAction.ShareText -> shareText(action.text)
                is SystemAction.LaunchApp -> launchApp(action.packageName)
            }
        } catch (e: Exception) {
            false
        }
    }

    fun openWifiSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openGeneralSettings()
        }
    }

    fun openBluetoothSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openGeneralSettings()
        }
    }

    fun openSoundSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openGeneralSettings()
        }
    }

    fun openAppSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openGeneralSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openWebLink(url: String): Boolean {
        return try {
            val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else url
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun shareText(text: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val chooser = Intent.createChooser(intent, "اشتراک‌گذاری با:")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun copyToClipboard(text: String, label: String = "MohammadPatrick") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun adjustMediaVolume(raise: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val direction = if (raise) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
    }

    fun getFormattedPersianDateTime(): PersianDateTimeInfo {
        val now = Date()
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val gregorianFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

        val timeStr = timeFormat.format(now)
        val gregorianDateStr = gregorianFormat.format(now)

        // Convert to Persian Solar Calendar (Jalali approximation algorithm)
        val persianDateStr = JalaliCalendarConverter.getTodayPersianDate()

        return PersianDateTimeInfo(
            time = timeStr,
            persianDate = persianDateStr,
            gregorianDate = gregorianDateStr
        )
    }
}

data class PersianDateTimeInfo(
    val time: String,
    val persianDate: String,
    val gregorianDate: String
)

/**
 * Jalali (Solar Hijri) Calendar converter algorithm
 */
object JalaliCalendarConverter {
    fun getTodayPersianDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val gYear = calendar.get(java.util.Calendar.YEAR)
        val gMonth = calendar.get(java.util.Calendar.MONTH) + 1
        val gDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(0, 31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy = gYear - 1600
        var gm = gMonth - 1
        var gd = gDay - 1

        var gDayNo = 365 * gy + ((gy + 3) / 4) - ((gy + 99) / 100) + ((gy + 399) / 400)
        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i + 1]
        }
        if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += ((jDayNo - 1) / 365)
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        var jd = 0
        for (i in 0 until 11) {
            if (jDayNo < jDaysInMonth[i + 1]) {
                jm = i + 1
                jd = jDayNo + 1
                break
            }
            jDayNo -= jDaysInMonth[i + 1]
        }
        if (jm == 0) {
            jm = 12
            jd = jDayNo + 1
        }

        val persianMonthNames = arrayOf(
            "", "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
        )

        return "$jd ${persianMonthNames[jm]} $jy"
    }
}
