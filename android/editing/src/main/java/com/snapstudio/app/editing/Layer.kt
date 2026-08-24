package com.snapstudio.app.editing

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import java.util.UUID

data class Layer(
    val id: String = UUID.randomUUID().toString(),
    val bitmap: Bitmap,
    var mask: Bitmap? = null,
    var offset: Offset = Offset.Zero,
    var scale: Float = 1f,
    var rotation: Float = 0f,
    var blendMode: BlendMode = BlendMode.SrcOver,
    var isVisible: Boolean = true,
    var zIndex: Int = 0,
    val name: String = "Layer"
)
