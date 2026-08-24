package com.snapstudio.app.filters

import androidx.compose.ui.graphics.Color

object FilterPresetRepository {

    val allFilters: List<FilterPreset> = listOf(
        // TIER 1: Classic (Color Matrix)
        FilterPreset.ColorMatrix(
            id = "original",
            name = "Original",
            thumbnailColors = listOf(Color(0xFF6B7280), Color(0xFF9CA3AF)),
            matrix = ColorMatrixUtils.NORMAL
        ),
        FilterPreset.ColorMatrix(
            id = "vivid",
            name = "Vivid",
            thumbnailColors = listOf(Color(0xFFF97316), Color(0xFFEAB308)),
            matrix = floatArrayOf(
                1.2f, 0f, 0f, 0f, 10f,
                0f, 1.2f, 0f, 0f, 10f,
                0f, 0f, 1.2f, 0f, 10f,
                0f, 0f, 0f, 1f, 0f
            )
        ),
        FilterPreset.ColorMatrix(
            id = "mono",
            name = "Mono",
            thumbnailColors = listOf(Color(0xFF374151), Color(0xFF9CA3AF)),
            matrix = ColorMatrixUtils.BLACK_AND_WHITE
        ),
        FilterPreset.ColorMatrix(
            id = "noir_classic_matrix",
            name = "Noir",
            thumbnailColors = listOf(Color(0xFF0F172A), Color(0xFF475569)),
            matrix = ColorMatrixUtils.NOIR
        ),
        FilterPreset.ColorMatrix(
            id = "warm",
            name = "Warm",
            thumbnailColors = listOf(Color(0xFFF59E0B), Color(0xFFDC2626)),
            matrix = ColorMatrixUtils.WARM
        ),
        FilterPreset.ColorMatrix(
            id = "cool",
            name = "Cool",
            thumbnailColors = listOf(Color(0xFF0EA5E9), Color(0xFF6366F1)),
            matrix = ColorMatrixUtils.COOL
        ),
        FilterPreset.ColorMatrix(
            id = "sepia",
            name = "Sepia",
            thumbnailColors = listOf(Color(0xFFD97706), Color(0xFF78350F)),
            matrix = ColorMatrixUtils.VINTAGE
        ),
        FilterPreset.ColorMatrix(
            id = "faded",
            name = "Faded",
            thumbnailColors = listOf(Color(0xFFD1D5DB), Color(0xFFF3F4F6)),
            matrix = floatArrayOf(
                0.8f, 0f, 0f, 0f, 20f,
                0f, 0.8f, 0f, 0f, 20f,
                0f, 0f, 0.8f, 0f, 20f,
                0f, 0f, 0f, 1f, 0f
            )
        ),
        FilterPreset.ColorMatrix(
            id = "cross_process",
            name = "Cross Process",
            thumbnailColors = listOf(Color(0xFF10B981), Color(0xFF34D399)),
            matrix = floatArrayOf(
                1.2f, 0f, 0f, 0f, 10f,
                0f, 1.1f, 0f, 0f, 0f,
                0f, 0f, 0.8f, 0f, 20f,
                0f, 0f, 0f, 1f, 0f
            )
        ),
        FilterPreset.ColorMatrix(
            id = "golden_hour",
            name = "Golden Hour",
            thumbnailColors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
            matrix = floatArrayOf(
                1.1f, 0f, 0f, 0f, 15f,
                0f, 1.05f, 0f, 0f, 10f,
                0f, 0f, 0.8f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
        ),
        FilterPreset.ColorMatrix(
            id = "moody_blue",
            name = "Moody Blue",
            thumbnailColors = listOf(Color(0xFF4F46E5), Color(0xFF9333EA)),
            matrix = floatArrayOf(
                0.9f, 0f, 0f, 0f, -10f,
                0f, 0.95f, 0f, 0f, 5f,
                0f, 0f, 1.2f, 0f, 20f,
                0f, 0f, 0f, 1f, 0f
            )
        ),
        FilterPreset.ColorMatrix(
            id = "rose",
            name = "Rose",
            thumbnailColors = listOf(Color(0xFFFDA4AF), Color(0xFFF43F5E)),
            matrix = floatArrayOf(
                1.1f, 0f, 0f, 0f, 15f,
                0f, 0.9f, 0f, 0f, 0f,
                0f, 0f, 0.95f, 0f, 5f,
                0f, 0f, 0f, 1f, 0f
            )
        ),
        FilterPreset.ColorMatrix(
            id = "high_contrast",
            name = "High Contrast",
            thumbnailColors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
            matrix = floatArrayOf(
                1.5f, 0f, 0f, 0f, -32f,
                0f, 1.5f, 0f, 0f, -32f,
                0f, 0f, 1.5f, 0f, -32f,
                0f, 0f, 0f, 1f, 0f
            )
        ),
        FilterPreset.ColorMatrix(
            id = "grayscale_blue",
            name = "Grayscale Blue",
            thumbnailColors = listOf(Color(0xFF94A3B8), Color(0xFF475569)),
            matrix = floatArrayOf(
                0.2f, 0.4f, 0.1f, 0f, 0f,
                0.2f, 0.4f, 0.1f, 0f, 0f,
                0.25f, 0.45f, 0.15f, 0f, 20f,
                0f, 0f, 0f, 1f, 0f
            )
        ),
        FilterPreset.ColorMatrix(
            id = "antique",
            name = "Antique",
            thumbnailColors = listOf(Color(0xFFD4D4D8), Color(0xFFA1A1AA)),
            matrix = floatArrayOf(
                0.9f, 0.1f, 0f, 0f, 10f,
                0f, 0.85f, 0.05f, 0f, 5f,
                0f, 0f, 0.7f, 0f, -10f,
                0f, 0f, 0f, 1f, 0f
            )
        ),

        // TIER 2: Creative (Shader-based)
        FilterPreset.ShaderEffect(
            id = "vignette_classic",
            name = "Vignette",
            thumbnailColors = listOf(Color(0xFF000000), Color(0xFF333333)),
            vignette = 0.6f
        ),
        FilterPreset.ShaderEffect(
            id = "film_grain",
            name = "Film Grain",
            thumbnailColors = listOf(Color(0xFF555555), Color(0xFF888888)),
            grain = 0.15f
        ),
        FilterPreset.ShaderEffect(
            id = "dreamy_glow",
            name = "Bloom",
            thumbnailColors = listOf(Color(0xFFFDE047), Color(0xFFFEF08A)),
            bloom = 0.5f
        ),
        FilterPreset.ShaderEffect(
            id = "tilt_shift",
            name = "Tilt-Shift",
            thumbnailColors = listOf(Color(0xFF10B981), Color(0xFFD1FAE5)),
            tiltShift = 0.8f
        ),
        FilterPreset.ShaderEffect(
            id = "chromatic_pop",
            name = "Chromatic Pop",
            thumbnailColors = listOf(Color(0xFFEF4444), Color(0xFF3B82F6)),
            chromaticPop = 0.02f
        ),
        FilterPreset.ShaderEffect(
            id = "duotone_sunset",
            name = "Duotone Sunset",
            thumbnailColors = listOf(Color(0xFFF97316), Color(0xFF6B21A8)),
            duotoneColors = Pair(Color(0xFF6B21A8), Color(0xFFF97316))
        ),
        FilterPreset.ShaderEffect(
            id = "duotone_noir",
            name = "Duotone Noir",
            thumbnailColors = listOf(Color(0xFF000000), Color(0xFF06B6D4)),
            duotoneColors = Pair(Color(0xFF000000), Color(0xFF06B6D4))
        ),

        // TIER 3: Film (3D LUTs)
        FilterPreset.LutEffect(
            id = "lut_kodak_gold",
            name = "K-Gold 90s",
            thumbnailColors = listOf(Color(0xFFFBBF24), Color(0xFFD97706)),
            lutAssetName = "luts/lut_kodak_gold.png"
        ),
        FilterPreset.LutEffect(
            id = "lut_portra_400",
            name = "P-400 Portrait",
            thumbnailColors = listOf(Color(0xFFFDE047), Color(0xFFFCD34D)),
            lutAssetName = "luts/lut_portra_400.png"
        ),
        FilterPreset.LutEffect(
            id = "lut_fuji_velvia",
            name = "F-Velvia Vivid",
            thumbnailColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
            lutAssetName = "luts/lut_fuji_velvia.png"
        ),
        FilterPreset.LutEffect(
            id = "lut_cinematic_teal_orange",
            name = "Teal & Orange",
            thumbnailColors = listOf(Color(0xFF0EA5E9), Color(0xFFF97316)),
            lutAssetName = "luts/lut_cinematic_teal_orange.png"
        ),
        FilterPreset.LutEffect(
            id = "lut_polaroid",
            name = "Polaroid Classic",
            thumbnailColors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)),
            lutAssetName = "luts/lut_polaroid.png"
        ),
        FilterPreset.LutEffect(
            id = "lut_noir_classic",
            name = "Noir Film",
            thumbnailColors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
            lutAssetName = "luts/lut_noir_classic.png"
        ),
        FilterPreset.LutEffect(
            id = "lut_pastel_dream",
            name = "Pastel Dream",
            thumbnailColors = listOf(Color(0xFFFBCFE8), Color(0xFFBFDBFE)),
            lutAssetName = "luts/lut_pastel_dream.png"
        ),
        FilterPreset.LutEffect(
            id = "lut_moody_editorial",
            name = "Moody Ed.",
            thumbnailColors = listOf(Color(0xFF334155), Color(0xFF1E293B)),
            lutAssetName = "luts/lut_moody_editorial.png"
        )
    )
}
