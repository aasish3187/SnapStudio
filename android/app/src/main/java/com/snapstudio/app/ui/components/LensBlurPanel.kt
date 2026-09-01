package com.snapstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BlurCircular
import androidx.compose.material.icons.outlined.BlurLinear
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapstudio.app.studio.LensBlurEngine
import com.snapstudio.app.ui.theme.*

@Composable
fun LensBlurPanel(
    blurStrength: Float,
    onBlurStrengthChanged: (Float) -> Unit,
    blurShape: LensBlurEngine.BlurShape,
    onBlurShapeChanged: (LensBlurEngine.BlurShape) -> Unit,
    focalSize: Float,
    onFocalSizeChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Blur Shape Switcher (Radial Bokeh vs Linear Tilt-Shift)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lens Blur & Bokeh",
                color = Amber,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Radial
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (blurShape == LensBlurEngine.BlurShape.CIRCULAR) Amber.copy(alpha = 0.25f) else Ink750)
                        .border(1.dp, if (blurShape == LensBlurEngine.BlurShape.CIRCULAR) Amber else Line.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable { onBlurShapeChanged(LensBlurEngine.BlurShape.CIRCULAR) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.BlurCircular, contentDescription = null, tint = if (blurShape == LensBlurEngine.BlurShape.CIRCULAR) Amber else FgMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Radial", color = if (blurShape == LensBlurEngine.BlurShape.CIRCULAR) Amber else FgMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Linear
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (blurShape == LensBlurEngine.BlurShape.LINEAR) Amber.copy(alpha = 0.25f) else Ink750)
                        .border(1.dp, if (blurShape == LensBlurEngine.BlurShape.LINEAR) Amber else Line.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable { onBlurShapeChanged(LensBlurEngine.BlurShape.LINEAR) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.BlurLinear, contentDescription = null, tint = if (blurShape == LensBlurEngine.BlurShape.LINEAR) Amber else FgMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Linear", color = if (blurShape == LensBlurEngine.BlurShape.LINEAR) Amber else FgMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Blur Strength Slider
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Blur", color = FgFaint, fontSize = 12.sp, modifier = Modifier.width(72.dp))
            Slider(
                value = blurStrength,
                onValueChange = onBlurStrengthChanged,
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
            )
            Text("${(blurStrength * 100).toInt()}%", color = Fg, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.width(42.dp))
        }

        // Focal Size Slider
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Focal Size", color = FgFaint, fontSize = 12.sp, modifier = Modifier.width(72.dp))
            Slider(
                value = focalSize,
                onValueChange = onFocalSizeChanged,
                valueRange = 0.1f..0.8f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(thumbColor = Amber, activeTrackColor = Amber, inactiveTrackColor = Ink700)
            )
            Text("${(focalSize * 100).toInt()}%", color = Fg, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.width(42.dp))
        }
    }
}
