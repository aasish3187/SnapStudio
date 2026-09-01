package com.snapstudio.app

import android.app.Application
import android.util.Log
import com.snapstudio.app.gallery.GalleryThumbnailLoader
import com.snapstudio.app.studio.AiFaceMeshEngine
import com.snapstudio.app.studio.AiSegmentationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SnapStudioApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        
        // 1. Production Global Uncaught Exception Handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("SnapStudio", "FATAL EXCEPTION in thread ${thread.name}: ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // 2. Pre-warm on-device AI models in background thread for zero-latency tool switching
        applicationScope.launch {
            try {
                // Initialize ML Kit selfie segmenter & face mesh pipeline
                AiSegmentationEngine
                AiFaceMeshEngine
                Log.d("SnapStudio", "On-Device AI Engines initialized successfully.")
            } catch (e: Throwable) {
                Log.w("SnapStudio", "Background AI pre-warming encountered: ${e.message}")
            }
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_RUNNING_LOW || level >= TRIM_MEMORY_MODERATE) {
            GalleryThumbnailLoader.cache.evictAll()
            Log.d("SnapStudio", "TrimMemory ($level): Evicted Gallery LRU thumbnail cache.")
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        GalleryThumbnailLoader.cache.evictAll()
        System.gc()
        Log.d("SnapStudio", "onLowMemory: Evicted all in-memory bitmap caches.")
    }
}
