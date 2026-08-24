package com.snapstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoTimeline(
    durationMs: Long,
    trimStartMs: Long,
    trimEndMs: Long,
    onTrimChanged: (Long, Long) -> Unit
) {
    var sliderPosition by remember(trimStartMs, trimEndMs) { 
        mutableStateOf(trimStartMs.toFloat()..trimEndMs.toFloat()) 
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(sliderPosition.start.toLong()),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatTime(sliderPosition.endInclusive.toLong()),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        RangeSlider(
            value = sliderPosition,
            onValueChange = { 
                sliderPosition = it 
            },
            onValueChangeFinished = {
                onTrimChanged(sliderPosition.start.toLong(), sliderPosition.endInclusive.toLong())
            },
            valueRange = 0f..durationMs.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFF9F1C),
                activeTrackColor = Color(0xFFFF9F1C),
                inactiveTrackColor = Color(0xFF333333)
            )
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
