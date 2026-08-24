package com.snapstudio.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapstudio.app.R
import com.snapstudio.app.filters.FilterPreset
import com.snapstudio.app.ui.theme.Amber
import com.snapstudio.app.ui.theme.Fg
import com.snapstudio.app.ui.theme.FgFaint
import com.snapstudio.app.ui.theme.Line
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun FilterCarousel(
    filters: List<FilterPreset>,
    activeFilterId: String?,
    thumbnailBitmap: ImageBitmap? = null,
    filterIntensity: Float = 1.0f,
    onIntensityChanged: ((Float) -> Unit)? = null,
    onSelect: (FilterPreset) -> Unit
) {
    var selectedTier by remember { mutableStateOf(FilterPreset.Tier.CLASSIC) }

    val currentFilters = remember(filters, selectedTier) {
        filters.filter { it.tier == selectedTier }
    }

    val coroutineScope = rememberCoroutineScope()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.toFloat()

    // Continuous scroll index offset (0.0 to currentFilters.size - 1)
    val scrollOffset = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // Sync scrollOffset ONLY when NOT dragging (e.g. initial load or tab switch)
    LaunchedEffect(activeFilterId, currentFilters) {
        if (!isDragging) {
            val targetIndex = currentFilters.indexOfFirst { it.id == activeFilterId }.coerceAtLeast(0)
            if (abs(scrollOffset.value - targetIndex) > 0.05f) {
                scrollOffset.animateTo(
                    targetValue = targetIndex.toFloat(),
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
                )
            }
        }
    }

    // Active filter name to display above the carousel
    val nearestIndex = scrollOffset.value.roundToInt().coerceIn(0, (currentFilters.size - 1).coerceAtLeast(0))
    val activeFilter = remember(currentFilters, nearestIndex) {
        if (nearestIndex in currentFilters.indices) currentFilters[nearestIndex] else currentFilters.firstOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Integrated Filter Title & Intensity Controller Header (No Collision)
        val showSlider = activeFilterId != null && activeFilterId != "original" && onIntensityChanged != null

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (showSlider) {
                // Integrated Glassmorphic Pill: [ Sepia  100% ────● ]
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.dp, Line.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = activeFilter?.name ?: "Filter",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(filterIntensity * 100).roundToInt()}%",
                            color = Amber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Slider(
                            value = filterIntensity,
                            onValueChange = { onIntensityChanged?.invoke(it) },
                            valueRange = 0f..1f,
                            modifier = Modifier.width(140.dp).height(20.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Amber,
                                activeTrackColor = Amber,
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            } else {
                // Clean Title Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(1.dp, Line.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Amber)
                    )
                    Text(
                        text = activeFilter?.name ?: "Original",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Precomputed ColorFilters for smooth 120 FPS scrolling
        val colorFilters = remember(currentFilters) {
            currentFilters.mapNotNull { filter ->
                if (filter is FilterPreset.ColorMatrix) {
                    filter.id to ColorFilter.colorMatrix(ColorMatrix(filter.matrix))
                } else null
            }.toMap()
        }

        // Rotary Curve Wheel (Arc Dome Design)
        val wheelHeightDp = 115.dp
        val radiusDp = (screenWidthDp * 0.42f).coerceAtLeast(145f)
        val angleSpacingRad = Math.toRadians(25.0).toFloat()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(wheelHeightDp)
                .pointerInput(currentFilters) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            val target = scrollOffset.value.roundToInt().coerceIn(0, (currentFilters.size - 1).coerceAtLeast(0))
                            coroutineScope.launch {
                                scrollOffset.animateTo(
                                    targetValue = target.toFloat(),
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                                if (target in currentFilters.indices) {
                                    onSelect(currentFilters[target])
                                }
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            val target = scrollOffset.value.roundToInt().coerceIn(0, (currentFilters.size - 1).coerceAtLeast(0))
                            coroutineScope.launch {
                                scrollOffset.animateTo(target.toFloat())
                                if (target in currentFilters.indices) {
                                    onSelect(currentFilters[target])
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            // Direct 1-to-1 tactile finger tracking along the rotary arc
                            val delta = -dragAmount / 62f
                            val newOffset = (scrollOffset.value + delta).coerceIn(0f, (currentFilters.size - 1).coerceAtLeast(0).toFloat())
                            coroutineScope.launch {
                                scrollOffset.snapTo(newOffset)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.TopCenter
        ) {
            val currentOffset = scrollOffset.value
            val centerIndex = currentOffset.roundToInt()

            // Render visible items around the center index (-4 to +4)
            val minVisible = (centerIndex - 4).coerceAtLeast(0)
            val maxVisible = (centerIndex + 4).coerceAtMost(currentFilters.size - 1)

            for (i in minVisible..maxVisible) {
                val filter = currentFilters[i]
                val itemOffset = i - currentOffset // Relative offset from center (e.g. -2.0, 0.0, +1.5)
                val angleRad = itemOffset * angleSpacingRad // Angle in radians

                // Skip items beyond +/- 80 degrees
                if (abs(angleRad) > Math.toRadians(82.0)) continue

                // True Arc / Curve coordinates
                val xOffsetDp = radiusDp * sin(angleRad)
                val yOffsetDp = radiusDp * (1f - cos(angleRad)) * 0.70f

                // Scale: 1.32x at center apex, gracefully tapering down to 0.6x at sides
                val absOffset = abs(itemOffset)
                val scale = (1.32f - absOffset * 0.26f).coerceIn(0.55f, 1.32f)
                val alpha = (1f - absOffset * 0.22f).coerceIn(0.35f, 1f)
                val isCenter = absOffset < 0.45f

                val discSize = 54.dp

                Box(
                    modifier = Modifier
                        .offset(x = xOffsetDp.dp, y = yOffsetDp.dp)
                        .size(discSize)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .shadow(if (isCenter) 12.dp else 2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .then(
                            if (isCenter) Modifier.border(3.dp, Color.White, CircleShape)
                            else Modifier.border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        )
                        .pointerInput(i) {
                            detectTapGestures {
                                coroutineScope.launch {
                                    scrollOffset.animateTo(
                                        targetValue = i.toFloat(),
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
                                    )
                                    onSelect(filter)
                                }
                            }
                        }
                ) {
                    // Distinctive Filter Preview Bubble
                    FilterThumbnailView(
                        filter = filter,
                        colorFilter = colorFilters[filter.id]
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tier Tabs (CLASSIC | CREATIVE | FILM)
        TabRow(
            selectedTabIndex = selectedTier.ordinal,
            containerColor = Color.Transparent,
            contentColor = Fg,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTier.ordinal]),
                    color = Amber,
                    height = 2.5.dp
                )
            },
            divider = {}
        ) {
            FilterPreset.Tier.values().forEach { tier ->
                Tab(
                    selected = selectedTier == tier,
                    onClick = {
                        selectedTier = tier
                        val firstInTier = filters.firstOrNull { it.tier == tier }
                        if (firstInTier != null) {
                            onSelect(firstInTier)
                        }
                    },
                    text = {
                        Text(
                            text = tier.name,
                            fontWeight = if (selectedTier == tier) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTier == tier) Amber else FgFaint,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun FilterThumbnailView(
    filter: FilterPreset,
    colorFilter: ColorFilter?
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (filter) {
            is FilterPreset.ColorMatrix -> {
                Image(
                    painter = painterResource(id = R.drawable.filter_sample_waterfall),
                    contentDescription = filter.name,
                    contentScale = ContentScale.Crop,
                    colorFilter = colorFilter,
                    modifier = Modifier.fillMaxSize()
                )
                // Distinctive chromatic overlay for signature looks
                when (filter.id) {
                    "mono", "noir_classic_matrix" -> {
                        // Monochrome is purely handled by matrix
                    }
                    "vivid" -> {
                        Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFFFF9800).copy(alpha = 0.25f), Color.Transparent))))
                    }
                    "warm", "golden_hour" -> {
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFB300).copy(alpha = 0.35f), Color(0xFFFF7043).copy(alpha = 0.25f)))))
                    }
                    "cool", "moody_blue" -> {
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF00E5FF).copy(alpha = 0.3f), Color(0xFF3D5AFE).copy(alpha = 0.25f)))))
                    }
                    "rose" -> {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE91E63).copy(alpha = 0.28f)))
                    }
                    "sepia", "antique" -> {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF795548).copy(alpha = 0.35f)))
                    }
                    "cross_process" -> {
                        Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFF00B0FF).copy(alpha = 0.3f), Color(0xFFFF6D00).copy(alpha = 0.3f)))))
                    }
                }
            }
            is FilterPreset.ShaderEffect -> {
                Image(
                    painter = painterResource(id = R.drawable.filter_sample_waterfall),
                    contentDescription = filter.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (filter.vignette > 0f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.minDimension / 1.3f
                            )
                        )
                    }
                    if (filter.duotoneColors != null) {
                        val (dark, light) = filter.duotoneColors
                        drawRect(
                            brush = Brush.linearGradient(listOf(dark.copy(alpha = 0.85f), light.copy(alpha = 0.85f))),
                            blendMode = BlendMode.Screen
                        )
                    }
                    if (filter.chromaticPop > 0f) {
                        drawRect(
                            brush = Brush.horizontalGradient(listOf(Color.Red.copy(alpha = 0.3f), Color.Transparent, Color.Cyan.copy(alpha = 0.3f))),
                            blendMode = BlendMode.Screen
                        )
                    }
                    if (filter.bloom > 0f) {
                        drawCircle(
                            brush = Brush.radialGradient(listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)),
                            radius = size.minDimension / 1.5f
                        )
                    }
                }
            }
            is FilterPreset.LutEffect -> {
                Image(
                    painter = painterResource(id = R.drawable.filter_sample_waterfall),
                    contentDescription = filter.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Apply characteristic film tint
                when (filter.id) {
                    "lut_kodak_gold" -> {
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFC107).copy(alpha = 0.35f), Color(0xFFFF5722).copy(alpha = 0.25f)))))
                    }
                    "lut_portra_400" -> {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFE0B2).copy(alpha = 0.3f)))
                    }
                    "lut_fuji_velvia" -> {
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF00E676).copy(alpha = 0.25f), Color(0xFF00B0FF).copy(alpha = 0.25f)))))
                    }
                    "lut_cinematic_teal_orange" -> {
                        Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFF00E5FF).copy(alpha = 0.35f), Color(0xFFFF6D00).copy(alpha = 0.35f)))))
                    }
                    "lut_polaroid" -> {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFF3E0).copy(alpha = 0.3f)))
                    }
                    "lut_noir_classic" -> {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
                    }
                    "lut_pastel_dream" -> {
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF48FB1).copy(alpha = 0.35f), Color(0xFF90CAF9).copy(alpha = 0.35f)))))
                    }
                    "lut_moody_editorial" -> {
                        Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))))
                    }
                    else -> {
                        val tintColor = filter.thumbnailColors.firstOrNull() ?: Color(0xFFD4A373)
                        Box(modifier = Modifier.fillMaxSize().background(tintColor.copy(alpha = 0.3f)))
                    }
                }
            }
        }
    }
}
