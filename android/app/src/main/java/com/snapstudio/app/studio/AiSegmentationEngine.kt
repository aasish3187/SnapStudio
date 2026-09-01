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
 * On-Device Neural AI Segmentation Engine powered by Google ML Kit.
 * Handles 1-Tap Subject, Background, Sky, and Interactive Object Tap-Segmentation.
 */
object AiSegmentationEngine {

    private val selfieSegmenter = Segmentation.getClient(
        SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .enableRawSizeMask()
            .build()
    )

    suspend fun segmentSubject(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val maskBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val inputImage = InputImage.fromBitmap(source, 0)
        val segmentationMask: SegmentationMask = suspendCancellableCoroutine { cont ->
            selfieSegmenter.process(inputImage)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

        val maskBuffer: ByteBuffer = segmentationMask.buffer
        val maskWidth = segmentationMask.width
        val maskHeight = segmentationMask.height

        val outPixels = IntArray(width * height)
        maskBuffer.rewind()

        for (y in 0 until height) {
            val my = (y.toFloat() / height * maskHeight).toInt().coerceIn(0, maskHeight - 1)
            for (x in 0 until width) {
                val mx = (x.toFloat() / width * maskWidth).toInt().coerceIn(0, maskWidth - 1)
                val bufferIdx = (my * maskWidth + mx) * 4
                val confidence = maskBuffer.getFloat(bufferIdx)
                val alpha = (confidence * 255).toInt().coerceIn(0, 255)
                outPixels[y * width + x] = if (alpha > 40) Color.WHITE else Color.TRANSPARENT
            }
        }

        maskBmp.setPixels(outPixels, 0, width, 0, 0, width, height)
        maskBmp
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

    suspend fun segmentObjectFromPoint(source: Bitmap, tapPoint: Offset): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val maskBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val startX = tapPoint.x.toInt().coerceIn(0, width - 1)
        val startY = tapPoint.y.toInt().coerceIn(0, height - 1)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val seedColor = pixels[startY * width + startX]
        val seedR = Color.red(seedColor)
        val seedG = Color.green(seedColor)
        val seedB = Color.blue(seedColor)

        val visited = BooleanArray(width * height)
        val queue = ArrayDeque<Int>()
        queue.add(startY * width + startX)
        visited[startY * width + startX] = true

        val maxRadius = min(width, height) * 0.35f
        val maxDistSq = maxRadius * maxRadius

        val outPixels = IntArray(width * height)

        while (!queue.isEmpty()) {
            val curr = queue.poll() ?: break
            val cx = curr % width
            val cy = curr / width

            outPixels[curr] = Color.WHITE

            val dx = cx - startX
            val dy = cy - startY
            if (dx * dx + dy * dy > maxDistSq) continue

            val neighbors = intArrayOf(
                if (cx > 0) curr - 1 else -1,
                if (cx < width - 1) curr + 1 else -1,
                if (cy > 0) curr - width else -1,
                if (cy < height - 1) curr + width else -1
            )

            for (n in neighbors) {
                if (n >= 0 && !visited[n]) {
                    val c = pixels[n]
                    val nr = Color.red(c)
                    val ng = Color.green(c)
                    val nb = Color.blue(c)

                    val colorDist = abs(seedR - nr) + abs(seedG - ng) + abs(seedB - nb)
                    if (colorDist < 65) {
                        visited[n] = true
                        queue.add(n)
                    }
                }
            }
        }

        maskBmp.setPixels(outPixels, 0, width, 0, 0, width, height)
        maskBmp
    }
}
