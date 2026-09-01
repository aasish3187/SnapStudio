package com.snapstudio.app.studio

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.geometry.Offset
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.util.ArrayDeque
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.*

/**
 * Universal AI Background & Object Segmentation Engine.
 * Dual-Stage Pipeline:
 * 1. Google ML Kit Neural Portrait Segmenter (for human portraits)
 * 2. Universal Boundary-Contrast Saliency & Saliency Matting (for objects, products, pets, cars, items, offline fallback)
 */
object AiSegmentationEngine {

    private val selfieSegmenter = try {
        Segmentation.getClient(
            SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                .enableRawSizeMask()
                .build()
        )
    } catch (e: Throwable) {
        null
    }

    /**
     * Segments the primary subject (works for both people and any general object/product).
     */
    suspend fun segmentSubject(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val maskBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        var mlKitSubjectPixels = 0
        val outPixels = IntArray(width * height)

        // Stage 1: Try ML Kit Selfie Segmenter
        if (selfieSegmenter != null) {
            try {
                val inputImage = InputImage.fromBitmap(source, 0)
                val segmentationMask: SegmentationMask = suspendCancellableCoroutine { cont ->
                    selfieSegmenter.process(inputImage)
                        .addOnSuccessListener { cont.resume(it) }
                        .addOnFailureListener { cont.resumeWithException(it) }
                }

                val maskBuffer: ByteBuffer = segmentationMask.buffer
                val maskWidth = segmentationMask.width
                val maskHeight = segmentationMask.height
                maskBuffer.rewind()

                for (y in 0 until height) {
                    val my = (y.toFloat() / height * maskHeight).toInt().coerceIn(0, maskHeight - 1)
                    for (x in 0 until width) {
                        val mx = (x.toFloat() / width * maskWidth).toInt().coerceIn(0, maskWidth - 1)
                        val bufferIdx = (my * maskWidth + mx) * 4
                        val confidence = maskBuffer.getFloat(bufferIdx)
                        if (confidence > 0.35f) {
                            outPixels[y * width + x] = Color.WHITE
                            mlKitSubjectPixels++
                        }
                    }
                }
            } catch (e: Throwable) {
                mlKitSubjectPixels = 0
            }
        }

        // Stage 2: If ML Kit did not find a person (e.g. object, product, pet, or error), use Universal Saliency
        val minRequiredPixels = (width * height * 0.02).toInt() // At least 2% of image
        if (mlKitSubjectPixels < minRequiredPixels) {
            val saliencyMask = computeUniversalObjectSaliency(source)
            maskBmp.setPixels(saliencyMask, 0, width, 0, 0, width, height)
        } else {
            maskBmp.setPixels(outPixels, 0, width, 0, 0, width, height)
        }

        maskBmp
    }

    /**
     * Universal Object & Product Saliency Matting.
     * Uses boundary background priors + center-surround color contrast to isolate any object.
     */
    private fun computeUniversalObjectSaliency(source: Bitmap): IntArray {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val outMask = IntArray(width * height)

        // 1. Build Background Color Model from outer 4% border pixels
        val borderPadX = max(4, width / 25)
        val borderPadY = max(4, height / 25)

        var bgSumR = 0L
        var bgSumG = 0L
        var bgSumB = 0L
        var bgCount = 0

        for (y in 0 until height) {
            val row = y * width
            val isBorderY = y < borderPadY || y >= height - borderPadY
            for (x in 0 until width) {
                val isBorderX = x < borderPadX || x >= width - borderPadX
                if (isBorderX || isBorderY) {
                    val c = pixels[row + x]
                    bgSumR += Color.red(c)
                    bgSumG += Color.green(c)
                    bgSumB += Color.blue(c)
                    bgCount++
                }
            }
        }

        if (bgCount == 0) bgCount = 1
        val bgR = (bgSumR / bgCount).toDouble()
        val bgG = (bgSumG / bgCount).toDouble()
        val bgB = (bgSumB / bgCount).toDouble()

        // 2. Compute Center Prior & Saliency Distance
        val centerX = width / 2.0
        val centerY = height / 2.0
        val maxDist = sqrt(centerX * centerX + centerY * centerY)

        for (y in 0 until height) {
            val row = y * width
            val dy = y - centerY
            for (x in 0 until width) {
                val dx = x - centerX
                val distFromCenter = sqrt(dx * dx + dy * dy)
                val centerWeight = 1.0 - (distFromCenter / maxDist).pow(1.5).coerceIn(0.0, 1.0)

                val c = pixels[row + x]
                val r = Color.red(c)
                val g = Color.green(c)
                val b = Color.blue(c)

                val dr = r - bgR
                val dg = g - bgG
                val db = b - bgB
                val colorDist = sqrt(dr * dr + dg * dg + db * db)

                // High color difference from border background + center bias = Foreground Object
                val saliency = (colorDist / 85.0).coerceIn(0.0, 1.0) * 0.7 + centerWeight * 0.3

                val isForeground = (saliency > 0.45) && (x >= borderPadX / 2) && (x < width - borderPadX / 2) &&
                        (y >= borderPadY / 2) && (y < height - borderPadY / 2)

                outMask[row + x] = if (isForeground) Color.WHITE else Color.TRANSPARENT
            }
        }

        return outMask
    }

    /**
     * 1-Tap Instant Background Removal / Transparent Cutout.
     */
    suspend fun removeBackground(source: Bitmap, backgroundColor: Int = Color.TRANSPARENT): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val subjectMask = segmentSubject(source)

        val srcPixels = IntArray(width * height)
        val maskPixels = IntArray(width * height)
        val outPixels = IntArray(width * height)

        source.getPixels(srcPixels, 0, width, 0, 0, width, height)
        subjectMask.getPixels(maskPixels, 0, width, 0, 0, width, height)

        for (i in srcPixels.indices) {
            val maskAlpha = Color.alpha(maskPixels[i])
            if (maskAlpha > 30) {
                outPixels[i] = srcPixels[i]
            } else {
                outPixels[i] = backgroundColor
            }
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(outPixels, 0, width, 0, 0, width, height)
        result
    }

    /**
     * 1-Tap Instant Subject Removal & Background Inpainting.
     */
    suspend fun removeSubject(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val subjectMask = segmentSubject(source)
        FastInpaintingEngine.inpaint(source, subjectMask, radius = 12)
    }

    /**
     * 1-Tap Instant Sky Removal.
     */
    suspend fun removeSky(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val skyMask = segmentSky(source)
        FastInpaintingEngine.inpaint(source, skyMask, radius = 12)
    }

    suspend fun segmentBackground(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val subjectMask = segmentSubject(source)
        val width = subjectMask.width
        val height = subjectMask.height
        val bgMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        subjectMask.getPixels(pixels, 0, width, 0, 0, width, height)
        for (i in pixels.indices) {
            val a = Color.alpha(pixels[i])
            pixels[i] = if (a < 50) Color.WHITE else Color.TRANSPARENT
        }
        bgMask.setPixels(pixels, 0, width, 0, 0, width, height)
        bgMask
    }

    suspend fun segmentSky(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val skyMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val outPixels = IntArray(width * height)
        for (y in 0 until (height * 0.70f).toInt()) {
            for (x in 0 until width) {
                val col = pixels[y * width + x]
                val r = Color.red(col)
                val g = Color.green(col)
                val b = Color.blue(col)

                val isSky = (b > r * 1.05f && b > g * 0.95f && b > 80) || (r > 200 && g > 200 && b > 200 && y < height * 0.4f)
                if (isSky) {
                    outPixels[y * width + x] = Color.WHITE
                }
            }
        }
        skyMask.setPixels(outPixels, 0, width, 0, 0, width, height)
        skyMask
    }
}
