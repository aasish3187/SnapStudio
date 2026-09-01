package com.snapstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ColourPanel(
    vibrance: Float,
    saturation: Float,
    temperature: Float,
    tint: Float,
    redBalance: Float,
    greenBalance: Float,
    blueBalance: Float,
    onVibranceChanged: (Float) -> Unit,
    onSaturationChanged: (Float) -> Unit,
    onTemperatureChanged: (Float) -> Unit,
    onTintChanged: (Float) -> Unit,
    onRedBalanceChanged: (Float) -> Unit,
    onGreenBalanceChanged: (Float) -> Unit,
    onBlueBalanceChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BespokeSlider(
            label = "Vibrance",
            value = vibrance,
            min = -1f,
            max = 1f,
            onValueChange = onVibranceChanged,
            onReset = { onVibranceChanged(0f) },
            bipolar = true
        )
        BespokeSlider(
            label = "Saturation",
            value = saturation,
            min = 0f,
            max = 2f,
            onValueChange = onSaturationChanged,
            onReset = { onSaturationChanged(1f) },
            bipolar = true
        )
        BespokeSlider(
            label = "Temperature",
            value = temperature,
            min = -1f,
            max = 1f,
            onValueChange = onTemperatureChanged,
            onReset = { onTemperatureChanged(0f) },
            bipolar = true
        )
        BespokeSlider(
            label = "Tint",
            value = tint,
            min = -1f,
            max = 1f,
            onValueChange = onTintChanged,
            onReset = { onTintChanged(0f) },
            bipolar = true
        )
        BespokeSlider(
            label = "Red Channel",
            value = redBalance,
            min = -1f,
            max = 1f,
            onValueChange = onRedBalanceChanged,
            onReset = { onRedBalanceChanged(0f) },
            bipolar = true
        )
        BespokeSlider(
            label = "Green Channel",
            value = greenBalance,
            min = -1f,
            max = 1f,
            onValueChange = onGreenBalanceChanged,
            onReset = { onGreenBalanceChanged(0f) },
            bipolar = true
        )
        BespokeSlider(
            label = "Blue Channel",
            value = blueBalance,
            min = -1f,
            max = 1f,
            onValueChange = onBlueBalanceChanged,
            onReset = { onBlueBalanceChanged(0f) },
            bipolar = true
        )
    }
}
