package com.snapstudio.app.studio

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * Spatial & Convolution Filter Engine.
 * Implements Dehaze, Unsharp Masking (Details/Structure), Lens Blur, Film Grain, and Vignette.
 */
object SpatialConvolutionEngine {

    /**
     * Unsharp Masking & High-Pass Structure for fine micro-contrast enhancement.
     */
    suspend fun applyDetails(
        source: Bitmap,
        structure: Float,
        sharpening: Float
    ): Bitmap = withContext(Dispatchers.Default) {
        if (structure == 0f && sharpening == 0f) return@withContext source

        val width = source.width
        val height = source.height
        val output = source.copy(Bitmap.Config.ARGB_8888, true)

        val srcPixels = IntArray(width * height)
        source.getPixels(srcPixels, 0, width, 0, 0, width, height)
        val outPixels = srcPixels.clone()

        val amount = sharpening.coerceIn(0f, 1f) * 1.5f + structure.coerceIn(0f, 1f) * 1.0f

        // 3x3 Laplacian / High-pass kernel
        for (y in 1 until height - 1) {
            val rowOffset = y * width
            for (x in 1 until width - 1) {
                val idx = rowOffset + x
                val center = srcPixels[idx]
                val a = Color.alpha(center)

                val top = srcPixels[(y - 1) * width + x]
                val bottom = srcPixels[(y + 1) * width + x]
                val left = srcPixels[rowOffset + (x - 1)]
                val right = srcPixels[rowOffset + (x + 1)]

                // High-pass difference
                val rDiff = 4 * Color.red(center) - Color.red(top) - Color.red(bottom) - Color.red(left) - Color.red(right)
                val gDiff = 4 * Color.green(center) - Color.green(top) - Color.green(bottom) - Color.green(left) - Color.green(right)
                val bDiff = 4 * Color.blue(center) - Color.blue(top) - Color.blue(bottom) - Color.blue(left) - Color.blue(right)

                val newR = (Color.red(center) + rDiff * amount * 0.25f).roundToInt().coerceIn(0, 255)
                val newG = (Color.green(center) + gDiff * amount * 0.25f).roundToInt().coerceIn(0, 255)
                val newB = (Color.blue(center) + bDiff * amount * 0.25f).roundToInt().coerceIn(0, 255)

                outPixels[idx] = Color.argb(a, newR, newG, newB)
            }
        }

        output.setPixels(outPixels, 0, width, 0, 0, width, height)
        output
    }

    /**
     * Atmospheric Dehaze based on Dark Channel Prior approximation.
     */
    suspend fun applyDehaze(
        source: Bitmap,
        strength: Float
    ): Bitmap = withContext(Dispatchers.Default) {
        val s = strength.coerceIn(0f, 1f)
        if (s == 0f) return@withContext source

        val width = source.width
        val height = source.height
        val output = source.copy(Bitmap.Config.ARGB_8888, true)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val atmosphericA = 220f // Standard atmospheric daylight baseline

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val a = Color.alpha(pixel)
            val r = Color.red(pixel).toFloat()
            val g = Color.green(pixel).toFloat()
            val b = Color.blue(pixel).toFloat()

            // Dark channel estimation (minimum color component)
            val darkChannel = min(r, min(g, b))
            val transmission = (1.0f - s * 0.75f * (darkChannel / atmosphericA)).coerceIn(0.2f, 1.0f)

            // Recover scene radiance: J = (I - A)/t + A
            val newR = (((r - atmosphericA) / transmission) + atmosphericA).roundToInt().coerceIn(0, 255)
            val newG = (((g - atmosphericA) / transmission) + atmosphericA).roundToInt().coerceIn(0, 255)
            val newB = (((b - atmosphericA) / transmission) + atmosphericA).roundToInt().coerceIn(0, 255)

            pixels[i] = Color.argb(a, newR, newG, newB)
        }

        output.setPixels(pixels, 0, width, 0, 0, width, height)
        output
    }

    /**
     * Procedural Film Grain using hash noise.
     */
    suspend fun applyFilmGrain(
        source: Bitmap,
        strength: Float
    ): Bitmap = withContext(Dispatchers.Default) {
        val s = strength.coerceIn(0f, 1f)
        if (s == 0f) return@withContext source

        val width = source.width
        val height = source.height
        val output = source.copy(Bitmap.Config.ARGB_8888, true)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val intensity = s * 45f

        for (y in 0 until height) {
            val rowOffset = y * width
            for (x in 0 until width) {
                val idx = rowOffset + x
                val pixel = pixels[idx]
                val a = Color.alpha(pixel)

                // High frequency procedural hash: fract(sin(x*12.9898 + y*78.233) * 43758.5453)
                val noise = ((sin(x * 12.9898 + y * 78.233) * 43758.5453) % 1.0).toFloat()
                val grainOffset = (noise - 0.5f) * intensity

                val r = (Color.red(pixel) + grainOffset).roundToInt().coerceIn(0, 255)
                val g = (Color.green(pixel) + grainOffset).roundToInt().coerceIn(0, 255)
                val b = (Color.blue(pixel) + grainOffset).roundToInt().coerceIn(0, 255)

                pixels[idx] = Color.argb(a, r, g, b)
            }
        }

        output.setPixels(pixels, 0, width, 0, 0, width, height)
        output
    }
}
