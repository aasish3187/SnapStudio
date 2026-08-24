package com.snapstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun TuneImagePanel(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    ambiance: Float,
    highlights: Float,
    shadows: Float,
    warmth: Float,
    onBrightnessChanged: (Float) -> Unit,
    onContrastChanged: (Float) -> Unit,
    onSaturationChanged: (Float) -> Unit,
    onAmbianceChanged: (Float) -> Unit,
    onHighlightsChanged: (Float) -> Unit,
    onShadowsChanged: (Float) -> Unit,
    onWarmthChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        BespokeSlider(
            label = "Brightness", value = brightness, min = -1f, max = 1f,
            onValueChange = onBrightnessChanged, onReset = { onBrightnessChanged(0f) }, bipolar = true
        )
        BespokeSlider(
            label = "Contrast", value = contrast, min = 0f, max = 2f,
            onValueChange = onContrastChanged, onReset = { onContrastChanged(1f) }, bipolar = true
        )
        BespokeSlider(
            label = "Saturation", value = saturation, min = 0f, max = 2f,
            onValueChange = onSaturationChanged, onReset = { onSaturationChanged(1f) }, bipolar = true
        )
        BespokeSlider(
            label = "Ambiance", value = ambiance, min = -1f, max = 1f,
            onValueChange = onAmbianceChanged, onReset = { onAmbianceChanged(0f) }, bipolar = true
        )
        BespokeSlider(
            label = "Highlights", value = highlights, min = -1f, max = 1f,
            onValueChange = onHighlightsChanged, onReset = { onHighlightsChanged(0f) }, bipolar = true
        )
        BespokeSlider(
            label = "Shadows", value = shadows, min = -1f, max = 1f,
            onValueChange = onShadowsChanged, onReset = { onShadowsChanged(0f) }, bipolar = true
        )
        BespokeSlider(
            label = "Warmth", value = warmth, min = -1f, max = 1f,
            onValueChange = onWarmthChanged, onReset = { onWarmthChanged(0f) }, bipolar = true
        )
    }
}
