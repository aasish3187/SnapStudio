# SnapStudio Production ProGuard / R8 Rules

# 1. Native C++ & JNI Bridge
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.snapstudio.editing.** { *; }

# 2. Google ML Kit On-Device AI
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# 3. AndroidX CameraX & Media3 Transformer
-keep class androidx.camera.** { *; }
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-dontwarn androidx.camera.**

# 4. Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# 5. Coil Image Loader & OkHttp
-keep class coil.** { *; }
-dontwarn coil.**
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# 6. Kotlin Coroutines & Reflection
-keepnames class kotlinx.coroutines.** { *; }
-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
