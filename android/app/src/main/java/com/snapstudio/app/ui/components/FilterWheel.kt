package com.snapstudio.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapstudio.app.filters.Filter
import com.snapstudio.app.ui.theme.Amber
import com.snapstudio.app.ui.theme.AmberGlow
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun FilterWheel(
    filters: List<Filter>,
    activeIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val stepDeg = 20f
    val radiusDp = 260.dp
    val density = LocalDensity.current
    val radiusPx = with(density) { radiusDp.toPx() }

    var rotation by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Keep rotation in sync when activeIndex changes externally
    LaunchedEffect(activeIndex) {
        if (!isDragging) {
            rotation = -activeIndex * stepDeg
        }
    }

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        label = "rotation"
    )

    fun angleToIndex(rot: Float): Int {
        val raw = -rot / stepDeg
        return raw.roundToInt().coerceIn(0, filters.size - 1)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        val idx = angleToIndex(rotation)
                        onSelect(idx)
                        rotation = -idx * stepDeg
                    },
                    onDragCancel = {
                        isDragging = false
                        val idx = angleToIndex(rotation)
                        onSelect(idx)
                        rotation = -idx * stepDeg
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val deltaDeg = dragAmount * 0.25f
                        val min = -(filters.size - 1) * stepDeg
                        rotation = (rotation + deltaDeg).coerceIn(min, 0f)
                    }
                )
            }
    ) {
        // Display the active filter name at the top
        Text(
            text = filters.getOrNull(activeIndex)?.name ?: "",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        val currentRot = if (isDragging) rotation else animatedRotation
        val arcCenterYPx = with(density) { 50.dp.toPx() }

        filters.forEachIndexed { i, filter ->
            val angleDeg = i * stepDeg + currentRot
            val rad = (angleDeg - 90f) * (Math.PI / 180f)
            val xPx = (cos(rad) * radiusPx).toFloat()
            val yPx = (sin(rad) * radiusPx).toFloat()

            // Shift so the top of the circle is at arcCenterYPx
            val finalYPx = yPx + radiusPx + arcCenterYPx

            val isActive = i == activeIndex
            val distFromTop = Math.abs(angleDeg)
            val opacity = Math.max(0.0f, 1f - distFromTop / 80f)

            // Scale tapers from 1.3f at center to 0.5f at edges
            val scale = Math.max(0.4f, 1.3f - (distFromTop / 40f) * 0.5f)

            if (opacity > 0.05f) {
                val filterMatrix = remember(filter) { androidx.compose.ui.graphics.ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix(filter.toFloatArray())) }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset { IntOffset(xPx.roundToInt(), finalYPx.roundToInt()) }
                        .size(56.dp * scale)
                        .border(
                            width = if (isActive) 3.dp else 2.dp,
                            color = if (isActive) Color.White else Color(0xFF333333).copy(alpha = opacity),
                            shape = CircleShape
                        )
                        .padding(if (isActive) 4.dp else 0.dp)
                        .clip(CircleShape)
                        .clickable { onSelect(i) }
                ) {
                    coil.compose.AsyncImage(
                        model = "https://images.unsplash.com/photo-1433086966358-54859d0ed716?w=200&q=80",
                        contentDescription = "Filter Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        colorFilter = filterMatrix
                    )
                }
            }
        }
    }
}
