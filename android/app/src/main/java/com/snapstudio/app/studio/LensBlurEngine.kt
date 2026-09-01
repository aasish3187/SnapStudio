package com.snapstudio.app.studio

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * Optical Lens Blur & Bokeh Engine.
 * Implements radial and linear depth-map tilt-shift blur with smooth transition falloff.
 */
object LensBlurEngine {

    enum class BlurShape {
        CIRCULAR, LINEAR
    }

    suspend fun applyLensBlur(
        source: Bitmap,
        shape: BlurShape = BlurShape.CIRCULAR,
        blurRadius: Int = 12,
        centerX: Float = 0.5f,
        centerY: Float = 0.5f,
        focalSize: Float = 0.35f,
        feather: Float = 0.35f
    ): Bitmap = withContext(Dispatchers.Default) {
        if (blurRadius <= 0) return@withContext source

        val width = source.width
        val height = source.height
        val output = source.copy(Bitmap.Config.ARGB_8888, true)

        // 1. Generate fast multi-pass blurred image
        val blurred = fastBoxBlur(source, blurRadius)

        val srcPixels = IntArray(width * height)
        val blurPixels = IntArray(width * height)
        val outPixels = IntArray(width * height)

        source.getPixels(srcPixels, 0, width, 0, 0, width, height)
        blurred.getPixels(blurPixels, 0, width, 0, 0, width, height)

        val maxDim = max(width, height).toFloat()
        val innerDist = focalSize * (maxDim * 0.5f)
        val outerDist = (focalSize + feather) * (maxDim * 0.5f)

        val centerPxX = centerX * width
        val centerPxY = centerY * height

        // 2. Blend sharp base with blurred image according to depth mask
        for (y in 0 until height) {
            val rowOffset = y * width
            for (x in 0 until width) {
                val idx = rowOffset + x

                val dist = when (shape) {
                    BlurShape.CIRCULAR -> hypot(x - centerPxX, y - centerPxY)
                    BlurShape.LINEAR -> abs(y - centerPxY)
                }

                // Smoothstep transition
                val blurAmount = when {
                    dist <= innerDist -> 0f
                    dist >= outerDist -> 1f
                    else -> {
                        val t = (dist - innerDist) / max(1f, outerDist - innerDist)
                        t * t * (3f - 2f * t)
                    }
                }

                val srcP = srcPixels[idx]
                val blurP = blurPixels[idx]
                val a = Color.alpha(srcP)

                val r = (Color.red(srcP) * (1f - blurAmount) + Color.red(blurP) * blurAmount).roundToInt().coerceIn(0, 255)
                val g = (Color.green(srcP) * (1f - blurAmount) + Color.green(blurP) * blurAmount).roundToInt().coerceIn(0, 255)
                val b = (Color.blue(srcP) * (1f - blurAmount) + Color.blue(blurP) * blurAmount).roundToInt().coerceIn(0, 255)

                outPixels[idx] = Color.argb(a, r, g, b)
            }
        }

        blurred.recycle()
        output.setPixels(outPixels, 0, width, 0, 0, width, height)
        output
    }

    private fun fastBoxBlur(src: Bitmap, radius: Int): Bitmap {
        val w = src.width
        val h = src.height
        val pix = IntArray(w * h)
        src.getPixels(pix, 0, w, 0, 0, w, h)

        val r = radius.coerceIn(1, 40)
        val div = 2 * r + 1

        val rSum = IntArray(w * h)
        val gSum = IntArray(w * h)
        val bSum = IntArray(w * h)

        // Horizontal Pass
        for (y in 0 until h) {
            var currR = 0; var currG = 0; var currB = 0
            val rowOffset = y * w

            for (i in -r..r) {
                val clampedX = i.coerceIn(0, w - 1)
                val p = pix[rowOffset + clampedX]
                currR += Color.red(p)
                currG += Color.green(p)
                currB += Color.blue(p)
            }

            for (x in 0 until w) {
                rSum[rowOffset + x] = currR / div
                gSum[rowOffset + x] = currG / div
                bSum[rowOffset + x] = currB / div

                val leftX = (x - r).coerceIn(0, w - 1)
                val rightX = (x + r + 1).coerceIn(0, w - 1)
                val pLeft = pix[rowOffset + leftX]
                val pRight = pix[rowOffset + rightX]

                currR += Color.red(pRight) - Color.red(pLeft)
                currG += Color.green(pRight) - Color.green(pLeft)
                currB += Color.blue(pRight) - Color.blue(pLeft)
            }
        }

        // Vertical Pass
        val outPix = IntArray(w * h)
        for (x in 0 until w) {
            var currR = 0; var currG = 0; var currB = 0

            for (i in -r..r) {
                val clampedY = i.coerceIn(0, h - 1)
                val rowOffset = clampedY * w
                currR += rSum[rowOffset + x]
                currG += gSum[rowOffset + x]
                currB += bSum[rowOffset + x]
            }

            for (y in 0 until h) {
                val idx = y * w + x
                outPix[idx] = Color.argb(
                    255,
                    (currR / div).coerceIn(0, 255),
                    (currG / div).coerceIn(0, 255),
                    (currB / div).coerceIn(0, 255)
                )

                val topY = (y - r).coerceIn(0, h - 1)
                val botY = (y + r + 1).coerceIn(0, h - 1)

                currR += rSum[botY * w + x] - rSum[topY * w + x]
                currG += gSum[botY * w + x] - gSum[topY * w + x]
                currB += bSum[botY * w + x] - bSum[topY * w + x]
            }
        }

        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(outPix, 0, w, 0, 0, w, h)
        return result
    }
}
