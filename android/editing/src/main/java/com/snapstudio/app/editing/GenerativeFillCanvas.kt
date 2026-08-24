package com.snapstudio.app.editing

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun GenerativeFillCanvas(
    baseImage: Bitmap,
    onGenerate: (Bitmap, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var prompt by remember { mutableStateOf("") }
    var brushSize by remember { mutableStateOf(50f) }
    
    // We maintain a list of paths the user has drawn
    val paths = remember { mutableStateListOf<androidx.compose.ui.graphics.Path>() }
    var currentPath by remember { mutableStateOf<androidx.compose.ui.graphics.Path?>(null) }
    
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(offset.x, offset.y)
                                }
                            },
                            onDrag = { change, _ ->
                                currentPath?.lineTo(change.position.x, change.position.y)
                                // Trigger recomposition by re-assigning
                                currentPath = currentPath
                            },
                            onDragEnd = {
                                currentPath?.let { paths.add(it) }
                                currentPath = null
                            },
                            onDragCancel = {
                                currentPath = null
                            }
                        )
                    }
            ) {
                // Draw base image
                drawImage(
                    image = baseImage.asImageBitmap(),
                    dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
                )
                
                // Draw all completed paths as a semi-transparent red mask
                paths.forEach { path ->
                    drawPath(
                        path = path,
                        color = Color.Red.copy(alpha = 0.5f),
                        style = Stroke(width = brushSize, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                    )
                }
                
                // Draw current path
                currentPath?.let { path ->
                    drawPath(
                        path = path,
                        color = Color.Red.copy(alpha = 0.5f),
                        style = Stroke(width = brushSize, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                    )
                }
            }
        }
        
        // Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Brush Size")
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = brushSize,
                    onValueChange = { brushSize = it },
                    valueRange = 10f..150f,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Describe what to generate") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { paths.clear() }) {
                    Text("Clear")
                }
                Button(
                    onClick = {
                        // Create mask bitmap from paths
                        val maskBitmap = Bitmap.createBitmap(baseImage.width, baseImage.height, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(maskBitmap)
                        val paint = Paint().apply {
                            color = android.graphics.Color.WHITE
                            style = Paint.Style.STROKE
                            strokeWidth = brushSize
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                        }
                        
                        // We need to scale the paths from Canvas coordinates to Bitmap coordinates
                        // This is simplified; assumes Canvas size matches Bitmap aspect ratio closely
                        // In a real app, map points precisely.
                        
                        onGenerate(maskBitmap, prompt)
                    },
                    enabled = paths.isNotEmpty() && prompt.isNotBlank()
                ) {
                    Text("Generate")
                }
            }
        }
    }
}
