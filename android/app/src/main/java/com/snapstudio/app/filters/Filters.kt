package com.snapstudio.app.filters

import androidx.compose.ui.graphics.Color

data class Filter(
    val id: String,
    val name: String,
    val index: Int, // Map to shader or just array index
    val swatchColors: List<Color>,
    val baseBrightness: Float = 0f, // -1 to 1 offset (0 is neutral)
    val baseContrast: Float = 1f, // multiplier (1 is neutral)
    val baseSaturation: Float = 1f, // multiplier (1 is neutral)
    val grayscale: Float = 0f, // 0 to 1
    val sepia: Float = 0f, // 0 to 1
    val hueRotateDeg: Float = 0f
) {
    fun toFloatArray(): FloatArray {
        val cm = android.graphics.ColorMatrix()
        // Brightness / Contrast
        val contrastMat = android.graphics.ColorMatrix().apply {
            val scale = baseContrast
            val translate = (-0.5f * scale + 0.5f) * 255f + (baseBrightness * 255f)
            set(floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            ))
        }
        cm.postConcat(contrastMat)
        
        // Saturation
        val satMat = android.graphics.ColorMatrix().apply { setSaturation(baseSaturation) }
        cm.postConcat(satMat)
        
        // Grayscale
        if (grayscale > 0f) {
            val grayMat = android.graphics.ColorMatrix().apply { setSaturation(1f - grayscale) }
            cm.postConcat(grayMat)
        }
        
        // Sepia
        if (sepia > 0f) {
            val sepiaMat = android.graphics.ColorMatrix().apply {
                set(floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            // For partial sepia, we'd blend. For simplicity, just concat if > 0.
            cm.postConcat(sepiaMat)
        }
        
        // Hue Rotation
        if (hueRotateDeg != 0f) {
            val cos = Math.cos(Math.toRadians(hueRotateDeg.toDouble())).toFloat()
            val sin = Math.sin(Math.toRadians(hueRotateDeg.toDouble())).toFloat()
            val lumR = 0.213f
            val lumG = 0.715f
            val lumB = 0.072f
            val hueMat = android.graphics.ColorMatrix(floatArrayOf(
                lumR + cos * (1 - lumR) + sin * (-lumR), lumG + cos * (-lumG) + sin * (-lumG), lumB + cos * (-lumB) + sin * (1 - lumB), 0f, 0f,
                lumR + cos * (-lumR) + sin * 0.143f, lumG + cos * (1 - lumG) + sin * 0.140f, lumB + cos * (-lumB) + sin * (-0.283f), 0f, 0f,
                lumR + cos * (-lumR) + sin * (-(1 - lumR)), lumG + cos * (-lumG) + sin * lumG, lumB + cos * (1 - lumB) + sin * lumB, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            cm.postConcat(hueMat)
        }
        
        return cm.array
    }
}

val FILTERS = listOf(
    Filter("original", "Original", 0, listOf(Color(0xFF6B7280), Color(0xFF9CA3AF))),
    Filter("vivid", "Vivid", 1, listOf(Color(0xFFF97316), Color(0xFFEAB308)), baseSaturation = 1.6f, baseContrast = 1.15f, baseBrightness = 0.05f),
    Filter("mono", "Mono", 2, listOf(Color(0xFF374151), Color(0xFF9CA3AF)), grayscale = 1f, baseContrast = 1.1f),
    Filter("noir", "Noir", 3, listOf(Color(0xFF0F172A), Color(0xFF475569)), grayscale = 1f, baseContrast = 1.4f, baseBrightness = -0.15f),
    Filter("warm", "Warm", 4, listOf(Color(0xFFF59E0B), Color(0xFFDC2626)), sepia = 0.35f, baseSaturation = 1.3f, baseBrightness = 0.05f, hueRotateDeg = -8f),
    Filter("cool", "Cool", 5, listOf(Color(0xFF0EA5E9), Color(0xFF6366F1)), baseSaturation = 1.2f, hueRotateDeg = 15f, baseBrightness = 0.02f, baseContrast = 1.05f),
    Filter("fade", "Fade", 6, listOf(Color(0xFFD1D5DB), Color(0xFFF3F4F6)), baseSaturation = 0.6f, baseBrightness = 0.1f, baseContrast = 0.9f),
    Filter("dream", "Dream", 7, listOf(Color(0xFFF0ABFC), Color(0xFFC4B5FD)), baseSaturation = 1.3f, baseContrast = 0.92f, baseBrightness = 0.12f),
    Filter("vintage", "Vintage", 8, listOf(Color(0xFFD97706), Color(0xFF78350F)), sepia = 0.5f, baseContrast = 1.2f, baseBrightness = -0.05f),
    Filter("cyber", "Cyber", 9, listOf(Color(0xFF06B6D4), Color(0xFFEC4899)), hueRotateDeg = 180f, baseSaturation = 1.5f, baseContrast = 1.1f),
    Filter("mint", "Mint", 10, listOf(Color(0xFF10B981), Color(0xFF34D399)), hueRotateDeg = -45f, baseSaturation = 1.1f, baseBrightness = 0.05f),
    Filter("dusk", "Dusk", 11, listOf(Color(0xFF4F46E5), Color(0xFF9333EA)), hueRotateDeg = 30f, baseSaturation = 1.2f, baseContrast = 1.1f, baseBrightness = -0.1f),
    Filter("peach", "Peach", 12, listOf(Color(0xFFFCA5A5), Color(0xFFFDE047)), hueRotateDeg = -15f, baseSaturation = 1.2f, baseBrightness = 0.08f),
    Filter("gotham", "Gotham", 13, listOf(Color(0xFF1E293B), Color(0xFF0F172A)), grayscale = 1f, baseContrast = 1.5f, baseBrightness = -0.2f),
    Filter("pastel", "Pastel", 14, listOf(Color(0xFFFBCFE8), Color(0xFFBFDBFE)), baseSaturation = 0.7f, baseBrightness = 0.15f, baseContrast = 0.85f),
    Filter("emerald", "Emerald", 15, listOf(Color(0xFF059669), Color(0xFF10B981)), hueRotateDeg = -70f, baseSaturation = 1.3f, baseContrast = 1.05f),
    Filter("rust", "Rust", 16, listOf(Color(0xFFB45309), Color(0xFF78350F)), sepia = 0.4f, hueRotateDeg = -20f, baseContrast = 1.15f),
    Filter("polar", "Polar", 17, listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD)), hueRotateDeg = 10f, baseSaturation = 0.8f, baseBrightness = 0.1f, baseContrast = 1.1f),
    Filter("golden", "Golden", 18, listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)), sepia = 0.2f, hueRotateDeg = -10f, baseSaturation = 1.4f, baseBrightness = 0.05f),
    Filter("matrix", "Matrix", 19, listOf(Color(0xFF22C55E), Color(0xFF166534)), hueRotateDeg = -120f, baseSaturation = 1.8f, baseContrast = 1.3f, baseBrightness = -0.1f),
    Filter("rose", "Rose", 20, listOf(Color(0xFFFDA4AF), Color(0xFFF43F5E)), hueRotateDeg = -30f, baseSaturation = 1.2f, baseContrast = 1.05f),
    Filter("ocean", "Ocean", 21, listOf(Color(0xFF0284C7), Color(0xFF0369A1)), hueRotateDeg = 40f, baseSaturation = 1.3f, baseContrast = 1.1f),
    Filter("sunset", "Sunset", 22, listOf(Color(0xFFF97316), Color(0xFFE11D48)), sepia = 0.3f, hueRotateDeg = -15f, baseSaturation = 1.5f),
    Filter("dracula", "Dracula", 23, listOf(Color(0xFF4C1D95), Color(0xFF1E1B4B)), hueRotateDeg = 250f, baseSaturation = 1.4f, baseContrast = 1.3f, baseBrightness = -0.1f),
    Filter("neon", "Neon", 24, listOf(Color(0xFF39FF14), Color(0xFFFF00FF)), baseSaturation = 2.0f, baseContrast = 1.3f, hueRotateDeg = 15f),
    Filter("washed", "Washed", 25, listOf(Color(0xFFD4D4D8), Color(0xFFA1A1AA)), baseSaturation = 0.4f, baseContrast = 0.8f, baseBrightness = 0.2f),
    Filter("crimson", "Crimson", 26, listOf(Color(0xFF9F1239), Color(0xFF4C0519)), hueRotateDeg = -50f, baseSaturation = 1.5f, baseContrast = 1.2f, baseBrightness = -0.1f),
    Filter("forest", "Forest", 27, listOf(Color(0xFF15803D), Color(0xFF064E3B)), hueRotateDeg = -80f, baseSaturation = 1.2f, baseContrast = 1.15f),
    Filter("indigo", "Indigo", 28, listOf(Color(0xFF4338CA), Color(0xFF312E81)), hueRotateDeg = 60f, baseSaturation = 1.3f, baseContrast = 1.2f),
    Filter("chrome", "Chrome", 29, listOf(Color(0xFFE5E7EB), Color(0xFF9CA3AF)), grayscale = 0.8f, baseContrast = 1.6f, baseBrightness = 0.05f),
    Filter("candy", "Candy", 30, listOf(Color(0xFFF472B6), Color(0xFFFB7185)), hueRotateDeg = 320f, baseSaturation = 1.4f, baseContrast = 1.1f, baseBrightness = 0.1f),
    Filter("mocha", "Mocha", 31, listOf(Color(0xFF78350F), Color(0xFF451A03)), sepia = 0.6f, hueRotateDeg = -5f, baseContrast = 1.2f, baseBrightness = -0.1f)
)
