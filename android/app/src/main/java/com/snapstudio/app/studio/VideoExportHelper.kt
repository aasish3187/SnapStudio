package com.snapstudio.app.studio

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.transformer.Composition
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

object VideoExportHelper {

    suspend fun exportVideo(
        context: Context,
        inputUri: Uri,
        trimStartMs: Long,
        trimEndMs: Long,
        targetHeight: Int = -1,
        colorMatrix: FloatArray? = null,
        overlays: List<OverlayItem> = emptyList(),
        onSuccess: (Uri) -> Unit,
        onError: (Exception) -> Unit
    ) = withContext(Dispatchers.Main) {
        
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(System.currentTimeMillis())
        val outputFile = File(context.cacheDir, "SnapStudio_Export_$name.mp4")

        val mediaItemBuilder = MediaItem.Builder().setUri(inputUri)
        
        if (trimEndMs > 0 && trimEndMs > trimStartMs) {
            mediaItemBuilder.setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(trimStartMs)
                    .setEndPositionMs(trimEndMs)
                    .build()
            )
        }
        
        val mediaItem = mediaItemBuilder.build()

        val editedMediaItemBuilder = androidx.media3.transformer.EditedMediaItem.Builder(mediaItem)
        val videoEffects = mutableListOf<androidx.media3.common.Effect>()
        
        if (targetHeight > 0) {
            videoEffects.add(androidx.media3.effect.Presentation.createForHeight(targetHeight))
        }
        
        if (colorMatrix != null) {
            videoEffects.add(com.snapstudio.app.filters.VideoFilterEffect(colorMatrix))
        }
        
        if (overlays.isNotEmpty()) {
            var videoWidth = 1080
            var videoHeight = 1920
            try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(context, inputUri)
                val widthStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                val heightStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                val rotStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                retriever.release()
                if (widthStr != null && heightStr != null) {
                    val w = widthStr.toInt()
                    val h = heightStr.toInt()
                    val rot = rotStr?.toIntOrNull() ?: 0
                    if (rot == 90 || rot == 270) {
                        videoWidth = h
                        videoHeight = w
                    } else {
                        videoWidth = w
                        videoHeight = h
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            val resultBitmap = android.graphics.Bitmap.createBitmap(videoWidth, videoHeight, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(resultBitmap)
            val uiScaleToBitmap = Math.max(videoWidth / 1080f, 1f)
            val textPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            overlays.forEach { overlay ->
                canvas.save()
                canvas.translate(
                    videoWidth / 2f + overlay.x * uiScaleToBitmap,
                    videoHeight / 2f + overlay.y * uiScaleToBitmap
                )
                canvas.rotate(overlay.rotation)
                canvas.scale(overlay.scale, overlay.scale)
                
                if (overlay.type == OverlayType.TEXT) {
                    textPaint.color = android.graphics.Color.WHITE
                    textPaint.textSize = 32f * 3f * uiScaleToBitmap
                    textPaint.setShadowLayer(4f * uiScaleToBitmap, 0f, 0f, android.graphics.Color.BLACK)
                    canvas.drawText(overlay.content, 0f, textPaint.textSize / 3f, textPaint)
                } else if (overlay.type == OverlayType.STICKER) {
                    textPaint.textSize = 64f * 3f * uiScaleToBitmap
                    textPaint.clearShadowLayer()
                    canvas.drawText(overlay.content, 0f, textPaint.textSize / 3f, textPaint)
                }
                
                canvas.restore()
            }

            val videoOverlayEffect = androidx.media3.effect.BitmapOverlay.createStaticBitmapOverlay(resultBitmap)
            val overlayEffect = androidx.media3.effect.OverlayEffect(
                com.google.common.collect.ImmutableList.of(videoOverlayEffect)
            )
            videoEffects.add(overlayEffect)
        }
        
        if (videoEffects.isNotEmpty()) {
            editedMediaItemBuilder.setEffects(
                androidx.media3.transformer.Effects(emptyList(), videoEffects)
            )
        }
        val editedMediaItem = editedMediaItemBuilder.build()

        val transformer = Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    // Copy from cache to MediaStore
                    val contentValues = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "SnapStudio-Edit-${System.currentTimeMillis()}.mp4")
                        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Movies/SnapStudio")
                        }
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        try {
                            resolver.openOutputStream(uri)?.use { outStream ->
                                outputFile.inputStream().use { inStream ->
                                    inStream.copyTo(outStream)
                                }
                            }
                            // Delete cached file
                            outputFile.delete()
                            onSuccess(uri)
                        } catch (e: Exception) {
                            onError(e)
                        }
                    } else {
                        onSuccess(Uri.fromFile(outputFile))
                    }
                }

                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    onError(exportException)
                }
            })
            .build()

        transformer.start(editedMediaItem, outputFile.absolutePath)
    }
}
