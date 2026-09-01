package com.snapstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapstudio.app.ui.theme.*

@Composable
fun HealingPanel(
    toolName: String,
    brushSize: Float,
    onBrushSizeChanged: (Float) -> Unit,
    isEraseMode: Boolean,
    onToggleErase: (Boolean) -> Unit,
    isHealingInProgress: Boolean,
    onApplyHeal: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Tip & Tool Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (toolName == "healing") "Blemish & Spot Healing" else "Smart Object Remover",
                color = Amber,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = if (isEraseMode) "Eraser Active • Rub to unselect" else "Brush over area to remove",
                color = if (isEraseMode) Color(0xFFFF9500) else FgMuted,
                fontSize = 12.sp,
                fontWeight = if (isEraseMode) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Brush Size Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Size", color = FgFaint, fontSize = 12.sp, modifier = Modifier.width(36.dp))
            Slider(
                value = brushSize,
                onValueChange = onBrushSizeChanged,
                valueRange = 10f..140f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Amber,
                    activeTrackColor = Amber,
                    inactiveTrackColor = Ink700
                )
            )
            Text(
                text = "${brushSize.toInt()}px",
                color = Fg,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier.width(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Controls Row: Select / Erase Segmented Control, Clear, and Action Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Paint / Erase Toggle Chips
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Ink800)
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Select Mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isEraseMode) Amber else Color.Transparent)
                        .clickable { onToggleErase(false) }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Paint",
                        color = if (!isEraseMode) Ink900 else FgMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Erase Mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isEraseMode) Amber else Color.Transparent)
                        .clickable { onToggleErase(true) }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Erase",
                        color = if (isEraseMode) Ink900 else FgMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Clear
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onClearSelection() }
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = "Clear Selection",
                        tint = FgMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Primary Heal / Erase Button
            Button(
                onClick = onApplyHeal,
                enabled = !isHealingInProgress,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber,
                    contentColor = Ink900,
                    disabledContainerColor = Ink750,
                    disabledContentColor = FgMuted
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                if (isHealingInProgress) {
                    CircularProgressIndicator(
                        color = Ink900,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Removing...", color = Ink900, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Icon(
                        Icons.Outlined.AutoFixHigh,
                        contentDescription = null,
                        tint = Ink900,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (toolName == "healing") "Heal Area" else "Remove Object",
                        color = Ink900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
