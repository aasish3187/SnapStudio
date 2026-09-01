package com.snapstudio.app.studio

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.ui.geometry.Offset
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.max

/**
 * High-performance non-destructive selective mask engine.
 * Stamps soft circular dabs with Catmull-Rom/linear sub-pixel interpolation
 * into an offscreen alpha mask buffer.
 */
class SelectiveMaskEngine(
    var width: Int = 1080,
    var height: Int = 1080
) {
    var maskBitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        private set
    private var maskCanvas: Canvas = Canvas(maskBitmap)

    private var lastPoint: Offset? = null

    fun resizeIfNeeded(newWidth: Int, newHeight: Int) {
        if (newWidth <= 0 || newHeight <= 0) return
        if (newWidth != width || newHeight != height) {
            val oldBitmap = maskBitmap
            width = newWidth
            height = newHeight
            maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            maskCanvas = Canvas(maskBitmap)
            if (!oldBitmap.isRecycled) {
                val srcRect = android.graphics.Rect(0, 0, oldBitmap.width, oldBitmap.height)
                val dstRect = android.graphics.Rect(0, 0, width, height)
                maskCanvas.drawBitmap(oldBitmap, srcRect, dstRect, null)
                oldBitmap.recycle()
            }
        }
    }

    fun startStroke(point: Offset, radius: Float, hardness: Float, opacity: Float, isErase: Boolean) {
        lastPoint = point
        stampDab(point.x, point.y, radius, hardness, opacity, isErase)
    }

    fun continueStroke(currentPoint: Offset, radius: Float, hardness: Float, opacity: Float, isErase: Boolean) {
        val start = lastPoint ?: currentPoint
        val distance = hypot(currentPoint.x - start.x, currentPoint.y - start.y)
        
        // Spacing: 15% of diameter (0.3 * radius) to avoid scalloping
        val step = max(1f, radius * 0.25f)
        val steps = ceil(distance / step).toInt()

        if (steps > 0) {
            for (i in 1..steps) {
                val t = i.toFloat() / steps
                val x = start.x + (currentPoint.x - start.x) * t
                val y = start.y + (currentPoint.y - start.y) * t
                stampDab(x, y, radius, hardness, opacity, isErase)
            }
        } else {
            stampDab(currentPoint.x, currentPoint.y, radius, hardness, opacity, isErase)
        }
        lastPoint = currentPoint
    }

    fun endStroke() {
        lastPoint = null
    }

    private fun stampDab(
        x: Float,
        y: Float,
        radius: Float,
        hardness: Float,
        opacity: Float,
        isErase: Boolean
    ) {
        val safeRadius = max(2f, radius)
        val safeHardness = hardness.coerceIn(0.01f, 0.95f)

        val colors = if (isErase) {
            intArrayOf(Color.BLACK, Color.BLACK, Color.TRANSPARENT)
        } else {
            intArrayOf(Color.WHITE, Color.WHITE, Color.TRANSPARENT)
        }
        val stops = floatArrayOf(0f, safeHardness, 1f)

        val gradient = RadialGradient(
            x, y, safeRadius,
            colors, stops,
            Shader.TileMode.CLAMP
        )

        val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
            shader = gradient
            alpha = (opacity.coerceIn(0.05f, 1f) * 255).toInt()
            if (isErase) {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            }
        }

        maskCanvas.drawCircle(x, y, safeRadius, paint)
    }

    fun clear() {
        maskBitmap.eraseColor(Color.TRANSPARENT)
        lastPoint = null
    }

    fun invert() {
        val inverted = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(inverted)
        canvas.drawColor(Color.WHITE)
        val paint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        }
        canvas.drawBitmap(maskBitmap, 0f, 0f, paint)
        maskBitmap.recycle()
        maskBitmap = inverted
        maskCanvas = Canvas(maskBitmap)
    }

    fun copyMask(): Bitmap {
        return maskBitmap.copy(Bitmap.Config.ARGB_8888, true)
    }

    fun restoreMask(bitmap: Bitmap) {
        maskBitmap.recycle()
        maskBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        width = maskBitmap.width
        height = maskBitmap.height
        maskCanvas = Canvas(maskBitmap)
    }
}
