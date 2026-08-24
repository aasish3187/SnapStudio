package com.snapstudio.app.studio

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ExportHelper {
    suspend fun flattenAndExport(
        context: Context,
        sourceBitmap: Bitmap,
        colorMatrixArray: FloatArray?,
        overlays: List<OverlayItem>,
        vignetteStrength: Float = 0f,
        frameStyle: String = "none"
    ): Uri? {
        return withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver

            val resultBitmap = Bitmap.createBitmap(
                sourceBitmap.width,
                sourceBitmap.height,
                sourceBitmap.config ?: Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(resultBitmap)
            val paint = Paint()

            // Construct ColorMatrix
            val colorMatrix = if (colorMatrixArray != null) ColorMatrix(colorMatrixArray) else ColorMatrix()

            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(sourceBitmap, 0f, 0f, paint)
            
            // Draw Overlays
            // Map UI coordinates to Bitmap coordinates roughly.
            // In a real app we'd measure the Box size to get exact UI scale. 
            // For now, assume a 1080p screen and a standard scale factor of ~3x
            val uiScaleToBitmap = Math.max(sourceBitmap.width / 1080f, 1f)
            
            val textPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
            
            overlays.forEach { overlay ->
                canvas.save()
                // Move to center of bitmap, then apply offset scaled up
                canvas.translate(
                    sourceBitmap.width / 2f + overlay.x * uiScaleToBitmap,
                    sourceBitmap.height / 2f + overlay.y * uiScaleToBitmap
                )
                // Compose rotation is in degrees clockwise
                canvas.rotate(overlay.rotation)
                canvas.scale(overlay.scale, overlay.scale)
                
                if (overlay.type == OverlayType.TEXT) {
                    val fontFam = when(overlay.fontFamily) {
                        "serif" -> android.graphics.Typeface.SERIF
                        "mono" -> android.graphics.Typeface.MONOSPACE
                        "cursive" -> android.graphics.Typeface.SERIF // Cursive doesn't exist natively, fallback to Serif
                        else -> android.graphics.Typeface.SANS_SERIF
                    }
                    textPaint.typeface = android.graphics.Typeface.create(fontFam, android.graphics.Typeface.BOLD)
                    
                    textPaint.color = android.graphics.Color.argb(
                        (overlay.color.alpha * 255).toInt(),
                        (overlay.color.red * 255).toInt(),
                        (overlay.color.green * 255).toInt(),
                        (overlay.color.blue * 255).toInt()
                    )
                    textPaint.textSize = 48f * 3f * uiScaleToBitmap // matching 48.sp
                    textPaint.clearShadowLayer() // No shadow
                    
                    val textWidth = textPaint.measureText(overlay.content)
                    val textHeight = textPaint.textSize
                    
                    if (overlay.bgStyle != "none") {
                        val bgPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(128, 0, 0, 0)
                            isAntiAlias = true
                        }
                        val paddingX = 12f * 3f * uiScaleToBitmap
                        val paddingY = 8f * 3f * uiScaleToBitmap
                        val rect = android.graphics.RectF(
                            -textWidth/2f - paddingX,
                            -textHeight/1.2f - paddingY,
                            textWidth/2f + paddingX,
                            textHeight/4f + paddingY
                        )
                        if (overlay.bgStyle == "rounded") {
                            canvas.drawRoundRect(rect, 12f * 3f * uiScaleToBitmap, 12f * 3f * uiScaleToBitmap, bgPaint)
                        } else {
                            canvas.drawRect(rect, bgPaint)
                        }
                    }
                    
                    canvas.drawText(overlay.content, 0f, 0f, textPaint)
                } else if (overlay.type == OverlayType.STICKER) {
                    textPaint.textSize = 20f * 3f * uiScaleToBitmap
                    textPaint.typeface = android.graphics.Typeface.DEFAULT_BOLD
                    textPaint.color = android.graphics.Color.WHITE
                    textPaint.clearShadowLayer()
                    val textWidth = textPaint.measureText(overlay.content)
                    val textHeight = textPaint.textSize
                    val paddingX = 10f * 3f * uiScaleToBitmap
                    val paddingY = 6f * 3f * uiScaleToBitmap
                    val bgPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(160, 0, 0, 0)
                        style = android.graphics.Paint.Style.FILL
                        isAntiAlias = true
                    }
                    val borderPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 1.5f * 3f * uiScaleToBitmap
                        isAntiAlias = true
                    }
                    val rect = android.graphics.RectF(
                        -textWidth/2f - paddingX,
                        -textHeight/1.2f - paddingY,
                        textWidth/2f + paddingX,
                        textHeight/4f + paddingY
                    )
                    canvas.drawRoundRect(rect, 6f * 3f * uiScaleToBitmap, 6f * 3f * uiScaleToBitmap, bgPaint)
                    canvas.drawRoundRect(rect, 6f * 3f * uiScaleToBitmap, 6f * 3f * uiScaleToBitmap, borderPaint)
                    canvas.drawText(overlay.content, -textWidth/2f, 0f, textPaint)
                } else if (overlay.type == OverlayType.IMAGE_STICKER) {
                    try {
                        val stickerUri = Uri.parse(overlay.content)
                        val stream = context.contentResolver.openInputStream(stickerUri)
                        val stickerBmp = BitmapFactory.decodeStream(stream)
                        stream?.close()
                        if (stickerBmp != null) {
                            val targetW = 120f * 3f * uiScaleToBitmap
                            val targetH = (stickerBmp.height.toFloat() / stickerBmp.width.toFloat()) * targetW
                            val rect = android.graphics.RectF(-targetW / 2f, -targetH / 2f, targetW / 2f, targetH / 2f)
                            canvas.drawBitmap(stickerBmp, null, rect, null)
                            stickerBmp.recycle()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                canvas.restore()
            }
            
            if (vignetteStrength > 0f) {
                val cx = sourceBitmap.width / 2f
                val cy = sourceBitmap.height / 2f
                val radius = Math.min(sourceBitmap.width, sourceBitmap.height) / 1.2f
                val colors = intArrayOf(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.argb((vignetteStrength * 255).toInt(), 0, 0, 0)
                )
                val radialGradient = android.graphics.RadialGradient(
                    cx, cy, radius, colors, null, android.graphics.Shader.TileMode.CLAMP
                )
                val vignettePaint = android.graphics.Paint().apply {
                    shader = radialGradient
                }
                canvas.drawRect(0f, 0f, sourceBitmap.width.toFloat(), sourceBitmap.height.toFloat(), vignettePaint)
            }
            
            // Draw frames
            if (frameStyle != "none") {
                val paint = android.graphics.Paint()
                val w = sourceBitmap.width.toFloat()
                val h = sourceBitmap.height.toFloat()
                if (frameStyle == "white_border") {
                    paint.color = android.graphics.Color.WHITE
                    val borderW = w * 0.05f
                    val borderH = h * 0.05f
                    canvas.drawRect(0f, 0f, w, borderH, paint)
                    canvas.drawRect(0f, h - borderH, w, h, paint)
                    canvas.drawRect(0f, 0f, borderW, h, paint)
                    canvas.drawRect(w - borderW, 0f, w, h, paint)
                } else if (frameStyle == "cinematic") {
                    paint.color = android.graphics.Color.BLACK
                    val barH = h * 0.15f
                    canvas.drawRect(0f, 0f, w, barH, paint)
                    canvas.drawRect(0f, h - barH, w, h, paint)
                } else if (frameStyle == "polaroid") {
                    paint.color = android.graphics.Color.WHITE
                    val borderW = w * 0.05f
                    val borderTop = h * 0.05f
                    val borderBottom = h * 0.20f
                    canvas.drawRect(0f, 0f, w, borderTop, paint)
                    canvas.drawRect(0f, h - borderBottom, w, h, paint)
                    canvas.drawRect(0f, 0f, borderW, h, paint)
                    canvas.drawRect(w - borderW, 0f, w, h, paint)
                }
            }

            // Save to MediaStore (Save as Copy)
            val name = "SnapStudio-Edit-${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/SnapStudio")
                }
            }

            val savedUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (savedUri != null) {
                contentResolver.openOutputStream(savedUri)?.use { out ->
                    resultBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
            }

            // Cleanup
            resultBitmap.recycle()

            savedUri
        }
    }
}

