package com.snapstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun TonalContrastPanel(
    highTones: Float,
    midTones: Float,
    lowTones: Float,
    protectShadows: Float,
    protectHighlights: Float,
    onHighTonesChanged: (Float) -> Unit,
    onMidTonesChanged: (Float) -> Unit,
    onLowTonesChanged: (Float) -> Unit,
    onProtectShadowsChanged: (Float) -> Unit,
    onProtectHighlightsChanged: (Float) -> Unit,
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
            label = "High Tones", value = highTones, min = -1f, max = 1f,
            onValueChange = onHighTonesChanged, onReset = { onHighTonesChanged(0f) }, bipolar = true
        )
        BespokeSlider(
            label = "Mid Tones", value = midTones, min = -1f, max = 1f,
            onValueChange = onMidTonesChanged, onReset = { onMidTonesChanged(0f) }, bipolar = true
        )
        BespokeSlider(
            label = "Low Tones", value = lowTones, min = -1f, max = 1f,
            onValueChange = onLowTonesChanged, onReset = { onLowTonesChanged(0f) }, bipolar = true
        )
        BespokeSlider(
            label = "Protect Shadows", value = protectShadows, min = 0f, max = 1f,
            onValueChange = onProtectShadowsChanged, onReset = { onProtectShadowsChanged(0f) }
        )
        BespokeSlider(
            label = "Protect Highlights", value = protectHighlights, min = 0f, max = 1f,
            onValueChange = onProtectHighlightsChanged, onReset = { onProtectHighlightsChanged(0f) }
        )
    }
}
