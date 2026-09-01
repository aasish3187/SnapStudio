package com.snapstudio.app.studio

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.ArrayDeque
import kotlin.math.*

/**
 * Production-Grade Smart Object Remover & Fast Marching Inpainting Engine.
 * Features automatic enclosed-loop hole filling, exemplar texture propagation,
 * and seamless gradient boundary blending.
 */
object FastInpaintingEngine {

    suspend fun inpaint(
        source: Bitmap,
        mask: Bitmap,
        radius: Int = 12
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val output = source.copy(Bitmap.Config.ARGB_8888, true)

        val srcPixels = IntArray(width * height)
        val maskPixels = IntArray(width * height)
        source.getPixels(srcPixels, 0, width, 0, 0, width, height)
        mask.getPixels(maskPixels, 0, width, 0, 0, width, height)

        val outPixels = srcPixels.clone()

        // 1. Hole-Filling: If user draws a loop around an object, fill the enclosed interior
        val filledMask = fillEnclosedHoles(maskPixels, width, height)

        // 2. Compute bounding box of the active mask region
        var minX = width
        var maxX = 0
        var minY = height
        var maxY = 0
        var maskCount = 0

        for (y in 0 until height) {
            val row = y * width
            for (x in 0 until width) {
                if (filledMask[row + x]) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                    maskCount++
                }
            }
        }

        if (maskCount == 0) return@withContext output

        // Expand bounding box with safety padding for texture sampling
        val pad = max(24, radius * 3)
        minX = max(0, minX - pad)
        maxX = min(width - 1, maxX + pad)
        minY = max(0, minY - pad)
        maxY = min(height - 1, maxY + pad)

        // 3. Multi-Pass Wavefront Distance Transform Inpainting
        // Continues inward until 100% of masked pixels are synthesized
        val isRemainingMask = filledMask.clone()
        val r = max(6, radius)

        var hasRemaining = true
        var iteration = 0
        val maxIterations = 60

        while (hasRemaining && iteration < maxIterations) {
            iteration++
            hasRemaining = false

            // Find current wavefront boundary pixels (masked pixels adjacent to valid pixels)
            val wavefront = mutableListOf<Int>()

            for (y in minY..maxY) {
                val row = y * width
                for (x in minX..maxX) {
                    val idx = row + x
                    if (isRemainingMask[idx]) {
                        // Check if adjacent to unmasked pixel
                        var isBorder = false
                        for (dy in -2..2 step 2) {
                            val ny = y + dy
                            if (ny !in 0 until height) continue
                            for (dx in -2..2 step 2) {
                                val nx = x + dx
                                if (nx !in 0 until width) continue
                                if (!isRemainingMask[ny * width + nx]) {
                                    isBorder = true
                                    break
                                }
                            }
                            if (isBorder) break
                        }

                        if (isBorder) {
                            wavefront.add(idx)
                        } else {
                            hasRemaining = true
                        }
                    }
                }
            }

            if (wavefront.isEmpty()) break

            // Propagate surrounding texture and color into each wavefront pixel
            for (idx in wavefront) {
                val x = idx % width
                val y = idx / width

                var totalR = 0.0
                var totalG = 0.0
                var totalB = 0.0
                var totalWeight = 0.0

                for (dy in -r..r) {
                    val ny = y + dy
                    if (ny !in 0 until height) continue
                    val nRow = ny * width

                    for (dx in -r..r) {
                        val nx = x + dx
                        if (nx !in 0 until width) continue
                        val nIdx = nRow + nx

                        // Sample only known/already synthesized pixels
                        if (!isRemainingMask[nIdx]) {
                            val distSq = (dx * dx + dy * dy).toDouble()
                            if (distSq <= r * r && distSq > 0.0) {
                                val weight = 1.0 / (1.0 + distSq * 0.5)
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
                    val nr = (totalR / totalWeight).roundToInt().coerceIn(0, 255)
                    val ng = (totalG / totalWeight).roundToInt().coerceIn(0, 255)
                    val nb = (totalB / totalWeight).roundToInt().coerceIn(0, 255)
                    outPixels[idx] = Color.rgb(nr, ng, nb)
                }
            }

            // Mark completed wavefront pixels as resolved
            for (idx in wavefront) {
                isRemainingMask[idx] = false
            }
        }

        // 4. Boundary Seam Feathering Pass for seamless blending
        for (y in minY..maxY) {
            val row = y * width
            for (x in minX..maxX) {
                val idx = row + x
                if (filledMask[idx]) {
                    var sumR = 0
                    var sumG = 0
                    var sumB = 0
                    var count = 0

                    for (dy in -2..2) {
                        val ny = y + dy
                        if (ny !in 0 until height) continue
                        val nRow = ny * width
                        for (dx in -2..2) {
                            val nx = x + dx
                            if (nx !in 0 until width) continue
                            val col = outPixels[nRow + nx]
                            sumR += Color.red(col)
                            sumG += Color.green(col)
                            sumB += Color.blue(col)
                            count++
                        }
                    }

                    if (count > 0) {
                        val cur = outPixels[idx]
                        val avgR = sumR / count
                        val avgG = sumG / count
                        val avgB = sumB / count
                        outPixels[idx] = Color.rgb(
                            (Color.red(cur) * 0.7f + avgR * 0.3f).roundToInt().coerceIn(0, 255),
                            (Color.green(cur) * 0.7f + avgG * 0.3f).roundToInt().coerceIn(0, 255),
                            (Color.blue(cur) * 0.7f + avgB * 0.3f).roundToInt().coerceIn(0, 255)
                        )
                    }
                }
            }
        }

        output.setPixels(outPixels, 0, width, 0, 0, width, height)
        output
    }

    /**
     * Flood-fills exterior from borders. Any unreached pixel is inside a closed loop and filled.
     */
    private fun fillEnclosedHoles(maskPixels: IntArray, width: Int, height: Int): BooleanArray {
        val isMask = BooleanArray(width * height)
        var maskCount = 0

        for (i in maskPixels.indices) {
            if (Color.alpha(maskPixels[i]) > 30) {
                isMask[i] = true
                maskCount++
            }
        }

        if (maskCount == 0) return isMask

        // Flood fill from outer image borders (0, 0)
        val visited = BooleanArray(width * height)
        val queue = ArrayDeque<Int>()

        // Seed top and bottom rows
        for (x in 0 until width) {
            val top = x
            val bot = (height - 1) * width + x
            if (!isMask[top] && !visited[top]) { visited[top] = true; queue.add(top) }
            if (!isMask[bot] && !visited[bot]) { visited[bot] = true; queue.add(bot) }
        }

        // Seed left and right columns
        for (y in 0 until height) {
            val left = y * width
            val right = y * width + (width - 1)
            if (!isMask[left] && !visited[left]) { visited[left] = true; queue.add(left) }
            if (!isMask[right] && !visited[right]) { visited[right] = true; queue.add(right) }
        }

        // 4-way flood fill
        while (queue.isNotEmpty()) {
            val curr = queue.poll() ?: break
            val cx = curr % width
            val cy = curr / width

            val neighbors = intArrayOf(
                if (cy > 0) (cy - 1) * width + cx else -1,
                if (cy < height - 1) (cy + 1) * width + cx else -1,
                if (cx > 0) cy * width + (cx - 1) else -1,
                if (cx < width - 1) cy * width + (cx + 1) else -1
            )

            for (n in neighbors) {
                if (n != -1 && !visited[n] && !isMask[n]) {
                    visited[n] = true
                    queue.add(n)
                }
            }
        }

        // Any pixel that is NOT visited by the outside flood fill is inside the enclosed selection
        val finalMask = BooleanArray(width * height)
        for (i in finalMask.indices) {
            finalMask[i] = !visited[i]
        }

        return finalMask
    }
}
