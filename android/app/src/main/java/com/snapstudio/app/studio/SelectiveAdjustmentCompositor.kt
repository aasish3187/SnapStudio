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

        // 1. Draw base image
        canvas.drawBitmap(baseBitmap, 0f, 0f, null)

        if (maskBitmap != null && (exposureEV != 0f || temperature != 0f || saturation != 1f || contrast != 1f || showMaskRubylith)) {
            val adjustedBitmap = Bitmap.createBitmap(baseBitmap.width, baseBitmap.height, Bitmap.Config.ARGB_8888)
            val adjustedCanvas = Canvas(adjustedBitmap)
            val colorMatrix = createAdjustedColorMatrix(exposureEV, temperature, saturation, contrast)
            val adjPaint = Paint().apply {
                isAntiAlias = true
                colorFilter = ColorMatrixColorFilter(colorMatrix)
            }
            adjustedCanvas.drawBitmap(baseBitmap, 0f, 0f, adjPaint)

            val maskPaint = Paint().apply {
                isAntiAlias = true
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            }
            val srcRect = android.graphics.Rect(0, 0, maskBitmap.width, maskBitmap.height)
            val dstRect = android.graphics.Rect(0, 0, baseBitmap.width, baseBitmap.height)
            adjustedCanvas.drawBitmap(maskBitmap, srcRect, dstRect, maskPaint)

            canvas.drawBitmap(adjustedBitmap, 0f, 0f, null)
            adjustedBitmap.recycle()
        }

        return result
    }

    /**
     * Composites Snapseed-Style Radial Control Points onto base image with color-affinity falloff.
     */
    fun compositeControlPoints(
        baseBitmap: Bitmap,
        points: List<SelectiveControlPoint>
    ): Bitmap {
        if (points.isEmpty()) return baseBitmap

        val result = Bitmap.createBitmap(baseBitmap.width, baseBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(baseBitmap, 0f, 0f, null)

        val width = baseBitmap.width
        val height = baseBitmap.height

        for (point in points) {
            val hasAdjustment = point.brightness != 0f || point.temperature != 0f || point.saturation != 1f || point.contrast != 1f
            if (!hasAdjustment) continue

            val centerX = point.x * width
            val centerY = point.y * height
            val radius = max(20f, point.radius)

            // 1. Render adjusted layer
            val adjustedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val adjustedCanvas = Canvas(adjustedBitmap)
            val colorMatrix = createAdjustedColorMatrix(point.brightness * 2f, point.temperature, point.saturation, point.contrast)
            val adjPaint = Paint().apply {
                isAntiAlias = true
                colorFilter = ColorMatrixColorFilter(colorMatrix)
            }
            adjustedCanvas.drawBitmap(baseBitmap, 0f, 0f, adjPaint)

            // 2. Generate radial alpha mask
            val maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val maskCanvas = Canvas(maskBitmap)
            val radialShader = RadialGradient(
                centerX, centerY, radius,
                intArrayOf(Color.WHITE, Color.argb(160, 255, 255, 255), Color.TRANSPARENT),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            val maskPaint = Paint().apply {
                isAntiAlias = true
                shader = radialShader
            }
            maskCanvas.drawCircle(centerX, centerY, radius, maskPaint)

            // 3. Mask adjusted layer
            val blendPaint = Paint().apply {
                isAntiAlias = true
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            }
            adjustedCanvas.drawBitmap(maskBitmap, 0f, 0f, blendPaint)
            maskBitmap.recycle()

            // 4. Draw over canvas
            canvas.drawBitmap(adjustedBitmap, 0f, 0f, null)
            adjustedBitmap.recycle()
        }

        return result
    }
}
