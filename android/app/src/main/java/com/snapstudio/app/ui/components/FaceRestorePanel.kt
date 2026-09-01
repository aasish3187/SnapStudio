package com.snapstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    skinGlow: Float,
    onSkinGlowChanged: (Float) -> Unit,
    isProcessing: Boolean,
    onApplyEnhance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
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
                    text = "AI Face Retouch",
                    color = Amber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Text(
                text = "Frequency Separation Skin Tone",
                color = FgMuted,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Skin Smoothing Slider
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Skin Smooth", color = FgFaint, fontSize = 12.sp, modifier = Modifier.width(82.dp))
            Slider(
                value = skinSmooth,
                onValueChange = onSkinSmoothChanged,
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
            )
            Text("${(skinSmooth * 100).toInt()}%", color = Fg, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.width(42.dp))
        }

        // Eye / Facial Clarity Slider
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Eye Clarity", color = FgFaint, fontSize = 12.sp, modifier = Modifier.width(82.dp))
            Slider(
                value = eyeClarity,
                onValueChange = onEyeClarityChanged,
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
            )
            Text("${(eyeClarity * 100).toInt()}%", color = Fg, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.width(42.dp))
        }

        // Skin Glow Slider
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Skin Glow", color = FgFaint, fontSize = 12.sp, modifier = Modifier.width(82.dp))
            Slider(
                value = skinGlow,
                onValueChange = onSkinGlowChanged,
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
            )
            Text("${(skinGlow * 100).toInt()}%", color = Fg, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.width(42.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Enhance / Apply Button
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
                CircularProgressIndicator(
                    color = Ink900,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enhancing Portrait...", color = Ink900, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            } else {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Ink900, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Apply Facial Retouch", color = Ink900, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
