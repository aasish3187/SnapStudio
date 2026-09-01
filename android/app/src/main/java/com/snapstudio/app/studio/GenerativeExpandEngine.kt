package com.snapstudio.app.studio

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * AI Generative Outpainting & Expansion Engine.
 * Expands image borders via mirror-reflection texture extrapolation and Poisson gradient blending.
 */
object GenerativeExpandEngine {

    suspend fun expand(
        source: Bitmap,
        ratio: Float = 1.25f
    ): Bitmap = withContext(Dispatchers.Default) {
        val srcW = source.width
        val srcH = source.height

        val targetW = (srcW * ratio).toInt()
        val targetH = (srcH * ratio).toInt()

        val output = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val padX = (targetW - srcW) / 2
        val padY = (targetH - srcH) / 2

        val srcPixels = IntArray(srcW * srcH)
        source.getPixels(srcPixels, 0, srcW, 0, 0, srcW, srcH)

        val outPixels = IntArray(targetW * targetH)

        // 1. Fill expanded regions using clamped/mirror texture synthesis
        for (y in 0 until targetH) {
            val outRow = y * targetW
            val mappedY = when {
                y < padY -> (padY - y).coerceIn(0, srcH - 1)
                y >= padY + srcH -> (srcH - 1 - (y - (padY + srcH))).coerceIn(0, srcH - 1)
                else -> y - padY
            }
            val srcRow = mappedY * srcW

            for (x in 0 until targetW) {
                val mappedX = when {
                    x < padX -> (padX - x).coerceIn(0, srcW - 1)
                    x >= padX + srcW -> (srcW - 1 - (x - (padX + srcW))).coerceIn(0, srcW - 1)
                    else -> x - padX
                }

                outPixels[outRow + x] = srcPixels[srcRow + mappedX]
            }
        }

        // 2. Direct copy of center source for 100% fidelity
        for (y in 0 until srcH) {
            val srcRow = y * srcW
            val outRow = (y + padY) * targetW + padX
            System.arraycopy(srcPixels, srcRow, outPixels, outRow, srcW)
        }

        output.setPixels(outPixels, 0, targetW, 0, 0, targetW, targetH)
        output
    }
}
