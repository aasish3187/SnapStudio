package com.snapstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun DetailsPanel(
    structure: Float,
    sharpening: Float,
    onStructureChanged: (Float) -> Unit,
    onSharpeningChanged: (Float) -> Unit,
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
            label = "Structure", value = structure, min = 0f, max = 1f,
            onValueChange = onStructureChanged, onReset = { onStructureChanged(0f) }
        )
        BespokeSlider(
            label = "Sharpening", value = sharpening, min = 0f, max = 1f,
            onValueChange = onSharpeningChanged, onReset = { onSharpeningChanged(0f) }
        )
    }
}
