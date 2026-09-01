package com.snapstudio.app.studio

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * High-performance 1D/3D Curves Look-Up Table Engine.
 * Evaluates spline control points into 256-step channel mapping tables.
 */
object CurvesLutEngine {

    data class CurveChannelKnots(
        val master: List<MonotoneCubicSpline.PointF> = listOf(
            MonotoneCubicSpline.PointF(0f, 0f),
            MonotoneCubicSpline.PointF(255f, 255f)
        ),
        val red: List<MonotoneCubicSpline.PointF> = listOf(
            MonotoneCubicSpline.PointF(0f, 0f),
            MonotoneCubicSpline.PointF(255f, 255f)
        ),
        val green: List<MonotoneCubicSpline.PointF> = listOf(
            MonotoneCubicSpline.PointF(0f, 0f),
            MonotoneCubicSpline.PointF(255f, 255f)
        ),
        val blue: List<MonotoneCubicSpline.PointF> = listOf(
            MonotoneCubicSpline.PointF(0f, 0f),
            MonotoneCubicSpline.PointF(255f, 255f)
        )
    )

    data class LutTables(
        val rLut: IntArray,
        val gLut: IntArray,
        val bLut: IntArray
    )

    fun generateLutTables(knots: CurveChannelKnots): LutTables {
        val splineMaster = MonotoneCubicSpline(knots.master)
        val splineR = MonotoneCubicSpline(knots.red)
        val splineG = MonotoneCubicSpline(knots.green)
        val splineB = MonotoneCubicSpline(knots.blue)

        val rLut = IntArray(256)
        val gLut = IntArray(256)
        val bLut = IntArray(256)

        for (i in 0 until 256) {
            val masterVal = splineMaster.evaluate(i.toFloat())

            val r = splineR.evaluate(masterVal).roundToInt().coerceIn(0, 255)
            val g = splineG.evaluate(masterVal).roundToInt().coerceIn(0, 255)
            val b = splineB.evaluate(masterVal).roundToInt().coerceIn(0, 255)

            rLut[i] = r
            gLut[i] = g
            bLut[i] = b
        }

        return LutTables(rLut, gLut, bLut)
    }

    suspend fun applyCurves(source: Bitmap, knots: CurveChannelKnots): Bitmap = withContext(Dispatchers.Default) {
        val tables = generateLutTables(knots)
        val width = source.width
        val height = source.height
        val output = source.copy(Bitmap.Config.ARGB_8888, true)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val rLut = tables.rLut
        val gLut = tables.gLut
        val bLut = tables.bLut

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val a = Color.alpha(pixel)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)

            val newR = rLut[r]
            val newG = gLut[g]
            val newB = bLut[b]

            pixels[i] = Color.argb(a, newR, newG, newB)
        }

        output.setPixels(pixels, 0, width, 0, 0, width, height)
        output
    }
}
