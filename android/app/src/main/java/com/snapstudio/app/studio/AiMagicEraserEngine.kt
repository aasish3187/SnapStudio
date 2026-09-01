package com.snapstudio.app.studio

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * AI Magic Eraser Suite powered by Telea Fast Marching & Semantic Distraction Detection.
 */
object AiMagicEraserEngine {

    suspend fun aiErase(
        source: Bitmap,
        mask: Bitmap,
        refineMaskWithAi: Boolean = true
    ): Bitmap = withContext(Dispatchers.Default) {
        // Run Directional Fast Marching inpainting with boundary Poisson synthesis
        FastInpaintingEngine.inpaint(source, mask, radius = 10)
    }

    /**
     * Automatically identifies background distractions and bystanders using ML Kit Segmentation.
     */
    suspend fun detectBackgroundDistractions(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val distractionMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Get full subject segmentation
        val subjectMask = AiSegmentationEngine.segmentSubject(source)
        val subPixels = IntArray(width * height)
        subjectMask.getPixels(subPixels, 0, width, 0, 0, width, height)

        val outPixels = IntArray(width * height)

        // Find primary subject center of mass
        var sumX = 0L
        var sumY = 0L
        var totalSubject = 0

        for (y in 0 until height) {
            val row = y * width
            for (x in 0 until width) {
                if (Color.alpha(subPixels[row + x]) > 100) {
                    sumX += x
                    sumY += y
                    totalSubject++
                }
            }
        }

        if (totalSubject > 0) {
            val primaryCenterX = (sumX / totalSubject).toInt()
            val primaryCenterY = (sumY / totalSubject).toInt()
            val primaryRadius = sqrt(totalSubject.toDouble() / Math.PI) * 1.25

            // Highlight background people or isolated elements far from primary subject
            for (y in 0 until height) {
                val row = y * width
                for (x in 0 until width) {
                    val idx = row + x
                    if (Color.alpha(subPixels[idx]) > 80) {
                        val dx = x - primaryCenterX
                        val dy = y - primaryCenterY
                        val dist = sqrt((dx * dx + dy * dy).toDouble())
                        if (dist > primaryRadius) {
                            outPixels[idx] = Color.WHITE
                        }
                    }
                }
            }
        }

        distractionMask.setPixels(outPixels, 0, width, 0, 0, width, height)
        distractionMask
    }
}
