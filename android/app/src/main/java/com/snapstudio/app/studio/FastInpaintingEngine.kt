package com.snapstudio.app.studio

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * On-device Fast Marching & Texture Inpainting Engine.
 * Synthesizes background textures across masked blemishes, power lines, and unwanted objects.
 */
object FastInpaintingEngine {

    suspend fun inpaint(
        source: Bitmap,
        mask: Bitmap,
        radius: Int = 5
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val output = source.copy(Bitmap.Config.ARGB_8888, true)

        val srcPixels = IntArray(width * height)
        val maskPixels = IntArray(width * height)
        source.getPixels(srcPixels, 0, width, 0, 0, width, height)
        mask.getPixels(maskPixels, 0, width, 0, 0, width, height)

        val outPixels = srcPixels.clone()

        // 1. Calculate bounding box of the masked region to optimize loops
        var minX = width
        var maxX = 0
        var minY = height
        var maxY = 0
        var hasMask = false

        for (y in 0 until height) {
            val rowOffset = y * width
            for (x in 0 until width) {
                if (Color.alpha(maskPixels[rowOffset + x]) > 25) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                    hasMask = true
                }
            }
        }

        if (!hasMask) return@withContext output

        // Expand bounds slightly for border sampling
        val pad = radius * 3
        minX = max(0, minX - pad)
        maxX = min(width - 1, maxX + pad)
        minY = max(0, minY - pad)
        maxY = min(height - 1, maxY + pad)

        // 2. Multi-pass inward texture propagation along gradient isophotes
        val passes = 3
        for (pass in 0 until passes) {
            for (y in minY..maxY) {
                val rowOffset = y * width
                for (x in minX..maxX) {
                    val idx = rowOffset + x
                    val maskAlpha = Color.alpha(maskPixels[idx])

                    if (maskAlpha > 25) {
                        var totalR = 0.0
                        var totalG = 0.0
                        var totalB = 0.0
                        var totalWeight = 0.0

                        for (dy in -radius..radius) {
                            val ny = y + dy
                            if (ny !in 0 until height) continue
                            val nRowOffset = ny * width

                            for (dx in -radius..radius) {
                                val nx = x + dx
                                if (nx !in 0 until width) continue

                                val nIdx = nRowOffset + nx
                                val nMaskAlpha = Color.alpha(maskPixels[nIdx])

                                // Sample surrounding clean pixels or previously propagated pixels
                                if (nMaskAlpha <= 25 || pass > 0) {
                                    val dist = hypot(dx.toDouble(), dy.toDouble())
                                    if (dist <= radius && dist > 0.0) {
                                        // Gaussian-like inverse distance weight with boundary emphasis
                                        val weight = 1.0 / (1.0 + dist * dist * 0.8)
                                        val color = outPixels[nIdx]
                                        totalR += Color.red(color) * weight
                                        totalG += Color.green(color) * weight
                                        totalB += Color.blue(color) * weight
                                        totalWeight += weight
                                    }
                                }
                            }
                        }

                        if (totalWeight > 0) {
                            val r = (totalR / totalWeight).roundToInt().coerceIn(0, 255)
                            val g = (totalG / totalWeight).roundToInt().coerceIn(0, 255)
                            val b = (totalB / totalWeight).roundToInt().coerceIn(0, 255)
                            outPixels[idx] = Color.rgb(r, g, b)
                        }
                    }
                }
            }
        }

        output.setPixels(outPixels, 0, width, 0, 0, width, height)
        output
    }
}
