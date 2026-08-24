package com.snapstudio.app.filters

import androidx.compose.ui.graphics.Color

sealed class FilterPreset {
    abstract val id: String
    abstract val name: String
    abstract val thumbnailColors: List<Color>
    abstract val tier: Tier

    enum class Tier {
        CLASSIC,
        CREATIVE,
        FILM
    }

    data class ColorMatrix(
        override val id: String,
        override val name: String,
        override val thumbnailColors: List<Color>,
        val matrix: FloatArray
    ) : FilterPreset() {
        override val tier = Tier.CLASSIC
    }

    data class ShaderEffect(
        override val id: String,
        override val name: String,
        override val thumbnailColors: List<Color>,
        val vignette: Float = 0f,
        val grain: Float = 0f,
        val chromaticPop: Float = 0f,
        val duotoneColors: Pair<Color, Color>? = null,
        val bloom: Float = 0f,
        val tiltShift: Float = 0f
    ) : FilterPreset() {
        override val tier = Tier.CREATIVE
    }

    data class LutEffect(
        override val id: String,
        override val name: String,
        override val thumbnailColors: List<Color>,
        val lutAssetName: String
    ) : FilterPreset() {
        override val tier = Tier.FILM
    }
}
