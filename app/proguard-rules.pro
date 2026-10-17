# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/.../build/intermediates/proguard-files/.../proguard-android.txt

# Keep data classes and Room
-keep class com.mohammadpatrick.assistant.domain.model.** { *; }
-keep class com.mohammadpatrick.assistant.data.local.db.entity.** { *; }
-keep class com.mohammadpatrick.assistant.data.remote.dto.** { *; }

# Retrofit & Moshi
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
