package com.snapstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

enum class PortraitParam(val label: String, val icon: ImageVector) {
    SKIN("Skin Smooth", Icons.Outlined.Face),
    EYES("Eye Clarity", Icons.Outlined.Visibility),
    TEETH("Teeth Whiten", Icons.Outlined.SentimentSatisfiedAlt),
    RELIGHT("3D Relight", Icons.Outlined.WbSunny)
}

@Composable
fun FaceRestorePanel(
    skinSmooth: Float,
    onSkinSmoothChanged: (Float) -> Unit,
    eyeClarity: Float,
    onEyeClarityChanged: (Float) -> Unit,
    teethWhiten: Float,
    onTeethWhitenChanged: (Float) -> Unit,
    relightIntensity: Float,
    onRelightIntensityChanged: (Float) -> Unit,
    relightAngle: Float,
    onRelightAngleChanged: (Float) -> Unit,
    isProcessing: Boolean,
    onApplyEnhance: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeParam by remember { mutableStateOf(PortraitParam.SKIN) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title & Architecture Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.FaceRetouchingNatural, contentDescription = null, tint = Amber, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "468-Point AI Portrait",
                    color = Amber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Text(
                text = "MediaPipe Face Mesh",
                color = FgMuted,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Parameter Selection Tabs
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(PortraitParam.values()) { param ->
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

        Spacer(modifier = Modifier.height(6.dp))

        // Active Parameter Slider Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (activeParam) {
                    PortraitParam.SKIN -> "Smooth"
                    PortraitParam.EYES -> "Clarity"
                    PortraitParam.TEETH -> "Whiten"
                    PortraitParam.RELIGHT -> "Light"
                },
                color = FgFaint,
                fontSize = 11.sp,
                modifier = Modifier.width(46.dp)
            )

            when (activeParam) {
                PortraitParam.SKIN -> {
                    Slider(
                        value = skinSmooth,
                        onValueChange = onSkinSmoothChanged,
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                    )
                    Text("${(skinSmooth * 100).toInt()}%", color = Fg, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                }
                PortraitParam.EYES -> {
                    Slider(
                        value = eyeClarity,
                        onValueChange = onEyeClarityChanged,
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                    )
                    Text("${(eyeClarity * 100).toInt()}%", color = Fg, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                }
                PortraitParam.TEETH -> {
                    Slider(
                        value = teethWhiten,
                        onValueChange = onTeethWhitenChanged,
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                    )
                    Text("${(teethWhiten * 100).toInt()}%", color = Fg, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                }
                PortraitParam.RELIGHT -> {
                    Slider(
                        value = relightIntensity,
                        onValueChange = onRelightIntensityChanged,
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                    )
                    Text("${(relightIntensity * 100).toInt()}%", color = Fg, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                }
            }
        }

        // Secondary Light Angle Slider if 3D Relight is selected
        if (activeParam == PortraitParam.RELIGHT && relightIntensity > 0.05f) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Angle", color = FgFaint, fontSize = 11.sp, modifier = Modifier.width(46.dp))
                Slider(
                    value = relightAngle,
                    onValueChange = onRelightAngleChanged,
                    valueRange = 0f..360f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                )
                Text("${relightAngle.toInt()}°", color = Fg, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Prominent, High-Visibility Apply Button
        Button(
            onClick = onApplyEnhance,
            enabled = !isProcessing,
            colors = ButtonDefaults.buttonColors(
                containerColor = Amber,
                contentColor = Ink900,
                disabledContainerColor = Ink750,
                disabledContentColor = FgMuted
            ),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = Ink900, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Applying 468-Point Mesh...", color = Ink900, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            } else {
                Icon(Icons.Outlined.FaceRetouchingNatural, contentDescription = null, tint = Ink900, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply 468-Point AI Portrait", color = Ink900, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
