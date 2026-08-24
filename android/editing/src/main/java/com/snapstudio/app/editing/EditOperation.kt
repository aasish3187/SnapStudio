package com.snapstudio.app.editing

import android.graphics.Bitmap

sealed class EditOperation {
    data class RemoveBackground(val maskBitmap: Bitmap) : EditOperation()
    data class GenerativeFill(val maskBitmap: Bitmap, val prompt: String) : EditOperation()
    data class HarmonizeLighting(val intensity: Float) : EditOperation()
    data class Upscale(val scaleFactor: Int) : EditOperation()
    data class CutoutCreated(val layerId: String) : EditOperation()
}
