package com.snapstudio.app.camera

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Locale

import com.snapstudio.app.filters.FilterPreset

object PhotoCaptureHelper {

    fun takePhoto(
        context: Context,
        imageCapture: ImageCapture,
        activeFilter: FilterPreset?,
        saveToGallery: Boolean,
        onPhotoCaptured: (String) -> Unit, // Returns the content URI string
        onError: (ImageCaptureException) -> Unit
    ) {
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())

        // We capture to memory, then apply filter, then save
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: androidx.camera.core.ImageProxy) {
                    val buffer = imageProxy.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    
                    var bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    
                    // Rotate based on ImageProxy rotation
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    if (rotationDegrees != 0) {
                        val matrix = android.graphics.Matrix()
                        matrix.postRotate(rotationDegrees.toFloat())
                        bitmap = android.graphics.Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    }
                    imageProxy.close()

                    // Apply Filter Matrix if available (only supports Tier 1 offline right now)
                    if (activeFilter is FilterPreset.ColorMatrix) {
                        val filteredBitmap = android.graphics.Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
                        val canvas = android.graphics.Canvas(filteredBitmap)
                        val paint = android.graphics.Paint()
                        paint.colorFilter = android.graphics.ColorMatrixColorFilter(android.graphics.ColorMatrix(activeFilter.matrix))
                        canvas.drawBitmap(bitmap, 0f, 0f, paint)
                        bitmap = filteredBitmap
                    }
                    
                    // Save to MediaStore or Cache
                    if (saveToGallery) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, "SnapStudio-$name.jpg")
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/SnapStudio")
                                put(MediaStore.MediaColumns.IS_PENDING, 1)
                            }
                        }
                        val resolver = context.contentResolver
                        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        if (uri != null) {
                            try {
                                resolver.openOutputStream(uri)?.use { out ->
                                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, out)
                                }
                                
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    val updateValues = ContentValues().apply {
                                        put(MediaStore.MediaColumns.IS_PENDING, 0)
                                    }
                                    resolver.update(uri, updateValues, null, null)
                                } else {
                                    // For older devices, we need to manually trigger the media scanner
                                    // But we don't have the absolute path from the content resolver insert easily.
                                    // The insert itself usually registers it on < Q, but just in case.
                                }
                                
                                Log.d("PhotoCaptureHelper", "Photo capture succeeded: $uri")
                                onPhotoCaptured(uri.toString())
                            } catch (e: Exception) {
                                Log.e("PhotoCaptureHelper", "Failed to write bitmap", e)
                                onError(ImageCaptureException(ImageCapture.ERROR_UNKNOWN, "Failed to write bitmap", e))
                            }
                        } else {
                            onError(ImageCaptureException(ImageCapture.ERROR_UNKNOWN, "Failed to create MediaStore entry", null))
                        }
                    } else {
                        // Save to cache
                        val cacheFile = java.io.File(context.cacheDir, "SnapStudio-$name.jpg")
                        try {
                            cacheFile.outputStream().use { out ->
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, out)
                            }
                            val uri = android.net.Uri.fromFile(cacheFile)
                            Log.d("PhotoCaptureHelper", "Photo capture cached: $uri")
                            onPhotoCaptured(uri.toString())
                        } catch (e: Exception) {
                            Log.e("PhotoCaptureHelper", "Failed to write bitmap to cache", e)
                            onError(ImageCaptureException(ImageCapture.ERROR_UNKNOWN, "Failed to write bitmap to cache", e))
                        }
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("PhotoCaptureHelper", "Photo capture failed: ${exc.message}", exc)
                    onError(exc)
                }
            }
        )
    }
}

