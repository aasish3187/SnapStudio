package com.snapstudio.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapstudio.app.filters.FILTERS
import com.snapstudio.app.filters.Filter
import com.snapstudio.app.ui.theme.Amber
import com.snapstudio.app.ui.theme.AmberGlow
import com.snapstudio.app.ui.theme.Fg
import com.snapstudio.app.ui.theme.LineStrong
import com.snapstudio.app.ui.theme.Scrim
import kotlinx.coroutines.delay

@Composable
fun FilterTray(
    selectedId: String,
    onSelect: (Filter) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeFilter = FILTERS.find { it.id == selectedId } ?: FILTERS[0]
    
    // Label flash logic
    var showLabel by remember { mutableStateOf(false) }
    var labelKey by remember { mutableStateOf(0) }
    
    LaunchedEffect(selectedId) {
        if (selectedId != "none") {
            showLabel = true
            labelKey++
            delay(1600) // Flash duration
            showLabel = false
        } else {
            showLabel = false
        }
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // Fading label
        Box(modifier = Modifier.height(20.dp), contentAlignment = Alignment.Center) {
            if (showLabel) {
                Text(
                    text = activeFilter.name,
                    color = Fg,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            items(FILTERS) { filter ->
                val isActive = filter.id == selectedId
                val size by animateDpAsState(
                    targetValue = if (isActive) 66.dp else 56.dp,
                    animationSpec = tween(durationMillis = 200),
                    label = "size"
                )

                Box(
                    modifier = Modifier
                        .size(size)
                        .shadow(
                            elevation = if (isActive) 18.dp else 0.dp,
                            shape = RoundedCornerShape(18.dp),
                            spotColor = AmberGlow,
                            ambientColor = AmberGlow
                        )
                        .border(
                            width = if (isActive) 2.5.dp else 1.5.dp,
                            color = if (isActive) Amber else LineStrong,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onSelect(filter) },
                    contentAlignment = Alignment.Center
                ) {
                    // Stub for mini-preview
                    Box(modifier = Modifier.matchParentSize().background(Color.DarkGray))
                    
                    if (filter.id == "none") {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Scrim),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("OFF", color = Fg, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
