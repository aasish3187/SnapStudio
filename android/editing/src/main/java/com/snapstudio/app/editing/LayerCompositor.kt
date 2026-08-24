package com.snapstudio.app.editing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LayerCompositor(
    layers: List<Layer>,
    selectedLayerId: String?,
    onTransform: (id: String, pan: Offset, zoom: Float, rotate: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(selectedLayerId) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    if (selectedLayerId != null) {
                        onTransform(selectedLayerId, pan, zoom, rotation)
                    }
                }
            }
    ) {
        // Draw layers sorted by zIndex
        val sortedLayers = layers.filter { it.isVisible }.sortedBy { it.zIndex }
        
        for (layer in sortedLayers) {
            val bmp = layer.bitmap.asImageBitmap()
            
            withTransform({
                // The pivot is the center of the canvas by default, or center of the image.
                // For simplicity, we apply translation, then rotation and scale.
                translate(left = layer.offset.x, top = layer.offset.y)
                // We rotate around the center of the bitmap
                val pivotX = size.width / 2f
                val pivotY = size.height / 2f
                rotate(degrees = layer.rotation, pivot = Offset(pivotX, pivotY))
                scale(scaleX = layer.scale, scaleY = layer.scale, pivot = Offset(pivotX, pivotY))
            }) {
                // Calculate destination size to fit the canvas proportionally
                val scale = Math.min(
                    size.width / bmp.width,
                    size.height / bmp.height
                )
                val dstWidth = (bmp.width * scale).toInt()
                val dstHeight = (bmp.height * scale).toInt()
                val dx = (size.width - dstWidth) / 2f
                val dy = (size.height - dstHeight) / 2f
                
                drawImage(
                    image = bmp,
                    dstOffset = androidx.compose.ui.unit.IntOffset(dx.toInt(), dy.toInt()),
                    dstSize = IntSize(dstWidth, dstHeight),
                    blendMode = layer.blendMode
                )
            }
        }
    }
}

@Composable
fun LayerStackPanel(
    layers: List<Layer>,
    selectedLayerId: String?,
    onSelectLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedLayers = layers.sortedByDescending { it.zIndex }
    
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color(0xFF1E1E1E))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Text("Layers", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        }
        items(sortedLayers) { layer ->
            val isSelected = layer.id == selectedLayerId
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Color(0xFF333333) else Color.Transparent)
                    .border(if (isSelected) 1.dp else 0.dp, if (isSelected) Color(0xFFF5A623) else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { onSelectLayer(layer.id) }
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Visibility toggle
                IconButton(onClick = { onToggleVisibility(layer.id) }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (layer.isVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = "Toggle Visibility",
                        tint = if (layer.isVisible) Color.White else Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = layer.name,
                    color = if (layer.isVisible) Color.White else Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}
