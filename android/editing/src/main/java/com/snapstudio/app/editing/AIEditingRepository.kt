package com.snapstudio.app.editing

import android.graphics.Bitmap

interface AIEditingRepository {
    /**
     * Extracts the subject from the background.
     * Returns a mask Bitmap where white is foreground, black is background.
     */
    suspend fun segmentSubject(bitmap: Bitmap): Bitmap

    /**
     * Removes an object from the image based on the mask using inpainting.
     */
    suspend fun removeObject(bitmap: Bitmap, mask: Bitmap): Bitmap

    /**
     * Replaces an area defined by the mask with AI-generated content.
     */
    suspend fun generativeFill(bitmap: Bitmap, mask: Bitmap, prompt: String): Bitmap
    
    /**
     * Harmonizes the lighting of the foreground subject to match the background.
     */
    suspend fun harmonizeLighting(compositedBitmap: Bitmap): Bitmap

    /**
     * Upscales the image using an SR model.
     */
    suspend fun upscale(bitmap: Bitmap, scaleFactor: Int): Bitmap
}
