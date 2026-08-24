package com.snapstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun CurvesPanel(
    luminance: Float,
    redCurve: Float,
    greenCurve: Float,
    blueCurve: Float,
    onLuminanceChanged: (Float) -> Unit,
    onRedCurveChanged: (Float) -> Unit,
    onGreenCurveChanged: (Float) -> Unit,
    onBlueCurveChanged: (Float) -> Unit,
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
            label = "Luminance", value = luminance, min = -1f, max = 1f,
            onValueChange = onLuminanceChanged, onReset = { onLuminanceChanged(0f) }, bipolar = true
        )
        BespokeSlider(
            label = "Red Channel", value = redCurve, min = -1f, max = 1f,
            onValueChange = onRedCurveChanged, onReset = { onRedCurveChanged(0f) }, bipolar = true
        )
        BespokeSlider(
            label = "Green Channel", value = greenCurve, min = -1f, max = 1f,
            onValueChange = onGreenCurveChanged, onReset = { onGreenCurveChanged(0f) }, bipolar = true
        )
        BespokeSlider(
            label = "Blue Channel", value = blueCurve, min = -1f, max = 1f,
            onValueChange = onBlueCurveChanged, onReset = { onBlueCurveChanged(0f) }, bipolar = true
        )
    }
}
