package com.snapstudio.app.studio

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * Intelligent Face Retouch & Portrait Enhancement Engine.
 * Uses dual-band frequency separation and intelligent skin tone segmentation
 * to soften skin blemishes while boosting facial, eye, and lip clarity.
 */
object FaceRetouchEngine {

    suspend fun enhancePortrait(
        source: Bitmap,
        skinSmooth: Float = 0.5f,
        clarity: Float = 0.4f,
        warmthGlow: Float = 0.2f
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val output = source.copy(Bitmap.Config.ARGB_8888, true)

        val srcPixels = IntArray(width * height)
        source.getPixels(srcPixels, 0, width, 0, 0, width, height)
        val outPixels = IntArray(width * height)

        val smoothStrength = skinSmooth.coerceIn(0f, 1f)
        val clarityStrength = clarity.coerceIn(0f, 1f)
        val glowStrength = warmthGlow.coerceIn(0f, 1f)

        // Box blur kernel radius for low frequency tone separation
        val blurRadius = (width * 0.015f).toInt().coerceIn(3, 15)
        val blurred = fastBoxBlur(source, blurRadius)
        val blurPixels = IntArray(width * height)
        blurred.getPixels(blurPixels, 0, width, 0, 0, width, height)

        for (y in 0 until height) {
            val rowOffset = y * width
            for (x in 0 until width) {
                val idx = rowOffset + x
                val srcP = srcPixels[idx]
                val blurP = blurPixels[idx]
                val a = Color.alpha(srcP)

                val r = Color.red(srcP)
                val g = Color.green(srcP)
                val b = Color.blue(srcP)

                // 1. Intelligent Skin Tone Detection
                // Standard human skin color range heuristic in RGB space
                val isSkin = (r > 60 && g > 40 && b > 20) &&
                        (r > g && g > b) &&
                        ((r - g) > 12) &&
                        (abs(r - g) > 15)

                // 2. High-Frequency Detail (Texture / Eye Clarity)
                val highR = r - Color.red(blurP)
                val highG = g - Color.green(blurP)
                val highB = b - Color.blue(blurP)

                var targetR = r.toFloat()
                var targetG = g.toFloat()
                var targetB = b.toFloat()

                if (isSkin && smoothStrength > 0f) {
                    // Soften low-frequency blemishes on skin
                    val blend = smoothStrength * 0.75f
                    targetR = (1f - blend) * targetR + blend * Color.red(blurP)
                    targetG = (1f - blend) * targetG + blend * Color.green(blurP)
                    targetB = (1f - blend) * targetB + blend * Color.blue(blurP)
                }

                // Boost high frequency sharpness (eyes, hair, lips, eyelashes)
                if (clarityStrength > 0f) {
                    val boost = clarityStrength * 0.65f
                    targetR += highR * boost
                    targetG += highG * boost
                    targetB += highB * boost
                }

                // Add soft healthy radiant skin glow
                if (glowStrength > 0f && isSkin) {
                    targetR += glowStrength * 15f
                    targetG += glowStrength * 6f
                }

                outPixels[idx] = Color.argb(
                    a,
                    targetR.roundToInt().coerceIn(0, 255),
                    targetG.roundToInt().coerceIn(0, 255),
                    targetB.roundToInt().coerceIn(0, 255)
                )
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

        val r = radius.coerceIn(1, 30)
        val div = 2 * r + 1

        val rSum = IntArray(w * h)
        val gSum = IntArray(w * h)
        val bSum = IntArray(w * h)

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
