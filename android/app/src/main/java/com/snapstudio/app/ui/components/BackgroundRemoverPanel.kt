package com.snapstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
fun BackgroundRemoverPanel(
    brushSize: Float,
    onBrushSizeChanged: (Float) -> Unit,
    isEraseMode: Boolean,
    onToggleErase: (Boolean) -> Unit,
    isProcessing: Boolean,
    processingText: String = "Processing AI...",
    onRemoveBackground: () -> Unit,
    onRemoveSubject: () -> Unit,
    onRemoveSky: () -> Unit,
    onApplyBrushErase: () -> Unit,
    onClearSelection: () -> Unit,
    onGenerativeReplace: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Direct 1-Tap AI Removal Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("1-Tap AI:", color = Amber, fontSize = 11.sp, fontWeight = FontWeight.Bold)

            // 1-Tap Remove Background
            Button(
                onClick = onRemoveBackground,
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Ink900),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("🌄 Remove Background", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // 1-Tap Remove Subject
            Button(
                onClick = onRemoveSubject,
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = Ink800, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("👤 Remove Subject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // 1-Tap Remove Sky
            Button(
                onClick = onRemoveSky,
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = Ink800, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("☁️ Remove Sky", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Generative Fill
            AssistChip(
                onClick = onGenerativeReplace,
                label = { Text("🪄 Gen Replace", fontSize = 11.sp, color = Amber) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Ink800),
                border = AssistChipDefaults.assistChipBorder(borderColor = Amber.copy(alpha = 0.5f))
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Manual Brush Size Slider for Custom Erasing
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Brush", color = FgFaint, fontSize = 11.sp, modifier = Modifier.width(36.dp))
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
                fontSize = 11.sp,
                modifier = Modifier.width(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Controls Row: Paint / Erase Toggle, Clear, and Apply Brush Erase Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Paint / Erase Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Ink800)
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isEraseMode) Amber else Color.Transparent)
                        .clickable { onToggleErase(false) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Brush",
                        color = if (!isEraseMode) Ink900 else FgMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isEraseMode) Amber else Color.Transparent)
                        .clickable { onToggleErase(true) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Unmask",
                        color = if (isEraseMode) Ink900 else FgMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onClearSelection() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = "Clear",
                        tint = FgMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Primary Erase Brush Button
            Button(
                onClick = onApplyBrushErase,
                enabled = !isProcessing,
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
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = Ink900,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(processingText, color = Ink900, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Icon(
                        Icons.Outlined.AutoFixHigh,
                        contentDescription = null,
                        tint = Ink900,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Erase Brushed Area",
                        color = Ink900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
