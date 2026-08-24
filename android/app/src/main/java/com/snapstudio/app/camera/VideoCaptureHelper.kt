package com.snapstudio.app.camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

class VideoCaptureHelper(private val context: Context) {
    val videoCapture: VideoCapture<Recorder>
    private var activeRecording: Recording? = null
    
    init {
        val qualitySelector = QualitySelector.fromOrderedList(
            listOf(Quality.FHD, Quality.HD, Quality.SD),
            FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
        )
        val recorder = Recorder.Builder()
            .setExecutor(ContextCompat.getMainExecutor(context))
            .setQualitySelector(qualitySelector)
            .build()
        videoCapture = VideoCapture.withOutput(recorder)
    }

    fun startRecording(
        executor: Executor,
        onVideoSaved: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "SnapStudio-$name.mp4")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/SnapStudio")
            }
        }

        val outputOptions = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        val pendingRecording = videoCapture.output.prepareRecording(context, outputOptions)

        try {
            activeRecording = pendingRecording.start(executor, Consumer { event ->
                when (event) {
                    is VideoRecordEvent.Finalize -> {
                        if (!event.hasError()) {
                            val savedUri = event.outputResults.outputUri
                            onVideoSaved(savedUri)
                        } else {
                            activeRecording?.close()
                            activeRecording = null
                            onError("Video recording error (${event.error})")
                        }
                    }
                    is VideoRecordEvent.Status -> {
                        val durationNs = event.recordingStats.recordedDurationNanos
                        if (durationNs >= 60_000_000_000L) { // 60 seconds limit
                            stopRecording()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            onError("Failed to start recording: ${e.message}")
        }
    }

    fun stopRecording() {
        try {
            activeRecording?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        activeRecording = null
    }
}


