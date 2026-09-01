package com.snapstudio.app.studio

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
data class SelectiveControlPoint(
    val id: String = UUID.randomUUID().toString(),
    val x: Float = 0.5f, // Normalized x (0f..1f)
    val y: Float = 0.5f, // Normalized y (0f..1f)
    val radius: Float = 180f, // Radius in image pixels
    val brightness: Float = 0f, // -1f..1f (maps to -2EV..+2EV)
    val contrast: Float = 1f, // 0.2f..2.5f
    val saturation: Float = 1f, // 0f..2.5f
    val temperature: Float = 0f // -1f..1f
)
