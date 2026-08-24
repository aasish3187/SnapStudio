package com.snapstudio.app.settings

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("snapstudio_settings", Context.MODE_PRIVATE)

    var defaultLensFacing: Int
        get() = prefs.getInt("default_lens_facing", androidx.camera.core.CameraSelector.LENS_FACING_BACK)
        set(value) = prefs.edit().putInt("default_lens_facing", value).apply()

    var showGridLines: Boolean
        get() = prefs.getBoolean("show_grid_lines", false)
        set(value) = prefs.edit().putBoolean("show_grid_lines", value).apply()

    var saveOriginals: Boolean
        get() = prefs.getBoolean("save_originals", false)
        set(value) = prefs.edit().putBoolean("save_originals", value).apply()

    var videoExportQuality: Int
        get() = prefs.getInt("video_export_quality", 1080) // 1080, 720, 480
        set(value) = prefs.edit().putInt("video_export_quality", value).apply()

    var addWatermark: Boolean
        get() = prefs.getBoolean("add_watermark", false)
        set(value) = prefs.edit().putBoolean("add_watermark", value).apply()

    var hapticFeedback: Boolean
        get() = prefs.getBoolean("haptic_feedback", true)
        set(value) = prefs.edit().putBoolean("haptic_feedback", value).apply()

    var autoSaveToGallery: Boolean
        get() = prefs.getBoolean("auto_save", true)
        set(value) = prefs.edit().putBoolean("auto_save", value).apply()

    fun clearCache(context: Context) {
        val cacheDir = context.cacheDir
        cacheDir.deleteRecursively()
        cacheDir.mkdir()
    }
}
