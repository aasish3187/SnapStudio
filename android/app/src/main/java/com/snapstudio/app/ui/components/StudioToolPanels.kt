package com.snapstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.snapstudio.app.ui.theme.Amber
import com.snapstudio.app.ui.theme.Fg
import com.snapstudio.app.ui.theme.FgFaint
import com.snapstudio.app.ui.theme.FgMuted
import com.snapstudio.app.ui.theme.Ink700
import com.snapstudio.app.ui.theme.Ink900
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate

@Composable
fun WhiteBalancePanel(
    temperature: Float,
    tint: Float,
    onTemperatureChanged: (Float) -> Unit,
    onTintChanged: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Temperature Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Temperature", color = FgFaint, modifier = Modifier.weight(1f))
            Slider(
                value = temperature,
                onValueChange = onTemperatureChanged,
                valueRange = -1f..1f,
                modifier = Modifier.weight(2f),
                colors = SliderDefaults.colors(
                    thumbColor = Amber,
                    activeTrackColor = Amber,
                    inactiveTrackColor = FgFaint.copy(alpha = 0.3f)
                )
            )
            Text(
                text = "${(temperature * 100).toInt()}",
                color = Fg,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.5f).padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Tint Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tint", color = FgFaint, modifier = Modifier.weight(1f))
            Slider(
                value = tint,
                onValueChange = onTintChanged,
                valueRange = -1f..1f,
                modifier = Modifier.weight(2f),
                colors = SliderDefaults.colors(
                    thumbColor = Amber,
                    activeTrackColor = Amber,
                    inactiveTrackColor = FgFaint.copy(alpha = 0.3f)
                )
            )
            Text(
                text = "${(tint * 100).toInt()}",
                color = Fg,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.5f).padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun VignettePanel(
    strength: Float,
    onStrengthChanged: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Strength", color = FgFaint, modifier = Modifier.weight(1f))
            Slider(
                value = strength,
                onValueChange = onStrengthChanged,
                valueRange = 0f..1f,
                modifier = Modifier.weight(2f),
                colors = SliderDefaults.colors(
                    thumbColor = Amber,
                    activeTrackColor = Amber,
                    inactiveTrackColor = FgFaint.copy(alpha = 0.3f)
                )
            )
            Text(
                text = "${(strength * 100).toInt()}",
                color = Fg,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.5f).padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun DehazePanel(
    strength: Float,
    onStrengthChanged: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dehaze", color = FgFaint, modifier = Modifier.weight(1f))
            Slider(
                value = strength,
                onValueChange = onStrengthChanged,
                valueRange = 0f..1f,
                modifier = Modifier.weight(2f),
                colors = SliderDefaults.colors(
                    thumbColor = Amber,
                    activeTrackColor = Amber,
                    inactiveTrackColor = FgFaint.copy(alpha = 0.3f)
                )
            )
            Text(
                text = "${(strength * 100).toInt()}",
                color = Fg,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.5f).padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun GenericToolPanel(
    toolName: String,
    strength: Float,
    onStrengthChanged: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Intensity", color = FgFaint, modifier = Modifier.weight(1f))
            Slider(
                value = strength,
                onValueChange = onStrengthChanged,
                valueRange = -1f..1f,
                modifier = Modifier.weight(2f),
                colors = SliderDefaults.colors(
                    thumbColor = Amber,
                    activeTrackColor = Amber,
                    inactiveTrackColor = FgFaint.copy(alpha = 0.3f)
                )
            )
            Text(
                text = "${(strength * 100).toInt()}",
                color = Fg,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.5f).padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun GrainPanel(
    strength: Float,
    onStrengthChanged: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Grain", color = FgFaint, modifier = Modifier.weight(1f))
            Slider(
                value = strength,
                onValueChange = onStrengthChanged,
                valueRange = 0f..1f,
                modifier = Modifier.weight(2f),
                colors = SliderDefaults.colors(
                    thumbColor = Amber,
                    activeTrackColor = Amber,
                    inactiveTrackColor = FgFaint.copy(alpha = 0.3f)
                )
            )
            Text(
                text = "${(strength * 100).toInt()}",
                color = Fg,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.5f).padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun LightLeakPanel(
    strength: Float,
    onStrengthChanged: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Light Leak", color = FgFaint, modifier = Modifier.weight(1f))
            Slider(
                value = strength,
                onValueChange = onStrengthChanged,
                valueRange = 0f..1f,
                modifier = Modifier.weight(2f),
                colors = SliderDefaults.colors(
                    thumbColor = Amber,
                    activeTrackColor = Amber,
                    inactiveTrackColor = FgFaint.copy(alpha = 0.3f)
                )
            )
            Text(
                text = "${(strength * 100).toInt()}",
                color = Fg,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.5f).padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun FramesPanel(
    selectedFrame: String,
    onFrameSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val options = listOf("none", "polaroid", "cinematic", "white_border")
        val displayNames = listOf("None", "Polaroid", "Cinematic", "White")
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Ink700)
                .padding(4.dp)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = selectedFrame == option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Ink900 else Color.Transparent)
                        .clickable { onFrameSelected(option) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayNames[index],
                        color = if (isSelected) Fg else FgMuted,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun DoubleExposurePanel(
    opacity: Float,
    onOpacityChanged: (Float) -> Unit,
    onPickImage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChromeButton(
                icon = Icons.Outlined.AddPhotoAlternate,
                contentDescription = "Pick Image",
                onClick = onPickImage
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("Select an image to overlay", color = Fg, fontSize = 14.sp)
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Opacity", color = FgFaint, modifier = Modifier.weight(1f))
            Slider(
                value = opacity,
                onValueChange = onOpacityChanged,
                valueRange = 0f..1f,
                modifier = Modifier.weight(2f),
                colors = SliderDefaults.colors(
                    thumbColor = Amber,
                    activeTrackColor = Amber,
                    inactiveTrackColor = FgFaint.copy(alpha = 0.3f)
                )
            )
            Text(
                text = "${(opacity * 100).toInt()}",
                color = Fg,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.5f).padding(start = 8.dp)
            )
        }
    }
}
