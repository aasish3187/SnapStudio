package com.snapstudio.app.studio

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.*

object SelectiveAdjustmentCompositor {

    fun createAdjustedColorMatrix(
        exposureEV: Float,
        temperature: Float,
        saturation: Float,
        contrast: Float
    ): ColorMatrix {
        val matrix = ColorMatrix()

        // 1. Saturation
        if (saturation != 1f) {
            val satMatrix = ColorMatrix()
            satMatrix.setSaturation(saturation.coerceIn(0f, 3f))
            matrix.postConcat(satMatrix)
        }

        // 2. Exposure (Photographic EV calculation: 2^EV)
        if (exposureEV != 0f) {
            val scale = 2.0f.pow(exposureEV.coerceIn(-2.5f, 2.5f))
            val expMatrix = ColorMatrix(floatArrayOf(
                scale, 0f, 0f, 0f, 0f,
                0f, scale, 0f, 0f, 0f,
                0f, 0f, scale, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            matrix.postConcat(expMatrix)
        }

        // 3. Temperature (Warmth shift: Red boost, Blue cut)
        if (temperature != 0f) {
            val t = temperature.coerceIn(-1f, 1f)
            val rScale = 1f + t * 0.25f
            val bScale = 1f - t * 0.25f
            val tempMatrix = ColorMatrix(floatArrayOf(
                rScale, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, bScale, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            matrix.postConcat(tempMatrix)
        }

        // 4. Contrast
        if (contrast != 1f) {
            val c = contrast.coerceIn(0.2f, 2.5f)
            val translate = (-0.5f * c + 0.5f) * 255f
            val contrastMatrix = ColorMatrix(floatArrayOf(
                c, 0f, 0f, 0f, translate,
                0f, c, 0f, 0f, translate,
                0f, 0f, c, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            ))
            matrix.postConcat(contrastMatrix)
        }

        return matrix
    }

    /**
     * Composites base image and selective adjustments using maskBitmap as an alpha matte.
     */
    fun composite(
        baseBitmap: Bitmap,
        maskBitmap: Bitmap?,
        exposureEV: Float,
        temperature: Float,
        saturation: Float,
        contrast: Float,
        showMaskRubylith: Boolean = false
    ): Bitmap {
        val result = Bitmap.createBitmap(baseBitmap.width, baseBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(baseBitmap, 0f, 0f, null)

        if (maskBitmap != null && (exposureEV != 0f || temperature != 0f || saturation != 1f || contrast != 1f || showMaskRubylith)) {
            val saveCount = canvas.saveLayer(0f, 0f, baseBitmap.width.toFloat(), baseBitmap.height.toFloat(), null)

            val colorMatrix = createAdjustedColorMatrix(exposureEV, temperature, saturation, contrast)
            val adjPaint = Paint().apply {
                isAntiAlias = true
                isFilterBitmap = true
                colorFilter = ColorMatrixColorFilter(colorMatrix)
            }
            canvas.drawBitmap(baseBitmap, 0f, 0f, adjPaint)

            val maskPaint = Paint().apply {
                isAntiAlias = true
                isFilterBitmap = true
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            }
            val srcRect = android.graphics.Rect(0, 0, maskBitmap.width, maskBitmap.height)
            val dstRect = android.graphics.Rect(0, 0, baseBitmap.width, baseBitmap.height)
            canvas.drawBitmap(maskBitmap, srcRect, dstRect, maskPaint)

            canvas.restoreToCount(saveCount)
        }

        return result
    }

    /**
     * Composites Snapseed-Style Radial Control Points onto base image with zero allocations and hardware saveLayer.
     */
    fun compositeControlPoints(
        baseBitmap: Bitmap,
        points: List<SelectiveControlPoint>
    ): Bitmap {
        if (points.isEmpty()) return baseBitmap

        val result = Bitmap.createBitmap(baseBitmap.width, baseBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(baseBitmap, 0f, 0f, null)

        val width = baseBitmap.width.toFloat()
        val height = baseBitmap.height.toFloat()

        for (point in points) {
            val hasAdjustment = point.brightness != 0f || point.temperature != 0f || point.saturation != 1f || point.contrast != 1f
            if (!hasAdjustment) continue

            val centerX = (point.x * width).coerceIn(0f, width)
            val centerY = (point.y * height).coerceIn(0f, height)
            val radius = max(20f, point.radius)

            val left = max(0f, centerX - radius)
            val top = max(0f, centerY - radius)
            val right = min(width, centerX + radius)
            val bottom = min(height, centerY + radius)

            if (left >= right || top >= bottom) continue

            val saveCount = canvas.saveLayer(left, top, right, bottom, null)

            // 1. Draw base image adjusted with ColorMatrix in local bounding box
            val colorMatrix = createAdjustedColorMatrix(point.brightness * 2f, point.temperature, point.saturation, point.contrast)
            val adjPaint = Paint().apply {
                isAntiAlias = true
                isFilterBitmap = true
                this.colorFilter = ColorMatrixColorFilter(colorMatrix)
            }
            val srcRect = android.graphics.Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
            val dstRect = android.graphics.RectF(left, top, right, bottom)
            canvas.drawBitmap(baseBitmap, srcRect, dstRect, adjPaint)

            // 2. Apply Radial Gradient alpha falloff mask directly with DST_IN
            val radialShader = RadialGradient(
                centerX, centerY, radius,
                intArrayOf(Color.WHITE, Color.argb(160, 255, 255, 255), Color.TRANSPARENT),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            val maskPaint = Paint().apply {
                isAntiAlias = true
                shader = radialShader
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            }
            canvas.drawCircle(centerX, centerY, radius, maskPaint)

            canvas.restoreToCount(saveCount)
        }

        return result
    }
}
