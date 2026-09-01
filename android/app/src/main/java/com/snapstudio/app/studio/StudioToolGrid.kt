package com.snapstudio.app.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapstudio.app.ui.theme.*

enum class StudioCategory {
    ALL, REFINE, FIX, STYLE
}

@Immutable
data class StudioTool(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val category: StudioCategory,
    val isNew: Boolean = false
)

val ALL_STUDIO_TOOLS = listOf(
    // REFINE
    StudioTool("tune_image", "Tune image", Icons.Outlined.Tune, StudioCategory.REFINE),
    StudioTool("details", "Details", Icons.Outlined.Details, StudioCategory.REFINE),
    StudioTool("dehaze", "Dehaze", Icons.Outlined.FilterDrama, StudioCategory.REFINE, isNew = true),
    StudioTool("tonal_contrast", "Tonal contrast", Icons.Outlined.Contrast, StudioCategory.REFINE),
    StudioTool("white_balance", "White balance", Icons.Outlined.WbSunny, StudioCategory.REFINE),
    StudioTool("colour", "Colour", Icons.Outlined.Palette, StudioCategory.REFINE),
    StudioTool("curves", "Curves", Icons.Outlined.ShowChart, StudioCategory.REFINE),

    // FIX / EDIT
    StudioTool("crop", "Crop", Icons.Outlined.CropRotate, StudioCategory.FIX),
    StudioTool("brush", "Brush", Icons.Outlined.Brush, StudioCategory.FIX),
    StudioTool("selective", "Selective", Icons.Outlined.Adjust, StudioCategory.FIX),
    StudioTool("lens_blur", "Lens Blur", Icons.Outlined.BlurCircular, StudioCategory.FIX),
    StudioTool("double_exposure", "Double Exposure", Icons.Outlined.Layers, StudioCategory.FIX, isNew = true),

    // EFFECTS / STYLE
    StudioTool("vignette", "Vignette", Icons.Outlined.Vignette, StudioCategory.STYLE),
    StudioTool("grain", "Film Grain", Icons.Outlined.Grain, StudioCategory.STYLE, isNew = true),
    StudioTool("light_leak", "Light Leak", Icons.Outlined.WbIridescent, StudioCategory.STYLE, isNew = true),
    StudioTool("frames", "Frames", Icons.Outlined.CropDin, StudioCategory.STYLE, isNew = true),
    StudioTool("text", "Text", Icons.Outlined.TextFields, StudioCategory.STYLE),
    StudioTool("stickers", "Stickers", Icons.Outlined.Face, StudioCategory.STYLE),

    // PRESETS
    StudioTool("vintage", "Vintage", Icons.Outlined.CameraRoll, StudioCategory.STYLE),
    StudioTool("bw", "B&W", Icons.Outlined.FilterBAndW, StudioCategory.STYLE),
    StudioTool("noir", "Noir", Icons.Outlined.Nightlight, StudioCategory.STYLE),
    StudioTool("drama", "Drama", Icons.Outlined.Theaters, StudioCategory.STYLE),
    StudioTool("retrolux", "Retrolux", Icons.Outlined.Camera, StudioCategory.STYLE),
    StudioTool("hdr_scape", "HDR Scape", Icons.Outlined.Landscape, StudioCategory.STYLE),
    StudioTool("grunge", "Grunge", Icons.Outlined.ArtTrack, StudioCategory.STYLE),

    // AI TOOLS
    StudioTool("object_remove", "Background Remover", Icons.Outlined.AutoFixHigh, StudioCategory.FIX, isNew = true),
    StudioTool("face_restore", "Face Restore", Icons.Outlined.FaceRetouchingNatural, StudioCategory.REFINE, isNew = true)
)

val VIDEO_STUDIO_TOOLS = listOf(
    StudioTool("trim", "Trim", Icons.Outlined.ContentCut, StudioCategory.FIX, isNew = true),
    StudioTool("filters", "Filters", Icons.Outlined.AutoAwesome, StudioCategory.STYLE)
)

@Composable
fun CategoryTabRow(
    activeCategory: StudioCategory,
    onCategorySelected: (StudioCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StudioCategory.values().forEach { category ->
            val isActive = activeCategory == category

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isActive) Amber else Ink800.copy(alpha = 0.6f))
                    .border(
                        width = 1.dp,
                        color = if (isActive) Amber else Line.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.name,
                    color = if (isActive) Ink900 else FgMuted,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun ToolGrid(
    tools: List<StudioTool>,
    onToolSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = tools,
            key = { it.id }
        ) { tool ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToolSelected(tool.id) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Circular Icon Tile
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Ink800)
                            .border(1.dp, Line.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tool.icon,
                            contentDescription = tool.label,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Elegant Badge in Top Right
                    if (tool.isNew) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFE040FB), Color(0xFFFF4081))
                                    )
                                )
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "AI",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = tool.label,
                    color = FgMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                )
            }
        }
    }
}
