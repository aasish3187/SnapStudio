package com.snapstudio.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.snapstudio.app.ui.theme.Amber
import com.snapstudio.app.ui.theme.AmberGlow
import com.snapstudio.app.ui.theme.Fg

@Composable
fun ShutterButton(
    onCapture: () -> Unit,
    isRecording: Boolean = false,
    mode: String = "photo",
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val ringScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "ringScale"
    )

    val discSize by animateDpAsState(
        targetValue = if (isRecording) 32.dp else if (isPressed) 52.dp else 60.dp,
        animationSpec = tween(durationMillis = 150),
        label = "discSize"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isRecording) 8.dp else 30.dp,
        animationSpec = tween(durationMillis = 200),
        label = "cornerRadius"
    )

    val discColor by animateColorAsState(
        targetValue = if (isRecording || mode == "video") Color(0xFFFF3B30) else Amber,
        animationSpec = tween(durationMillis = 200),
        label = "discColor"
    )
    
    val shadowSize = if (isPressed || isRecording) 0.dp else 22.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(78.dp)
            .pointerInput(Unit) {
                while (true) {
                    awaitPointerEventScope {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        val up = waitForUpOrCancellation()
                        isPressed = false
                        if (up != null) {
                            onCapture()
                        }
                    }
                }
            }
    ) {
        // Outer ring
        Box(
            modifier = Modifier
                .matchParentSize()
                .scale(ringScale)
                .border(3.5.dp, if (isRecording) Color(0xFFFF3B30) else Fg, CircleShape)
        )

        // Inner disc / square
        Box(
            modifier = Modifier
                .size(discSize)
                .shadow(
                    elevation = shadowSize,
                    shape = RoundedCornerShape(cornerRadius),
                    spotColor = if (mode == "video") Color(0xFFFF3B30) else AmberGlow,
                    ambientColor = if (mode == "video") Color(0xFFFF3B30) else AmberGlow
                )
                .clip(RoundedCornerShape(cornerRadius))
                .background(discColor)
        )
    }
}

