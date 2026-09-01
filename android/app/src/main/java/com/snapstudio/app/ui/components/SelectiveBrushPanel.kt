package com.snapstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.snapstudio.app.ui.theme.*

enum class SelectiveMode(val label: String, val icon: ImageVector) {
    EXPOSURE("Exposure", Icons.Outlined.Brightness6),
    TEMPERATURE("Warmth", Icons.Outlined.WbSunny),
    SATURATION("Saturation", Icons.Outlined.Palette),
    CONTRAST("Contrast", Icons.Outlined.Contrast)
}

@Composable
fun SelectiveBrushPanel(
    activeMode: SelectiveMode,
    onModeChanged: (SelectiveMode) -> Unit,
    exposureEV: Float,
    onExposureChanged: (Float) -> Unit,
    temperature: Float,
    onTemperatureChanged: (Float) -> Unit,
    saturation: Float,
    onSaturationChanged: (Float) -> Unit,
    contrast: Float,
    onContrastChanged: (Float) -> Unit,
    brushSize: Float,
    onBrushSizeChanged: (Float) -> Unit,
    brushHardness: Float,
    onBrushHardnessChanged: (Float) -> Unit,
    isEraseMode: Boolean,
    onToggleErase: (Boolean) -> Unit,
    showMaskRubylith: Boolean,
    onToggleMaskRubylith: (Boolean) -> Unit,
    onInvertMask: () -> Unit,
    onClearMask: () -> Unit,
    onAiSelectSubject: () -> Unit = {},
    onAiSelectBackground: () -> Unit = {},
    onAiSelectSky: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showBrushSettings by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // AI Smart Select Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("AI Mask:", color = Amber, fontSize = 11.sp, fontWeight = FontWeight.Bold)

            AssistChip(
                onClick = onAiSelectSubject,
                label = { Text("👤 Subject", fontSize = 11.sp, color = Color.White) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Ink800),
                border = AssistChipDefaults.assistChipBorder(borderColor = Ink700)
            )

            AssistChip(
                onClick = onAiSelectBackground,
                label = { Text("🌄 Background", fontSize = 11.sp, color = Color.White) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Ink800),
                border = AssistChipDefaults.assistChipBorder(borderColor = Ink700)
            )

            AssistChip(
                onClick = onAiSelectSky,
                label = { Text("☁️ Sky", fontSize = 11.sp, color = Color.White) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Ink800),
                border = AssistChipDefaults.assistChipBorder(borderColor = Ink700)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Mode Selector Tabs (Exposure, Warmth, Saturation, Contrast)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(SelectiveMode.values()) { mode ->
                val isSelected = mode == activeMode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Amber else Ink750)
                        .clickable { onModeChanged(mode) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = mode.icon,
                            contentDescription = mode.label,
                            tint = if (isSelected) Ink900 else Fg,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = mode.label,
                            color = if (isSelected) Ink900 else Fg,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Parameter Adjustment Slider for Selected Mode
        when (activeMode) {
            SelectiveMode.EXPOSURE -> {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Dodge/Burn", color = FgFaint, fontSize = 11.sp, modifier = Modifier.width(72.dp))
                    Slider(
                        value = exposureEV,
                        onValueChange = onExposureChanged,
                        valueRange = -2f..2f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                    )
                    Text(
                        text = if (exposureEV > 0) "+%.1f EV".format(exposureEV) else "%.1f EV".format(exposureEV),
                        color = if (exposureEV != 0f) Amber else Fg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.width(56.dp)
                    )
                }
            }
            SelectiveMode.TEMPERATURE -> {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Warmth", color = FgFaint, fontSize = 11.sp, modifier = Modifier.width(72.dp))
                    Slider(
                        value = temperature,
                        onValueChange = onTemperatureChanged,
                        valueRange = -1f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                    )
                    Text(
                        text = "${(temperature * 100).toInt()}",
                        color = if (temperature != 0f) Amber else Fg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.width(56.dp)
                    )
                }
            }
            SelectiveMode.SATURATION -> {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Saturation", color = FgFaint, fontSize = 11.sp, modifier = Modifier.width(72.dp))
                    Slider(
                        value = saturation,
                        onValueChange = onSaturationChanged,
                        valueRange = 0f..2f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                    )
                    Text(
                        text = "${(saturation * 100).toInt()}%",
                        color = if (saturation != 1f) Amber else Fg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.width(56.dp)
                    )
                }
            }
            SelectiveMode.CONTRAST -> {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Contrast", color = FgFaint, fontSize = 11.sp, modifier = Modifier.width(72.dp))
                    Slider(
                        value = contrast,
                        onValueChange = onContrastChanged,
                        valueRange = 0.5f..1.5f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                    )
                    Text(
                        text = "${(contrast * 100).toInt()}%",
                        color = if (contrast != 1f) Amber else Fg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.width(56.dp)
                    )
                }
            }
        }

        // Expandable Brush Settings (Size, Hardness)
        if (showBrushSettings) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Brush Size", color = FgMuted, fontSize = 11.sp, modifier = Modifier.width(72.dp))
                Slider(
                    value = brushSize,
                    onValueChange = onBrushSizeChanged,
                    valueRange = 10f..200f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Ink700)
                )
                Text("${brushSize.toInt()}px", color = Fg, fontSize = 11.sp, modifier = Modifier.width(56.dp))
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Hardness", color = FgMuted, fontSize = 11.sp, modifier = Modifier.width(72.dp))
                Slider(
                    value = brushHardness,
                    onValueChange = onBrushHardnessChanged,
                    valueRange = 0.1f..1f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Ink700)
                )
                Text("${(brushHardness * 100).toInt()}%", color = Fg, fontSize = 11.sp, modifier = Modifier.width(56.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Tool Action Bar: Paint/Erase, Brush Size Settings Toggle, Mask Eye Toggle, Invert, Clear
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Ink800)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Paint / Erase Toggle
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (!isEraseMode) Amber.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onToggleErase(false) }
                    .padding(6.dp)
            ) {
                Icon(Icons.Outlined.Brush, contentDescription = "Paint", tint = if (!isEraseMode) Amber else FgMuted, modifier = Modifier.size(18.dp))
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isEraseMode) Amber.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onToggleErase(true) }
                    .padding(6.dp)
            ) {
                Icon(Icons.Outlined.AutoFixNormal, contentDescription = "Erase", tint = if (isEraseMode) Amber else FgMuted, modifier = Modifier.size(18.dp))
            }

            // Brush Settings Expand/Collapse
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (showBrushSettings) Ink700 else Color.Transparent)
                    .clickable { showBrushSettings = !showBrushSettings }
                    .padding(6.dp)
            ) {
                Icon(Icons.Outlined.Tune, contentDescription = "Brush Settings", tint = if (showBrushSettings) Amber else FgMuted, modifier = Modifier.size(18.dp))
            }

            // Show Mask Rubylith Overlay
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (showMaskRubylith) Color(0xFFFF2D55).copy(alpha = 0.25f) else Color.Transparent)
                    .clickable { onToggleMaskRubylith(!showMaskRubylith) }
                    .padding(6.dp)
            ) {
                Icon(if (showMaskRubylith) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff, contentDescription = "Mask Overlay", tint = if (showMaskRubylith) Color(0xFFFF375F) else FgMuted, modifier = Modifier.size(18.dp))
            }

            // Invert Mask
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onInvertMask() }
                    .padding(6.dp)
            ) {
                Icon(Icons.Outlined.InvertColors, contentDescription = "Invert Mask", tint = FgMuted, modifier = Modifier.size(18.dp))
            }

            // Clear Mask
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onClearMask() }
                    .padding(6.dp)
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Clear Mask", tint = FgMuted, modifier = Modifier.size(18.dp))
            }
        }
    }
}
