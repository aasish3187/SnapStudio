package com.snapstudio.app.studio

import kotlin.math.*

/**
 * Fritsch-Carlson Monotone Cubic Spline Interpolation.
 * Guarantees monotonic output without overshoots or color-clipping artifacts.
 */
class MonotoneCubicSpline(points: List<PointF>) {
    data class PointF(val x: Float, val y: Float)

    private val xs: FloatArray
    private val ys: FloatArray
    private val m: FloatArray
    private val n: Int

    init {
        val sorted = points.sortedBy { it.x }
        n = sorted.size
        xs = FloatArray(n) { sorted[it].x }
        ys = FloatArray(n) { sorted[it].y }
        m = FloatArray(n)

        if (n >= 2) {
            val dx = FloatArray(n - 1)
            val dy = FloatArray(n - 1)
            val slopes = FloatArray(n - 1)

            for (i in 0 until n - 1) {
                dx[i] = xs[i + 1] - xs[i]
                dy[i] = ys[i + 1] - ys[i]
                slopes[i] = if (abs(dx[i]) > 1e-5f) dy[i] / dx[i] else 0f
            }

            // Tangents initialized as average of adjacent secants
            m[0] = slopes[0]
            for (i in 1 until n - 1) {
                m[i] = (slopes[i - 1] + slopes[i]) * 0.5f
            }
            m[n - 1] = slopes[n - 2]

            // Fritsch-Carlson monotonicity condition
            for (i in 0 until n - 1) {
                if (abs(slopes[i]) < 1e-5f) {
                    m[i] = 0f
                    m[i + 1] = 0f
                } else {
                    val alpha = m[i] / slopes[i]
                    val beta = m[i + 1] / slopes[i]
                    val dist = alpha * alpha + beta * beta
                    if (dist > 9.0f) {
                        val tau = 3.0f / sqrt(dist)
                        m[i] = tau * alpha * slopes[i]
                        m[i + 1] = tau * beta * slopes[i]
                    }
                }
            }
        }
    }

    fun evaluate(x: Float): Float {
        if (n == 0) return x
        if (n == 1) return ys[0]
        if (x <= xs[0]) return ys[0]
        if (x >= xs[n - 1]) return ys[n - 1]

        var i = 0
        while (i < n - 1 && xs[i + 1] < x) {
            i++
        }

        val h = xs[i + 1] - xs[i]
        if (abs(h) < 1e-5f) return ys[i]

        val t = (x - xs[i]) / h
        val t2 = t * t
        val t3 = t2 * t

        val h00 = 2f * t3 - 3f * t2 + 1f
        val h10 = t3 - 2f * t2 + t
        val h01 = -2f * t3 + 3f * t2
        val h11 = t3 - t2

        val value = h00 * ys[i] + h10 * h * m[i] + h01 * ys[i + 1] + h11 * h * m[i + 1]
        return value.coerceIn(0f, 255f)
    }
}
