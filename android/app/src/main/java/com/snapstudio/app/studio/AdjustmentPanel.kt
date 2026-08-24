package com.snapstudio.app.studio

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdjustmentPanel(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    onBrightnessChanged: (Float) -> Unit,
    onContrastChanged: (Float) -> Unit,
    onSaturationChanged: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Text("Brightness: ${"%.2f".format(brightness)}")
        Slider(
            value = brightness,
            onValueChange = onBrightnessChanged,
            valueRange = -1f..1f
        )
        
        Text("Contrast: ${"%.2f".format(contrast)}")
        Slider(
            value = contrast,
            onValueChange = onContrastChanged,
            valueRange = 0f..2f
        )
        
        Text("Saturation: ${"%.2f".format(saturation)}")
        Slider(
            value = saturation,
            onValueChange = onSaturationChanged,
            valueRange = 0f..2f
        )
    }
}
