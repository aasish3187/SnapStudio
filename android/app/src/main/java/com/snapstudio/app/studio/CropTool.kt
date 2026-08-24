package com.snapstudio.app.studio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun CropToolOverlay(onCropUpdated: (Float, Float, Float, Float) -> Unit) {
    var cropRect by remember { mutableStateOf(Rect(100f, 100f, 400f, 400f)) }

    Canvas(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                // Stub: Move the whole rect instead of resizing for simplicity
                cropRect = cropRect.translate(dragAmount.x, dragAmount.y)
                // Normalize and send to C++ in a real implementation
                // onCropUpdated(...)
            }
        }
    ) {
        drawRect(
            color = Color.White,
            topLeft = cropRect.topLeft,
            size = cropRect.size,
            style = Stroke(width = 4f)
        )
    }
}
