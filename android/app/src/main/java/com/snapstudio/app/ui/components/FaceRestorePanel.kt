package com.snapstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapstudio.app.ui.theme.*

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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title & Tip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Face, contentDescription = null, tint = Amber, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI 468-Point Portrait Studio",
                    color = Amber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Text(
                text = "MediaPipe 3D Mesh",
                color = FgMuted,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Sliders List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 140.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Skin Smoothing Slider
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Skin Smooth", color = FgFaint, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                Slider(
                    value = skinSmooth,
                    onValueChange = onSkinSmoothChanged,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                )
                Text("${(skinSmooth * 100).toInt()}%", color = Fg, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, modifier = Modifier.width(36.dp))
            }

            // Eye Clarity Slider
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Eye Clarity", color = FgFaint, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                Slider(
                    value = eyeClarity,
                    onValueChange = onEyeClarityChanged,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                )
                Text("${(eyeClarity * 100).toInt()}%", color = Fg, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, modifier = Modifier.width(36.dp))
            }

            // Teeth Whitening Slider
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Teeth Whiten", color = FgFaint, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                Slider(
                    value = teethWhiten,
                    onValueChange = onTeethWhitenChanged,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                )
                Text("${(teethWhiten * 100).toInt()}%", color = Fg, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, modifier = Modifier.width(36.dp))
            }

            // 3D Studio Relighting Slider
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("3D Relight", color = FgFaint, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                Slider(
                    value = relightIntensity,
                    onValueChange = onRelightIntensityChanged,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                )
                Text("${(relightIntensity * 100).toInt()}%", color = Fg, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, modifier = Modifier.width(36.dp))
            }

            // Relight Angle Slider
            if (relightIntensity > 0.05f) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Light Angle", color = FgFaint, fontSize = 11.sp, modifier = Modifier.width(80.dp))
                    Slider(
                        value = relightAngle,
                        onValueChange = onRelightAngleChanged,
                        valueRange = 0f..360f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
                    )
                    Text("${relightAngle.toInt()}°", color = Fg, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, modifier = Modifier.width(36.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Action Button
        Button(
            onClick = onApplyEnhance,
            enabled = !isProcessing,
            colors = ButtonDefaults.buttonColors(
                containerColor = Amber,
                contentColor = Ink900,
                disabledContainerColor = Ink750,
                disabledContentColor = FgMuted
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = Ink900, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Applying 3D AI Mesh...", color = Ink900, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            } else {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Ink900, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply AI Portrait Retouch", color = Ink900, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
