package com.mohammadpatrick.assistant.domain.model

sealed class SystemAction(val label: String, val description: String) {
    object ShowDateTime : SystemAction("نمایش تاریخ و ساعت", "نمایش زمان جاری به هجری شمسی و میلادی")
    object OpenWifiSettings : SystemAction("تنظیمات وای‌فای", "باز کردن صفحه پیکربندی شبکه بی‌سیم")
    object OpenBluetoothSettings : SystemAction("تنظیمات بلوتوث", "باز کردن صفحه اتصالات بلوتوث")
    object OpenSoundSettings : SystemAction("تنظیمات صدا", "تنظیم بلندی صدای رسانه و زنگ")
    object OpenAppSettings : SystemAction("دسترسی‌های برنامه", "مدیریت مجوزها در تنظیمات سیستم")
    object OpenSamsungBatteryGuide : SystemAction("بهینه‌سازی سامسونگ A23", "تنظیم باتری بدون محدودیت در One UI")
    data class OpenWebLink(val url: String) : SystemAction("باز کردن پیوند وب", url)
    data class ShareText(val text: String) : SystemAction("اشتراک‌گذاری متن", "ارسال متن به سایر برنامه‌ها")
    data class LaunchApp(val packageName: String, val appName: String) : SystemAction("اجرای برنامه", appName)
}
