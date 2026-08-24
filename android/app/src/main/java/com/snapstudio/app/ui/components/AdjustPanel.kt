package com.snapstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdjustPanel(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    onBrightnessChanged: (Float) -> Unit,
    onContrastChanged: (Float) -> Unit,
    onSaturationChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        BespokeSlider(
            label = "Brightness",
            value = brightness,
            min = -1f,
            max = 1f,
            onValueChange = onBrightnessChanged,
            onReset = { onBrightnessChanged(0f) },
            bipolar = true
        )
        
        BespokeSlider(
            label = "Contrast",
            value = contrast,
            min = 0f,
            max = 2f,
            onValueChange = onContrastChanged,
            onReset = { onContrastChanged(1f) },
            bipolar = true // Values < 1 map to left, > 1 map to right. We center at 1f.
        )
        
        BespokeSlider(
            label = "Saturation",
            value = saturation,
            min = 0f,
            max = 2f,
            onValueChange = onSaturationChanged,
            onReset = { onSaturationChanged(1f) },
            bipolar = true // Values < 1 map to left, > 1 map to right. We center at 1f.
        )
    }
}
