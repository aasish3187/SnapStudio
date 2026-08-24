package com.snapstudio.app.filters

object ColorMatrixUtils {

    // Identity Matrix
    val NORMAL = floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )

    // Black and White
    val BLACK_AND_WHITE = floatArrayOf(
        0.33f, 0.59f, 0.11f, 0f, 0f,
        0.33f, 0.59f, 0.11f, 0f, 0f,
        0.33f, 0.59f, 0.11f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )

    // Vintage / Sepia
    val VINTAGE = floatArrayOf(
        0.393f, 0.769f, 0.189f, 0f, 0f,
        0.349f, 0.686f, 0.168f, 0f, 0f,
        0.272f, 0.534f, 0.131f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )
    
    // Cool
    val COOL = floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1.25f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )
    
    // Warm
    val WARM = floatArrayOf(
        1.25f, 0f, 0f, 0f, 0f,
        0f, 1.1f, 0f, 0f, 0f,
        0f, 0f, 0.8f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )
    
    // Noir (High Contrast BW)
    val NOIR = floatArrayOf(
        0.5f, 0.5f, 0.5f, 0f, -30f,
        0.5f, 0.5f, 0.5f, 0f, -30f,
        0.5f, 0.5f, 0.5f, 0f, -30f,
        0f, 0f, 0f, 1f, 0f
    )
    
    // Drama (Desaturated, High Contrast)
    val DRAMA = floatArrayOf(
        0.8f, 0.1f, 0.1f, 0f, -20f,
        0.1f, 0.8f, 0.1f, 0f, -20f,
        0.1f, 0.1f, 0.8f, 0f, -20f,
        0f, 0f, 0f, 1f, 0f
    )
    
    // Grunge (Dark, tinted)
    val GRUNGE = floatArrayOf(
        0.6f, 0.1f, 0.1f, 0f, -40f,
        0.1f, 0.5f, 0.1f, 0f, -40f,
        0.1f, 0.1f, 0.4f, 0f, -40f,
        0f, 0f, 0f, 1f, 0f
    )
    
    // Retrolux
    val RETROLUX = floatArrayOf(
        1.1f, 0.2f, 0f, 0f, 20f,
        0.2f, 1.0f, 0f, 0f, 10f,
        0f, 0.2f, 0.7f, 0f, -10f,
        0f, 0f, 0f, 1f, 0f
    )
    
    // HDR-scape (High Contrast & Saturation)
    val HDR_SCAPE = floatArrayOf(
        1.5f, -0.2f, -0.2f, 0f, 10f,
        -0.2f, 1.5f, -0.2f, 0f, 10f,
        -0.2f, -0.2f, 1.5f, 0f, 10f,
        0f, 0f, 0f, 1f, 0f
    )
}
