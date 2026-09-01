package com.snapstudio.app.ui.components

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.snapstudio.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

@Composable
fun StudioHistogram(
    bitmap: Bitmap?,
    modifier: Modifier = Modifier
) {
    var rBins by remember { mutableStateOf(FloatArray(64)) }
    var gBins by remember { mutableStateOf(FloatArray(64)) }
    var bBins by remember { mutableStateOf(FloatArray(64)) }
    var lumBins by remember { mutableStateOf(FloatArray(64)) }

    LaunchedEffect(bitmap) {
        if (bitmap == null) return@LaunchedEffect
        withContext(Dispatchers.Default) {
            // Downsample for sub-millisecond histogram computation
            val step = max(1, (bitmap.width * bitmap.height) / 4000)
            val w = bitmap.width
            val h = bitmap.height

            val r = FloatArray(64)
            val g = FloatArray(64)
            val b = FloatArray(64)
            val lum = FloatArray(64)

            val pixels = IntArray(w * h)
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

            var maxVal = 1f
            for (i in pixels.indices step step) {
                val p = pixels[i]
                val red = Color.red(p) / 4
                val green = Color.green(p) / 4
                val blue = Color.blue(p) / 4
                val l = ((red * 0.2126f + green * 0.7152f + blue * 0.0722f)).toInt().coerceIn(0, 63)

                r[red]++
                g[green]++
                b[blue]++
                lum[l]++

                maxVal = max(maxVal, max(r[red], max(g[green], max(b[blue], lum[l]))))
            }

            // Normalize
            for (k in 0 until 64) {
                r[k] /= maxVal
                g[k] /= maxVal
                b[k] /= maxVal
                lum[k] /= maxVal
            }

            rBins = r
            gBins = g
            bBins = b
            lumBins = lum
        }
    }

    Box(
        modifier = modifier
            .width(84.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ComposeColor.Black.copy(alpha = 0.55f))
            .border(1.dp, ComposeColor.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            fun drawChannel(bins: FloatArray, color: ComposeColor) {
                val path = Path()
                for (i in 0 until 64) {
                    val x = (i / 63f) * width
                    val y = height - (bins[i] * height)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color = color.copy(alpha = 0.75f), style = Stroke(width = 1.2f))
            }

            drawChannel(lumBins, ComposeColor.White.copy(alpha = 0.4f))
            drawChannel(rBins, ComposeColor(0xFFFF453A))
            drawChannel(gBins, ComposeColor(0xFF32D74B))
            drawChannel(bBins, ComposeColor(0xFF0A84FF))
        }
    }
}
