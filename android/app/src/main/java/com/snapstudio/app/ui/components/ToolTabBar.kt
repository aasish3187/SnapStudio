package com.snapstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapstudio.app.ui.theme.*

data class ToolDef(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val live: Boolean
)

val STUDIO_TOOLS = listOf(
    ToolDef("filters", "Filters", Icons.Outlined.AutoFixHigh, true),
    ToolDef("adjust", "Adjust", Icons.Outlined.Tune, true),
    ToolDef("crop", "Crop", Icons.Outlined.Crop, true),
    ToolDef("text", "Text", Icons.Outlined.Title, false),
    ToolDef("stickers", "Stickers", Icons.Outlined.EmojiEmotions, false),
    ToolDef("draw", "Draw", Icons.Outlined.Edit, false)
)

@Composable
fun ToolTabBar(
    activeTabId: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(Ink900)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(STUDIO_TOOLS) { tool ->
            val isActive = tool.id == activeTabId
            val color = if (isActive) Amber else FgFaint
            
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp)) // radius-chip
                    .clickable { onTabSelected(tool.id) }
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .defaultMinSize(minWidth = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.label,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = tool.label,
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
