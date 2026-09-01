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
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                text = "Brush over area • Tap Heal",
                color = FgMuted,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Brush Size Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Size", color = FgFaint, fontSize = 12.sp, modifier = Modifier.width(42.dp))
            Slider(
                value = brushSize,
                onValueChange = onBrushSizeChanged,
                valueRange = 10f..120f,
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
                modifier = Modifier.width(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Controls Row: Paint/Erase, Clear, and Action Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection Paint / Erase Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Ink800)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (!isEraseMode) Amber.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { onToggleErase(false) }
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Brush,
                        contentDescription = "Highlight Target",
                        tint = if (!isEraseMode) Amber else FgMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isEraseMode) Amber.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { onToggleErase(true) }
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.AutoFixNormal,
                        contentDescription = "Erase Selection",
                        tint = if (isEraseMode) Amber else FgMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onClearSelection() }
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = "Clear Selection",
                        tint = FgMuted,
                        modifier = Modifier.size(20.dp)
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
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                if (isHealingInProgress) {
                    CircularProgressIndicator(
                        color = Ink900,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Inpainting...", color = Ink900, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                } else {
                    Icon(
                        Icons.Outlined.Healing,
                        contentDescription = null,
                        tint = Ink900,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (toolName == "healing") "Heal Blemish" else "Erase Object",
                        color = Ink900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
