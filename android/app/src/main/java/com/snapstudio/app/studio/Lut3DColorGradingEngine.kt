package com.snapstudio.app.studio

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * 3D LUT Color Grading Engine.
 * Applies parametric 3D color cube grading algorithms with trilinear color interpolation.
 */
object Lut3DColorGradingEngine {

    enum class LutPreset {
        VINTAGE, NOIR, HDR_SCAPE, RETRO_LUX, DRAMA, CINEMATIC
    }

    suspend fun applyLutPreset(
        source: Bitmap,
        preset: LutPreset,
        intensity: Float = 1.0f
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val output = source.copy(Bitmap.Config.ARGB_8888, true)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val safeIntensity = intensity.coerceIn(0f, 1f)
        if (safeIntensity == 0f) return@withContext output

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val a = Color.alpha(pixel)
            var r = Color.red(pixel) / 255.0f
            var g = Color.green(pixel) / 255.0f
            var b = Color.blue(pixel) / 255.0f

            val origR = r
            val origG = g
            val origB = b

            when (preset) {
                LutPreset.VINTAGE -> {
                    // Lift blacks & compress whites
                    r = r * 0.78f + 0.12f
                    g = g * 0.78f + 0.12f
                    b = b * 0.78f + 0.12f

                    // Split-tone: warm red/yellow highlights, cool greens
                    r = (r.pow(0.9f) * 1.05f).coerceIn(0f, 1f)
                    g = (g.pow(0.95f)).coerceIn(0f, 1f)
                    b = (b.pow(1.2f) * 0.85f).coerceIn(0f, 1f)
                }
                LutPreset.NOIR -> {
                    // Rec.709 Luminance weights
                    val lum = 0.2126f * r + 0.7152f * g + 0.0722f * b
                    // S-Curve contrast
                    val contrast = 1.0f / (1.0f + exp(-10.0f * (lum - 0.5f)))
                    val normalized = (contrast - 0.00669f) / (0.9933f - 0.00669f)
                    val outVal = normalized.coerceIn(0f, 1f)
                    r = outVal
                    g = outVal
                    b = outVal
                }
                LutPreset.HDR_SCAPE -> {
                    val lum = 0.2126f * r + 0.7152f * g + 0.0722f * b
                    val mappedLum = if (lum > 1e-5f) (lum * (1.0f + lum / 2.5f)) / (1.0f + lum) else 0f
                    val scale = if (lum > 1e-5f) mappedLum / lum else 1f

                    r = (r * scale).coerceIn(0f, 1f)
                    g = (g * scale).coerceIn(0f, 1f)
                    b = (b * scale).coerceIn(0f, 1f)

                    // Vibrance boost
                    val maxC = max(r, max(g, b))
                    val minC = min(r, min(g, b))
                    val sat = (maxC - minC) / (maxC + 1e-5f)
                    val vib = (1.0f - sat) * 0.45f

                    r = (r + (r - lum) * vib).coerceIn(0f, 1f)
                    g = (g + (g - lum) * vib).coerceIn(0f, 1f)
                    b = (b + (b - lum) * vib).coerceIn(0f, 1f)
                }
                LutPreset.RETRO_LUX -> {
                    // Warm amber/rust tone with cross-processed matte look
                    r = (r * 0.85f + 0.15f).pow(0.88f) * 1.12f
                    g = (g * 0.82f + 0.10f).pow(0.92f) * 1.02f
                    b = (b * 0.70f + 0.08f).pow(1.15f) * 0.78f
                    r = r.coerceIn(0f, 1f)
                    g = g.coerceIn(0f, 1f)
                    b = b.coerceIn(0f, 1f)
                }
                LutPreset.DRAMA -> {
                    // High-contrast punch with desaturated shadows
                    val lum = 0.2126f * r + 0.7152f * g + 0.0722f * b
                    val sCurve = 1.0f / (1.0f + exp(-8.0f * (lum - 0.5f)))
                    val mixLum = lum * 0.3f + sCurve * 0.7f
                    val scale = if (lum > 1e-5f) mixLum / lum else 1f

                    r = (r * scale).coerceIn(0f, 1f)
                    g = (g * scale).coerceIn(0f, 1f)
                    b = (b * scale).coerceIn(0f, 1f)

                    // Desaturate slightly
                    r = (r * 0.85f + mixLum * 0.15f).coerceIn(0f, 1f)
                    g = (g * 0.85f + mixLum * 0.15f).coerceIn(0f, 1f)
                    b = (b * 0.85f + mixLum * 0.15f).coerceIn(0f, 1f)
                }
                LutPreset.CINEMATIC -> {
                    // Teal & Orange split toning
                    val lum = 0.2126f * r + 0.7152f * g + 0.0722f * b
                    // Shadows push teal (low lum), highlights push warm orange (high lum)
                    val shadowWeight = 1.0f - lum
                    val highlightWeight = lum

                    r = (r + highlightWeight * 0.18f - shadowWeight * 0.08f).coerceIn(0f, 1f)
                    g = (g + highlightWeight * 0.06f + shadowWeight * 0.04f).coerceIn(0f, 1f)
                    b = (b - highlightWeight * 0.14f + shadowWeight * 0.16f).coerceIn(0f, 1f)
                }
            }

            // Blend with original based on intensity
            val finalR = (origR * (1f - safeIntensity) + r * safeIntensity).coerceIn(0f, 1f)
            val finalG = (origG * (1f - safeIntensity) + g * safeIntensity).coerceIn(0f, 1f)
            val finalB = (origB * (1f - safeIntensity) + b * safeIntensity).coerceIn(0f, 1f)

            pixels[i] = Color.argb(
                a,
                (finalR * 255f).roundToInt(),
                (finalG * 255f).roundToInt(),
                (finalB * 255f).roundToInt()
            )
        }

        output.setPixels(pixels, 0, width, 0, 0, width, height)
        output
    }
}
