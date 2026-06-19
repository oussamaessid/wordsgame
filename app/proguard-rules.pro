# Add project specific ProGuard rules here.

# Stack traces lisibles
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# AdMob / Google Mobile Ads
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# JSON parsing (org.json)
-keep class org.json.** { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**