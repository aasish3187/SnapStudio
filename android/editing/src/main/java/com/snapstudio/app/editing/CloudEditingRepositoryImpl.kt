package com.snapstudio.app.editing

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CloudEditingRepositoryImpl(private val apiKey: String) : AIEditingRepository {

    override suspend fun segmentSubject(bitmap: Bitmap): Bitmap {
        throw NotImplementedError("Segmentation is done on-device via OnDeviceEditingRepositoryImpl")
    }

    override suspend fun removeObject(bitmap: Bitmap, mask: Bitmap): Bitmap {
        // Vertex AI / Imagen 3 API integration would go here.
        // Convert Bitmap -> Base64 -> JSON Payload -> Retrofit -> Base64 -> Bitmap
        return withContext(Dispatchers.IO) {
            bitmap // Placeholder
        }
    }

    override suspend fun generativeFill(bitmap: Bitmap, mask: Bitmap, prompt: String): Bitmap {
        // Vertex AI Imagen 3 Edit API Call
        return withContext(Dispatchers.IO) {
            bitmap // Placeholder
        }
    }

    override suspend fun harmonizeLighting(compositedBitmap: Bitmap): Bitmap {
        // img2img API call with low denoise strength for harmonization
        return withContext(Dispatchers.IO) {
            compositedBitmap // Placeholder
        }
    }

    override suspend fun upscale(bitmap: Bitmap, scaleFactor: Int): Bitmap {
        throw NotImplementedError("Upscaling is typically done on-device to avoid massive bandwidth usage")
    }
}
