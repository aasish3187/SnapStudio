package com.snapstudio.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import com.snapstudio.app.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun BespokeSlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    onValueChange: (Float) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    bipolar: Boolean = true // True for brightness/contrast (-1 to 1), false for saturation (0 to 2)
) {
    var isDragging by remember { mutableStateOf(false) }
    var trackSize by remember { mutableStateOf(IntSize.Zero) }

    val pct = ((value - min) / (max - min)).coerceIn(0f, 1f)
    val zeroPct = if (bipolar) ((0f - min) / (max - min)) else 0f

    val startPct = minOf(zeroPct, pct)
    val endPct = maxOf(zeroPct, pct)
    val fillWidthPct = endPct - startPct

    val thumbSize by animateDpAsState(
        targetValue = if (isDragging) 22.dp else 18.dp,
        animationSpec = tween(150),
        label = "thumbSize"
    )

    val valueColor by animateColorAsState(
        targetValue = if (value == if(bipolar) 0f else 1f) FgFaint else Amber, // Saturation defaults to 1.0, others to 0.0
        label = "valueColor"
    )
    
    val displayValue = if (bipolar) value else (value - 1f) // Adjust display for non-bipolar (saturation 1.0 -> 0.0)
    val displayString = buildString {
        if (displayValue > 0) append("+")
        append(String.format("%.2f", displayValue))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onReset() }
                )
            }
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = label,
                color = Fg,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onReset() }
            )
            Text(
                text = displayString,
                color = valueColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }

        // Track Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .onSizeChanged { trackSize = it }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, _ ->
                            if (trackSize.width > 0) {
                                val dx = change.position.x / trackSize.width.toFloat()
                                val newValue = min + dx * (max - min)
                                onValueChange(newValue.coerceIn(min, max))
                            }
                        }
                    )
                }
        ) {
            // Background Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(LineStrong)
            )

            // Fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(fillWidthPct)
                    .height(3.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = (trackSize.width * startPct).dp / LocalDensity.current.density)
                    .clip(CircleShape)
                    .background(Amber)
            )

            // Zero Tick
            if (bipolar) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(8.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = (trackSize.width * zeroPct).dp / LocalDensity.current.density)
                        .background(FgFaint)
                )
            }

            // Thumb
            Box(
                modifier = Modifier
                    .size(thumbSize)
                    .align(Alignment.CenterStart)
                    .offset(x = ((trackSize.width * pct).dp / LocalDensity.current.density) - (thumbSize / 2))
                    .shadow(
                        elevation = if (isDragging) 14.dp else 4.dp,
                        shape = CircleShape,
                        spotColor = if (isDragging) AmberGlow else Color.Black,
                        ambientColor = if (isDragging) AmberGlow else Color.Black
                    )
                    .clip(CircleShape)
                    .background(Fg)
            )
        }
    }
}
