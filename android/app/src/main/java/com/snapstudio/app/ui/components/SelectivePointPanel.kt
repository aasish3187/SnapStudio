package com.snapstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapstudio.app.studio.SelectiveControlPoint
import com.snapstudio.app.ui.theme.*

enum class SelectivePointParam(val label: String, val icon: ImageVector) {
    BRIGHTNESS("Brightness", Icons.Outlined.Brightness6),
    CONTRAST("Contrast", Icons.Outlined.Contrast),
    SATURATION("Saturation", Icons.Outlined.Palette),
    WARMTH("Warmth", Icons.Outlined.WbSunny),
    RADIUS("Radius", Icons.Outlined.RadioButtonUnchecked)
}

@Composable
fun SelectivePointPanel(
    controlPoints: List<SelectiveControlPoint>,
    selectedPoint: SelectiveControlPoint?,
    onPointUpdated: (SelectiveControlPoint) -> Unit,
    onAddPointClicked: () -> Unit,
    onDeletePoint: () -> Unit,
    onResetPoint: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeParam by remember { mutableStateOf(SelectivePointParam.BRIGHTNESS) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (selectedPoint == null) {
            // No point selected guidance
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.TouchApp, contentDescription = null, tint = Amber, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Tap anywhere on the photo to drop a Control Point", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Edits brightness, contrast & saturation within that radius", color = FgMuted, fontSize = 11.sp)
            }
        } else {
            // Parameter Selection Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(SelectivePointParam.values()) { param ->
                    val isSelected = param == activeParam
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Amber else Ink800)
                            .clickable { activeParam = param }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = param.icon,
                                contentDescription = param.label,
                                tint = if (isSelected) Ink900 else Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = param.label,
                                color = if (isSelected) Ink900 else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Slider for active parameter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (activeParam) {
                        SelectivePointParam.BRIGHTNESS -> "Bright"
                        SelectivePointParam.CONTRAST -> "Contr"
                        SelectivePointParam.SATURATION -> "Satur"
                        SelectivePointParam.WARMTH -> "Warm"
                        SelectivePointParam.RADIUS -> "Radius"
                    },
                    color = FgFaint,
                    fontSize = 11.sp,
                    modifier = Modifier.width(42.dp)
                )

                when (activeParam) {
                    SelectivePointParam.BRIGHTNESS -> {
                        Slider(
                            value = selectedPoint.brightness,
                            onValueChange = { onPointUpdated(selectedPoint.copy(brightness = it)) },
                            valueRange = -1f..1f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                        )
                        Text("${(selectedPoint.brightness * 100).toInt()}%", color = Fg, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
                    }
                    SelectivePointParam.CONTRAST -> {
                        Slider(
                            value = selectedPoint.contrast,
                            onValueChange = { onPointUpdated(selectedPoint.copy(contrast = it)) },
                            valueRange = 0.2f..2.5f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                        )
                        Text("${((selectedPoint.contrast - 1f) * 100).toInt()}%", color = Fg, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
                    }
                    SelectivePointParam.SATURATION -> {
                        Slider(
                            value = selectedPoint.saturation,
                            onValueChange = { onPointUpdated(selectedPoint.copy(saturation = it)) },
                            valueRange = 0f..2.5f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                        )
                        Text("${((selectedPoint.saturation - 1f) * 100).toInt()}%", color = Fg, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
                    }
                    SelectivePointParam.WARMTH -> {
                        Slider(
                            value = selectedPoint.temperature,
                            onValueChange = { onPointUpdated(selectedPoint.copy(temperature = it)) },
                            valueRange = -1f..1f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                        )
                        Text("${(selectedPoint.temperature * 100).toInt()}%", color = Fg, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
                    }
                    SelectivePointParam.RADIUS -> {
                        Slider(
                            value = selectedPoint.radius,
                            onValueChange = { onPointUpdated(selectedPoint.copy(radius = it)) },
                            valueRange = 40f..600f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                        )
                        Text("${selectedPoint.radius.toInt()}px", color = Fg, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Control Point Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Point #${controlPoints.indexOfFirst { it.id == selectedPoint.id } + 1} of ${controlPoints.size}",
                    color = Amber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onResetPoint) {
                        Icon(Icons.Outlined.RestartAlt, contentDescription = "Reset", tint = FgMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", color = FgMuted, fontSize = 11.sp)
                    }

                    TextButton(onClick = onDeletePoint) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = Color(0xFFFF5252), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
