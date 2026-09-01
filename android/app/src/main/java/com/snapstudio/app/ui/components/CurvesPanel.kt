package com.snapstudio.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapstudio.app.studio.MonotoneCubicSpline
import com.snapstudio.app.ui.theme.*

enum class CurveChannel(val label: String, val color: Color) {
    RGB("RGB", Color.White),
    RED("Red", Color(0xFFFF453A)),
    GREEN("Green", Color(0xFF32D74B)),
    BLUE("Blue", Color(0xFF0A84FF))
}

@Composable
fun CurvesPanel(
    luminance: Float,
    redCurve: Float,
    greenCurve: Float,
    blueCurve: Float,
    onLuminanceChanged: (Float) -> Unit,
    onRedCurveChanged: (Float) -> Unit,
    onGreenCurveChanged: (Float) -> Unit,
    onBlueCurveChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeChannel by remember { mutableStateOf(CurveChannel.RGB) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Channel Tabs (RGB, Red, Green, Blue)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CurveChannel.values().forEach { channel ->
                    val isSelected = channel == activeChannel
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) channel.color.copy(alpha = 0.25f) else Ink750)
                            .border(1.dp, if (isSelected) channel.color else Line.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .clickable { activeChannel = channel }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = channel.label,
                            color = if (isSelected) channel.color else FgMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    when (activeChannel) {
                        CurveChannel.RGB -> onLuminanceChanged(0f)
                        CurveChannel.RED -> onRedCurveChanged(0f)
                        CurveChannel.GREEN -> onGreenCurveChanged(0f)
                        CurveChannel.BLUE -> onBlueCurveChanged(0f)
                    }
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Outlined.RestartAlt, contentDescription = "Reset Curve", tint = FgMuted, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive Monotone Spline Curve Visualizer Box
        val currentKnots = remember(activeChannel, luminance, redCurve, greenCurve, blueCurve) {
            val offset = when (activeChannel) {
                CurveChannel.RGB -> luminance * 40f
                CurveChannel.RED -> redCurve * 40f
                CurveChannel.GREEN -> greenCurve * 40f
                CurveChannel.BLUE -> blueCurve * 40f
            }
            listOf(
                MonotoneCubicSpline.PointF(0f, (0f + offset * 0.3f).coerceIn(0f, 255f)),
                MonotoneCubicSpline.PointF(64f, (64f - offset * 0.4f).coerceIn(0f, 255f)),
                MonotoneCubicSpline.PointF(192f, (192f + offset * 0.4f).coerceIn(0f, 255f)),
                MonotoneCubicSpline.PointF(255f, (255f - offset * 0.3f).coerceIn(0f, 255f))
            )
        }

        val spline = remember(currentKnots) { MonotoneCubicSpline(currentKnots) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Ink800)
                .border(1.dp, Line.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw Grid Lines
                drawLine(Color.White.copy(alpha = 0.1f), Offset(w * 0.33f, 0f), Offset(w * 0.33f, h), strokeWidth = 1f)
                drawLine(Color.White.copy(alpha = 0.1f), Offset(w * 0.66f, 0f), Offset(w * 0.66f, h), strokeWidth = 1f)
                drawLine(Color.White.copy(alpha = 0.1f), Offset(0f, h * 0.33f), Offset(w, h * 0.33f), strokeWidth = 1f)
                drawLine(Color.White.copy(alpha = 0.1f), Offset(0f, h * 0.66f), Offset(w, h * 0.66f), strokeWidth = 1f)

                // Draw Linear Diagonal Reference
                drawLine(Color.White.copy(alpha = 0.15f), Offset(0f, h), Offset(w, 0f), strokeWidth = 1f)

                // Draw Evaluated Monotone Cubic Spline Path
                val path = Path()
                val steps = 64
                for (step in 0..steps) {
                    val inputVal = (step.toFloat() / steps) * 255f
                    val outputVal = spline.evaluate(inputVal)
                    val x = (step.toFloat() / steps) * w
                    val y = h - (outputVal / 255f) * h
                    if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(path, color = activeChannel.color, style = Stroke(width = 2.5f))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Fine Adjustment Slider for Active Channel
        when (activeChannel) {
            CurveChannel.RGB -> {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Luminance", color = FgFaint, fontSize = 12.sp, modifier = Modifier.width(76.dp))
                    Slider(
                        value = luminance,
                        onValueChange = onLuminanceChanged,
                        valueRange = -1f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Ink700)
                    )
                    Text("${(luminance * 100).toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(42.dp))
                }
            }
            CurveChannel.RED -> {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Red S-Curve", color = FgFaint, fontSize = 12.sp, modifier = Modifier.width(76.dp))
                    Slider(
                        value = redCurve,
                        onValueChange = onRedCurveChanged,
                        valueRange = -1f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFFF453A), activeTrackColor = Color(0xFFFF453A), inactiveTrackColor = Ink700)
                    )
                    Text("${(redCurve * 100).toInt()}", color = Color(0xFFFF453A), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(42.dp))
                }
            }
            CurveChannel.GREEN -> {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Green S-Curve", color = FgFaint, fontSize = 12.sp, modifier = Modifier.width(76.dp))
                    Slider(
                        value = greenCurve,
                        onValueChange = onGreenCurveChanged,
                        valueRange = -1f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF32D74B), activeTrackColor = Color(0xFF32D74B), inactiveTrackColor = Ink700)
                    )
                    Text("${(greenCurve * 100).toInt()}", color = Color(0xFF32D74B), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(42.dp))
                }
            }
            CurveChannel.BLUE -> {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Blue S-Curve", color = FgFaint, fontSize = 12.sp, modifier = Modifier.width(76.dp))
                    Slider(
                        value = blueCurve,
                        onValueChange = onBlueCurveChanged,
                        valueRange = -1f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF0A84FF), activeTrackColor = Color(0xFF0A84FF), inactiveTrackColor = Ink700)
                    )
                    Text("${(blueCurve * 100).toInt()}", color = Color(0xFF0A84FF), fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(42.dp))
                }
            }
        }
    }
}
