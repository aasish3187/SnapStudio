package com.snapstudio.app.studio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun DrawToolOverlay() {
    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    Canvas(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    currentPath = Path().apply { moveTo(offset.x, offset.y) }
                },
                onDragEnd = {
                    currentPath?.let { paths.add(it) }
                    currentPath = null
                },
                onDragCancel = { currentPath = null }
            ) { change, _ ->
                change.consume()
                currentPath?.lineTo(change.position.x, change.position.y)
            }
        }
    ) {
        for (path in paths) {
            drawPath(path, color = Color.Red, style = Stroke(width = 10f))
        }
        currentPath?.let {
            drawPath(it, color = Color.Red, style = Stroke(width = 10f))
        }
    }
}
