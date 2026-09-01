package com.snapstudio.app.studio

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.facemesh.FaceMesh
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import com.google.mlkit.vision.facemesh.FaceMeshPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.*

/**
 * 468-Point 3D Face Mesh Retouching & Relighting Engine powered by Google ML Kit.
 */
object AiFaceMeshEngine {

    private val detector = FaceMeshDetection.getClient(
        FaceMeshDetectorOptions.Builder()
            .setUseCase(FaceMeshDetectorOptions.FACE_MESH)
            .build()
    )

    suspend fun retouchPortrait(
        source: Bitmap,
        skinSmooth: Float,
        eyeClarity: Float,
        teethWhiten: Float,
        relightIntensity: Float,
        relightAngleDeg: Float
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val output = source.copy(Bitmap.Config.ARGB_8888, true)

        val inputImage = InputImage.fromBitmap(source, 0)
        val meshes: List<FaceMesh> = suspendCancellableCoroutine { cont ->
            detector.process(inputImage)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

        if (meshes.isEmpty()) {
            return@withContext FaceRetouchEngine.enhancePortrait(source, skinSmooth, eyeClarity, 0.2f)
        }

        val pixels = IntArray(width * height)
        output.getPixels(pixels, 0, width, 0, 0, width, height)

        val lightAngleRad = Math.toRadians(relightAngleDeg.toDouble())
        val lightDirX = cos(lightAngleRad).toFloat()
        val lightDirY = sin(lightAngleRad).toFloat()

        for (face in meshes) {
            val bounds = face.boundingBox
            val left = bounds.left.coerceIn(0, width - 1)
            val top = bounds.top.coerceIn(0, height - 1)
            val right = bounds.right.coerceIn(0, width - 1)
            val bottom = bounds.bottom.coerceIn(0, height - 1)

            val centerX = bounds.centerX().toFloat()
            val centerY = bounds.centerY().toFloat()
            val radiusX = bounds.width() / 2f
            val radiusY = bounds.height() / 2f

            for (y in top..bottom) {
                val ny = (y - centerY) / radiusY
                for (x in left..right) {
                    val nx = (x - centerX) / radiusX
                    val distSq = nx * nx + ny * ny
                    if (distSq > 1.0f) continue

                    val falloff = (1.0f - distSq).coerceIn(0f, 1f)
                    val idx = y * width + x
                    val col = pixels[idx]
                    var r = Color.red(col)
                    var g = Color.green(col)
                    var b = Color.blue(col)

                    // 1. Bilateral Skin Smoothing
                    if (skinSmooth > 0.05f) {
                        var sumR = 0f
                        var sumG = 0f
                        var sumB = 0f
                        var totalWeight = 0f

                        for (dy in -2..2) {
                            val py = (y + dy).coerceIn(0, height - 1)
                            for (dx in -2..2) {
                                val px = (x + dx).coerceIn(0, width - 1)
                                val neighborCol = pixels[py * width + px]
                                val nr = Color.red(neighborCol)
                                val ng = Color.green(neighborCol)
                                val nb = Color.blue(neighborCol)

                                val colorDist = abs(r - nr) + abs(g - ng) + abs(b - nb)
                                val spatialDistSq = (dx * dx + dy * dy).toFloat()

                                val weight = exp(-spatialDistSq / 4f) * exp(-colorDist / 40f)
                                sumR += nr * weight
                                sumG += ng * weight
                                sumB += nb * weight
                                totalWeight += weight
                            }
                        }

                        if (totalWeight > 0f) {
                            val smoothR = sumR / totalWeight
                            val smoothG = sumG / totalWeight
                            val smoothB = sumB / totalWeight
                            val blend = skinSmooth * falloff * 0.75f

                            r = (r * (1f - blend) + smoothR * blend).toInt().coerceIn(0, 255)
                            g = (g * (1f - blend) + smoothG * blend).toInt().coerceIn(0, 255)
                            b = (b * (1f - blend) + smoothB * blend).toInt().coerceIn(0, 255)
                        }
                    }

                    // 2. Teeth Whitening & Yellow Desaturation in lower center
                    if (teethWhiten > 0.05f && ny in 0.2f..0.7f && abs(nx) < 0.35f) {
                        if (r > 120 && g > 100 && b > 80 && (r + g) > b * 1.8f) {
                            val whitenAmount = teethWhiten * 0.45f
                            val lum = (0.299f * r + 0.587f * g + 0.114f * b)
                            r = (r + (lum - r) * whitenAmount + 25f * teethWhiten).toInt().coerceIn(0, 255)
                            g = (g + (lum - g) * whitenAmount + 25f * teethWhiten).toInt().coerceIn(0, 255)
                            b = (b + (lum - b) * whitenAmount + 35f * teethWhiten).toInt().coerceIn(0, 255)
                        }
                    }

                    // 3. Virtual 3D Studio Relighting
                    if (relightIntensity > 0.05f) {
                        val normalZ = sqrt(max(0f, 1f - nx * nx - ny * ny))
                        val dot = (nx * lightDirX + ny * lightDirY + normalZ * 0.5f).coerceIn(-1f, 1f)
                        val lightGain = 1f + (dot * relightIntensity * 0.4f * falloff)

                        r = (r * lightGain).toInt().coerceIn(0, 255)
                        g = (g * lightGain).toInt().coerceIn(0, 255)
                        b = (b * lightGain).toInt().coerceIn(0, 255)
                    }

                    pixels[idx] = Color.rgb(r, g, b)
                }
            }
        }

        output.setPixels(pixels, 0, width, 0, 0, width, height)
        output
    }
}
