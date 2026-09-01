package com.snapstudio.app.studio

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix as AndroidColorMatrix
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Redo
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapstudio.app.studio.FaceRetouchEngine
import com.snapstudio.app.studio.GenerativeExpandEngine
import com.snapstudio.app.studio.LensBlurEngine
import com.snapstudio.app.ui.components.BackgroundRemoverPanel
import com.snapstudio.app.ui.components.ChromeButton
import com.snapstudio.app.ui.components.FaceRestorePanel
import com.snapstudio.app.ui.components.LensBlurPanel
import com.snapstudio.app.ui.components.SelectiveBrushPanel
import com.snapstudio.app.ui.components.SelectiveMode
import com.snapstudio.app.ui.components.StudioHistogram
import com.snapstudio.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.roundToInt

enum class OverlayType { TEXT, STICKER, IMAGE_STICKER }

data class OverlayItem(
    val id: String = UUID.randomUUID().toString(),
    val type: OverlayType,
    val content: String,
    var x: Float = 0f,
    var y: Float = 0f,
    var rotation: Float = 0f,
    var scale: Float = 1f,
    var color: Color = Color.White,
    var fontFamily: String = "sans",
    var bgStyle: String = "none"
)

class StudioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageUriString = intent.getStringExtra("IMAGE_URI") 
            ?: intent.getStringExtra("image_uri") 
            ?: intent.dataString
        val videoUriString = intent.getStringExtra("video_uri") 
            ?: intent.getStringExtra("VIDEO_URI")
        val isVideo = videoUriString != null
        val uriString = videoUriString ?: imageUriString ?: return

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StudioScreen(
                        mediaUri = Uri.parse(uriString),
                        isVideo = isVideo,
                        onCancel = { finish() },
                        onSaved = { finish() }
                    )
                }
            }
        }
    }
}

data class StudioHistoryItem(
    val overlays: List<OverlayItem> = emptyList(),
    val bitmap: Bitmap? = null,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val vignetteStrength: Float = 0f,
    val dehaze: Float = 0f,
    val genericIntensity: Float = 0f,
    val grainStrength: Float = 0f,
    val lightLeakStrength: Float = 0f,
    val frameStyle: String = "none",
    val ambiance: Float = 0f,
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val warmth: Float = 0f,
    val structure: Float = 0f,
    val sharpening: Float = 0f,
    val highTones: Float = 0f,
    val midTones: Float = 0f,
    val lowTones: Float = 0f,
    val protectShadows: Float = 0f,
    val protectHighlights: Float = 0f,
    val luminance: Float = 0f,
    val redCurve: Float = 0f,
    val greenCurve: Float = 0f,
    val blueCurve: Float = 0f,
    val selectedFilterMatrix: FloatArray? = null,
    val doubleExposureOpacity: Float = 0.5f
)

data class ToolSnapshot(
    val brightness: Float,
    val contrast: Float,
    val saturation: Float,
    val temperature: Float,
    val tint: Float,
    val vignetteStrength: Float,
    val dehaze: Float,
    val genericIntensity: Float,
    val grainStrength: Float,
    val lightLeakStrength: Float,
    val frameStyle: String,
    val ambiance: Float,
    val highlights: Float,
    val shadows: Float,
    val warmth: Float,
    val structure: Float,
    val sharpening: Float,
    val highTones: Float,
    val midTones: Float,
    val lowTones: Float,
    val protectShadows: Float,
    val protectHighlights: Float,
    val luminance: Float,
    val redCurve: Float,
    val greenCurve: Float,
    val blueCurve: Float,
    val selectedFilterMatrix: FloatArray?,
    val overlays: List<OverlayItem>,
    val bitmap: Bitmap?
)

@Composable
fun StudioScreen(mediaUri: Uri, isVideo: Boolean, onCancel: () -> Unit, onSaved: () -> Unit) {
    var brightness by remember { mutableStateOf(0f) }
    var contrast by remember { mutableStateOf(1f) }
    var saturation by remember { mutableStateOf(1f) }
    var temperature by remember { mutableStateOf(0f) }
    var tint by remember { mutableStateOf(0f) }
    var vignetteStrength by remember { mutableStateOf(0f) }
    var dehaze by remember { mutableStateOf(0f) }
    var genericIntensity by remember { mutableStateOf(0f) }
    var grainStrength by remember { mutableStateOf(0f) }
    var lightLeakStrength by remember { mutableStateOf(0f) }
    var frameStyle by remember { mutableStateOf("none") }
    
    var ambiance by remember { mutableStateOf(0f) }
    var highlights by remember { mutableStateOf(0f) }
    var shadows by remember { mutableStateOf(0f) }
    var warmth by remember { mutableStateOf(0f) }
    var structure by remember { mutableStateOf(0f) }
    var sharpening by remember { mutableStateOf(0f) }
    var highTones by remember { mutableStateOf(0f) }
    var midTones by remember { mutableStateOf(0f) }
    var lowTones by remember { mutableStateOf(0f) }
    var protectShadows by remember { mutableStateOf(0f) }
    var protectHighlights by remember { mutableStateOf(0f) }
    var luminance by remember { mutableStateOf(0f) }
    var redCurve by remember { mutableStateOf(0f) }
    var greenCurve by remember { mutableStateOf(0f) }
    var blueCurve by remember { mutableStateOf(0f) }
    var doubleExposureBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var doubleExposureOpacity by remember { mutableStateOf(0.5f) }
    var selectedFilterMatrix by remember { mutableStateOf<FloatArray?>(null) }
    var activeTab by remember { mutableStateOf("") }
    var activeCategory by remember { mutableStateOf(StudioCategory.ALL) }
    
    // Selective Masking & Parametric Brush Engine
    val selectiveMaskEngine = remember { SelectiveMaskEngine() }
    var selectiveExposureEV by remember { mutableStateOf(0f) }
    var selectiveTemperature by remember { mutableStateOf(0f) }
    var selectiveSaturation by remember { mutableStateOf(1f) }
    var selectiveContrast by remember { mutableStateOf(1f) }
    var selectiveBrushSize by remember { mutableStateOf(45f) }
    var selectiveBrushHardness by remember { mutableStateOf(0.25f) }
    var selectiveIsErase by remember { mutableStateOf(false) }
    var selectiveShowRubylith by remember { mutableStateOf(false) }
    var selectiveMode by remember { mutableStateOf(SelectiveMode.EXPOSURE) }
    var maskVersion by remember { mutableStateOf(0) }

    // Snapseed-Style Selective Control Points Engine
    var selectivePoints by remember { mutableStateOf(listOf<SelectiveControlPoint>()) }
    var selectedPointId by remember { mutableStateOf<String?>(null) }

    // Smart Inpainting & Object Healing Engine
    val healingMaskEngine = remember { SelectiveMaskEngine() }
    var healingBrushSize by remember { mutableStateOf(35f) }
    var healingIsErase by remember { mutableStateOf(false) }
    var isHealingInProgress by remember { mutableStateOf(false) }
    var healingMaskVersion by remember { mutableStateOf(0) }

    // Lens Blur & Bokeh State
    var lensBlurStrength by remember { mutableStateOf(0.4f) }
    var lensBlurShape by remember { mutableStateOf(LensBlurEngine.BlurShape.CIRCULAR) }
    var lensBlurFocalSize by remember { mutableStateOf(0.35f) }

    // Face Retouch State (468-Point 3D Mesh)
    var faceSkinSmooth by remember { mutableStateOf(0.4f) }
    var faceEyeClarity by remember { mutableStateOf(0.35f) }
    var faceTeethWhiten by remember { mutableStateOf(0.30f) }
    var faceRelightIntensity by remember { mutableStateOf(0.0f) }
    var faceRelightAngle by remember { mutableStateOf(45f) }
    var isFaceProcessing by remember { mutableStateOf(false) }
    var showGenerativeReplaceDialog by remember { mutableStateOf(false) }
    var generativePrompt by remember { mutableStateOf("") }
    var isGenerativeProcessing by remember { mutableStateOf(false) }

    var isComparingOriginal by remember { mutableStateOf(false) }

    var canvasScale by remember { mutableStateOf(1f) }
    var canvasOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    
    var selectedOverlayId by remember { mutableStateOf<String?>(null) }
    var overlays by remember { mutableStateOf(listOf<OverlayItem>()) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Instant zero-lag GPU vector stroke tracking
    val activeStrokePoints = remember { mutableStateListOf<Offset>() }
    var activeStrokeRadius by remember { mutableStateOf(35f) }
    var activeStrokeIsErase by remember { mutableStateOf(false) }

    LaunchedEffect(bitmap) {
        bitmap?.let { b ->
            selectiveMaskEngine.resizeIfNeeded(b.width, b.height)
            healingMaskEngine.resizeIfNeeded(b.width, b.height)
        }
    }

    val liveDisplayBitmap = remember(bitmap, maskVersion, selectiveExposureEV, selectiveTemperature, selectiveSaturation, selectiveContrast, selectiveShowRubylith, selectivePoints, activeTab) {
        if (activeTab == "selective" && bitmap != null && selectivePoints.isNotEmpty()) {
            SelectiveAdjustmentCompositor.compositeControlPoints(
                baseBitmap = bitmap!!,
                points = selectivePoints
            )
        } else if (activeTab == "brush" && bitmap != null) {
            SelectiveAdjustmentCompositor.composite(
                baseBitmap = bitmap!!,
                maskBitmap = selectiveMaskEngine.maskBitmap,
                exposureEV = selectiveExposureEV,
                temperature = selectiveTemperature,
                saturation = selectiveSaturation,
                contrast = selectiveContrast,
                showMaskRubylith = selectiveShowRubylith
            )
        } else {
            bitmap
        }
    }
    
    // Complete Undo / Redo History
    var history by remember { mutableStateOf(listOf(StudioHistoryItem())) }
    var historyIndex by remember { mutableStateOf(0) }

    fun commitHistory(newOverlays: List<OverlayItem> = overlays, newBitmap: Bitmap? = bitmap) {
        val snapshot = StudioHistoryItem(
            overlays = newOverlays,
            bitmap = newBitmap,
            brightness = brightness,
            contrast = contrast,
            saturation = saturation,
            temperature = temperature,
            tint = tint,
            vignetteStrength = vignetteStrength,
            dehaze = dehaze,
            genericIntensity = genericIntensity,
            grainStrength = grainStrength,
            lightLeakStrength = lightLeakStrength,
            frameStyle = frameStyle,
            ambiance = ambiance,
            highlights = highlights,
            shadows = shadows,
            warmth = warmth,
            structure = structure,
            sharpening = sharpening,
            highTones = highTones,
            midTones = midTones,
            lowTones = lowTones,
            protectShadows = protectShadows,
            protectHighlights = protectHighlights,
            luminance = luminance,
            redCurve = redCurve,
            greenCurve = greenCurve,
            blueCurve = blueCurve,
            selectedFilterMatrix = selectedFilterMatrix,
            doubleExposureOpacity = doubleExposureOpacity
        )
        val truncated = history.take(historyIndex + 1).toMutableList()
        truncated.add(snapshot)
        history = truncated
        historyIndex = truncated.size - 1
    }

    fun restoreFromHistory(item: StudioHistoryItem) {
        overlays = item.overlays
        if (item.bitmap != null) bitmap = item.bitmap
        brightness = item.brightness
        contrast = item.contrast
        saturation = item.saturation
        temperature = item.temperature
        tint = item.tint
        vignetteStrength = item.vignetteStrength
        dehaze = item.dehaze
        genericIntensity = item.genericIntensity
        grainStrength = item.grainStrength
        lightLeakStrength = item.lightLeakStrength
        frameStyle = item.frameStyle
        ambiance = item.ambiance
        highlights = item.highlights
        shadows = item.shadows
        warmth = item.warmth
        structure = item.structure
        sharpening = item.sharpening
        highTones = item.highTones
        midTones = item.midTones
        lowTones = item.lowTones
        protectShadows = item.protectShadows
        protectHighlights = item.protectHighlights
        luminance = item.luminance
        redCurve = item.redCurve
        greenCurve = item.greenCurve
        blueCurve = item.blueCurve
        selectedFilterMatrix = item.selectedFilterMatrix
        doubleExposureOpacity = item.doubleExposureOpacity
        selectedOverlayId = null
    }

    fun applyUndo() {
        if (historyIndex > 0) {
            historyIndex--
            restoreFromHistory(history[historyIndex])
        }
    }

    fun applyRedo() {
        if (historyIndex < history.size - 1) {
            historyIndex++
            restoreFromHistory(history[historyIndex])
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingsManager = remember { com.snapstudio.app.settings.SettingsManager(context) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    var toolSnapshot by remember { mutableStateOf<ToolSnapshot?>(null) }

    fun captureSnapshot(): ToolSnapshot {
        return ToolSnapshot(
            brightness, contrast, saturation, temperature, tint, vignetteStrength, dehaze, genericIntensity, 
            grainStrength, lightLeakStrength, frameStyle, ambiance, highlights, shadows, warmth, 
            structure, sharpening, highTones, midTones, lowTones, protectShadows, protectHighlights, 
            luminance, redCurve, greenCurve, blueCurve, selectedFilterMatrix, overlays, bitmap
        )
    }

    fun openTool(toolId: String) {
        toolSnapshot = captureSnapshot()
        val filterMap = mapOf(
            "vintage" to "vintage", "bw" to "mono", "noir" to "noir", "drama" to "gotham",
            "hdr_scape" to "vivid", "retrolux" to "rust", "grunge" to "dracula"
        )
        if (filterMap.containsKey(toolId)) {
            val filterId = filterMap[toolId]
            selectedFilterMatrix = com.snapstudio.app.filters.FILTERS.firstOrNull { it.id == filterId }?.toFloatArray()
            genericIntensity = 1f
        }
        activeTab = toolId
    }

    fun cancelTool() {
        toolSnapshot?.let { s ->
            brightness = s.brightness; contrast = s.contrast; saturation = s.saturation; temperature = s.temperature
            tint = s.tint; vignetteStrength = s.vignetteStrength; dehaze = s.dehaze; genericIntensity = s.genericIntensity
            grainStrength = s.grainStrength; lightLeakStrength = s.lightLeakStrength; frameStyle = s.frameStyle
            ambiance = s.ambiance; highlights = s.highlights; shadows = s.shadows; warmth = s.warmth
            structure = s.structure; sharpening = s.sharpening; highTones = s.highTones; midTones = s.midTones
            lowTones = s.lowTones; protectShadows = s.protectShadows; protectHighlights = s.protectHighlights
            luminance = s.luminance; redCurve = s.redCurve; greenCurve = s.greenCurve; blueCurve = s.blueCurve
            selectedFilterMatrix = s.selectedFilterMatrix
            overlays = s.overlays
            bitmap = s.bitmap
        }
        if (activeTab == "selective") {
            selectivePoints = emptyList()
            selectedPointId = null
        }
        if (activeTab == "brush") {
            selectiveMaskEngine.clear()
            selectiveExposureEV = 0f
            selectiveTemperature = 0f
            selectiveSaturation = 1f
            selectiveContrast = 1f
            selectiveShowRubylith = false
            maskVersion++
        }
        if (activeTab == "object_remove") {
            healingMaskEngine.clear()
            healingMaskVersion++
        }
        toolSnapshot = null
        activeTab = ""
    }

    fun applyTool() {
        if (activeTab == "selective" && bitmap != null) {
            val composited = if (selectivePoints.isNotEmpty()) {
                SelectiveAdjustmentCompositor.compositeControlPoints(
                    baseBitmap = bitmap!!,
                    points = selectivePoints
                )
            } else {
                bitmap!!
            }
            bitmap = composited
            selectivePoints = emptyList()
            selectedPointId = null
            commitHistory(overlays, composited)
        } else if (activeTab == "brush" && bitmap != null) {
            val composited = SelectiveAdjustmentCompositor.composite(
                baseBitmap = bitmap!!,
                maskBitmap = selectiveMaskEngine.maskBitmap,
                exposureEV = selectiveExposureEV,
                temperature = selectiveTemperature,
                saturation = selectiveSaturation,
                contrast = selectiveContrast,
                showMaskRubylith = false
            )
            bitmap = composited
            selectiveMaskEngine.clear()
            selectiveExposureEV = 0f
            selectiveTemperature = 0f
            selectiveSaturation = 1f
            selectiveContrast = 1f
            selectiveShowRubylith = false
            maskVersion++
            commitHistory(overlays, composited)
        } else if (activeTab == "lens_blur" && bitmap != null && lensBlurStrength > 0f) {
            val cur = bitmap!!
            coroutineScope.launch {
                val blurred = LensBlurEngine.applyLensBlur(
                    source = cur,
                    shape = lensBlurShape,
                    blurRadius = (lensBlurStrength * 25).toInt().coerceIn(2, 35),
                    focalSize = lensBlurFocalSize
                )
                bitmap = blurred
                commitHistory(overlays, blurred)
                Toast.makeText(context, "Lens Blur Applied!", Toast.LENGTH_SHORT).show()
            }
        } else {
            commitHistory(overlays, bitmap)
        }
        toolSnapshot = null
        activeTab = ""
    }

    var videoTrimStart by remember { mutableStateOf(0L) }
    var videoTrimEnd by remember { mutableStateOf(-1L) }
    var isExporting by remember { mutableStateOf(false) }
    
    val exoPlayer = remember {
        if (isVideo) {
            androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
                val mediaItem = androidx.media3.common.MediaItem.fromUri(mediaUri)
                setMediaItem(mediaItem)
                repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
                prepare()
                playWhenReady = true
            }
        } else null
    }

    val doubleExposurePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    val decoded = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(context.contentResolver, uri))
                    } else {
                        @Suppress("DEPRECATION")
                        android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                    doubleExposureBitmap = decoded.asImageBitmap()
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    )

    val customStickerPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val newItem = OverlayItem(
                    id = UUID.randomUUID().toString(),
                    type = OverlayType.IMAGE_STICKER,
                    content = uri.toString(),
                    x = 0f,
                    y = 0f
                )
                overlays = overlays + newItem
                selectedOverlayId = newItem.id
                commitHistory(overlays, bitmap)
            }
        }
    )

    val combinedColorMatrix = remember(brightness, contrast, saturation, temperature, tint, dehaze, genericIntensity, selectedFilterMatrix, luminance, redCurve, greenCurve, blueCurve) {
        val androidMatrix = AndroidColorMatrix().apply {
            setSaturation(saturation)
            if (genericIntensity != 0f && selectedFilterMatrix == null) {
                val c = 1f + genericIntensity * 0.2f
                postConcat(AndroidColorMatrix(floatArrayOf(c,0f,0f,0f,0f, 0f,c,0f,0f,0f, 0f,0f,c,0f,0f, 0f,0f,0f,1f,0f)))
            }
            if (dehaze > 0f) {
                val c = 1f + dehaze * 0.5f; val b = -dehaze * 20f
                postConcat(AndroidColorMatrix(floatArrayOf(c,0f,0f,0f,b, 0f,c,0f,0f,b, 0f,0f,c,0f,b, 0f,0f,0f,1f,0f)))
            }
            if (temperature != 0f || tint != 0f) postConcat(AndroidColorMatrix().apply { setScale(1f + temperature * 0.3f, 1f + tint * 0.3f, 1f - temperature * 0.3f, 1f) })
            // Monotone Spline Curve Matrix Transforms
            if (luminance != 0f || redCurve != 0f || greenCurve != 0f || blueCurve != 0f) {
                val rScale = (1f + redCurve * 0.35f + luminance * 0.25f).coerceAtLeast(0.05f)
                val gScale = (1f + greenCurve * 0.35f + luminance * 0.25f).coerceAtLeast(0.05f)
                val bScale = (1f + blueCurve * 0.35f + luminance * 0.25f).coerceAtLeast(0.05f)
                postConcat(AndroidColorMatrix().apply { setScale(rScale, gScale, bScale, 1f) })
            }
            postConcat(AndroidColorMatrix().apply { set(floatArrayOf(contrast,0f,0f,0f,0f, 0f,contrast,0f,0f,0f, 0f,0f,contrast,0f,0f, 0f,0f,0f,1f,0f)) })
            postConcat(AndroidColorMatrix().apply { set(floatArrayOf(1f,0f,0f,0f,brightness * 255f, 0f,1f,0f,0f,brightness * 255f, 0f,0f,1f,0f,brightness * 255f, 0f,0f,0f,1f,0f)) })
            if (selectedFilterMatrix != null) postConcat(AndroidColorMatrix(selectedFilterMatrix))
        }
        androidMatrix.array
    }

    LaunchedEffect(combinedColorMatrix) {
        if (isVideo && exoPlayer != null) {
            val effects = mutableListOf<androidx.media3.common.Effect>()
            effects.add(com.snapstudio.app.filters.VideoFilterEffect(combinedColorMatrix))
            exoPlayer.setVideoEffects(effects)
        }
    }
    
    DisposableEffect(exoPlayer) { onDispose { exoPlayer?.release() } }
    
    LaunchedEffect(mediaUri) {
        if (!isVideo) {
            withContext(Dispatchers.IO) {
                try {
                    val stream = context.contentResolver.openInputStream(mediaUri)
                    val rawBitmap = BitmapFactory.decodeStream(stream)
                    stream?.close()
                    bitmap = rawBitmap
                    val initialHistory = StudioHistoryItem(
                        overlays = emptyList(),
                        bitmap = rawBitmap,
                        brightness = 0f,
                        contrast = 1f,
                        saturation = 1f
                    )
                    history = listOf(initialHistory)
                    historyIndex = 0
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    val composeColorMatrix = remember(combinedColorMatrix) { ColorMatrix(combinedColorMatrix) }
    val thumbnailImageBitmap = remember(bitmap) { bitmap?.let { b -> val s = 200f / maxOf(b.width, b.height); if (s < 1f) Bitmap.createScaledBitmap(b, (b.width * s).toInt(), (b.height * s).toInt(), true).asImageBitmap() else b.asImageBitmap() } }

    Column(modifier = Modifier.fillMaxSize().background(Ink900)) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cancel", color = FgMuted, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onCancel() }.padding(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChromeButton(
                    icon = Icons.Outlined.Undo,
                    contentDescription = "Undo",
                    onClick = { applyUndo() },
                    enabled = historyIndex > 0
                )
                ChromeButton(
                    icon = Icons.Outlined.Redo,
                    contentDescription = "Redo",
                    onClick = { applyRedo() },
                    enabled = historyIndex < history.size - 1
                )
                // RAW Hold-to-Compare Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isComparingOriginal) Amber else Ink750)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isComparingOriginal = true
                                    tryAwaitRelease()
                                    isComparingOriginal = false
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RAW",
                        color = if (isComparingOriginal) Ink900 else Fg,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                // 1-Tap AI Auto-Enhance Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Amber.copy(alpha = 0.2f))
                        .clickable {
                            val b = bitmap ?: return@clickable
                            coroutineScope.launch {
                                val res = AiAutoEnhanceEngine.analyze(b)
                                brightness = res.brightness
                                contrast = res.contrast
                                saturation = res.saturation
                                ambiance = res.ambiance
                                highlights = res.highlights
                                shadows = res.shadows
                                warmth = res.warmth
                                Toast.makeText(context, "AI Auto-Enhanced!", Toast.LENGTH_SHORT).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = "AI Auto Enhance",
                        tint = Amber,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Ink750).clickable {
                        if (isVideo) {
                            isExporting = true
                            coroutineScope.launch {
                                com.snapstudio.app.studio.VideoExportHelper.exportVideo(
                                    context = context,
                                    inputUri = mediaUri,
                                    trimStartMs = videoTrimStart,
                                    trimEndMs = if (videoTrimEnd == -1L) exoPlayer?.duration ?: -1L else videoTrimEnd,
                                    targetHeight = settingsManager.videoExportQuality,
                                    colorMatrix = combinedColorMatrix,
                                    overlays = overlays,
                                    onSuccess = { uri ->
                                        isExporting = false
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "video/mp4"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share via"))
                                    },
                                    onError = { isExporting = false; Toast.makeText(context, "Share failed", Toast.LENGTH_SHORT).show() }
                                )
                            }
                        } else {
                            coroutineScope.launch {
                                val currentBitmap = bitmap ?: return@launch
                                val savedUri = ExportHelper.flattenAndExport(
                                    context = context,
                                    sourceBitmap = currentBitmap,
                                    colorMatrixArray = combinedColorMatrix,
                                    overlays = overlays,
                                    vignetteStrength = vignetteStrength,
                                    grainStrength = grainStrength,
                                    lightLeakStrength = lightLeakStrength,
                                    frameStyle = frameStyle,
                                    doubleExposureBitmap = doubleExposureBitmap?.asAndroidBitmap(),
                                    doubleExposureOpacity = doubleExposureOpacity
                                )
                                if (savedUri != null) {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/jpeg"
                                        putExtra(Intent.EXTRA_STREAM, savedUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share via"))
                                }
                            }
                        }
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Fg, modifier = Modifier.size(18.dp))
                }
                Button(
                    onClick = {
                        if (settingsManager.hapticFeedback) haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        if (isVideo) {
                            isExporting = true
                            coroutineScope.launch {
                                com.snapstudio.app.studio.VideoExportHelper.exportVideo(
                                    context, mediaUri, videoTrimStart, 
                                    if (videoTrimEnd == -1L) exoPlayer?.duration ?: -1L else videoTrimEnd, 
                                    settingsManager.videoExportQuality, combinedColorMatrix, overlays, 
                                    onSuccess = { isExporting = false; Toast.makeText(context, "Video Saved to Gallery!", Toast.LENGTH_SHORT).show(); onSaved() }, 
                                    onError = { isExporting = false; Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show() }
                                )
                            }
                        } else {
                            coroutineScope.launch {
                                val currentBitmap = bitmap ?: return@launch
                                val savedUri = ExportHelper.flattenAndExport(
                                    context = context,
                                    sourceBitmap = currentBitmap,
                                    colorMatrixArray = combinedColorMatrix,
                                    overlays = overlays,
                                    vignetteStrength = vignetteStrength,
                                    grainStrength = grainStrength,
                                    lightLeakStrength = lightLeakStrength,
                                    frameStyle = frameStyle,
                                    doubleExposureBitmap = doubleExposureBitmap?.asAndroidBitmap(),
                                    doubleExposureOpacity = doubleExposureOpacity
                                )
                                if (savedUri != null) { Toast.makeText(context, "Saved to Photos!", Toast.LENGTH_SHORT).show(); onSaved() } else Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Ink900),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Save Copy", color = Ink900, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Top Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (activeTab == "") 0.95f else 1.5f)
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { selectedOverlayId = null })
                },
            contentAlignment = Alignment.Center
        ) {
            if (isVideo && exoPlayer != null) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx -> androidx.media3.ui.PlayerView(ctx).apply { player = exoPlayer; useController = false } },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (bitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = canvasScale,
                            scaleY = canvasScale,
                            translationX = canvasOffset.x,
                            translationY = canvasOffset.y
                        )
                        .pointerInput(activeTab, selectivePoints, selectedPointId) {
                            if (activeTab == "selective") {
                                detectTapGestures(
                                    onTap = { offset ->
                                        val b = bitmap ?: return@detectTapGestures
                                        val fitScale = minOf(size.width / b.width.toFloat(), size.height / b.height.toFloat())
                                        val fitW = b.width * fitScale
                                        val fitH = b.height * fitScale
                                        val padX = (size.width - fitW) / 2f
                                        val padY = (size.height - fitH) / 2f
                                        val imgX = (offset.x - padX) / fitScale
                                        val imgY = (offset.y - padY) / fitScale

                                        if (imgX in 0f..b.width.toFloat() && imgY in 0f..b.height.toFloat()) {
                                            val normX = (imgX / b.width).coerceIn(0f, 1f)
                                            val normY = (imgY / b.height).coerceIn(0f, 1f)

                                            val hitPoint = selectivePoints.firstOrNull { pt ->
                                                val ptScreenX = padX + pt.x * fitW
                                                val ptScreenY = padY + pt.y * fitH
                                                kotlin.math.hypot(offset.x - ptScreenX, offset.y - ptScreenY) <= 45f
                                            }

                                            if (hitPoint != null) {
                                                selectedPointId = hitPoint.id
                                            } else {
                                                val newPt = SelectiveControlPoint(
                                                    x = normX,
                                                    y = normY,
                                                    radius = (b.width * 0.22f).coerceIn(80f, 400f),
                                                    brightness = 0.25f
                                                )
                                                selectivePoints = selectivePoints + newPt
                                                selectedPointId = newPt.id
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        .pointerInput(activeTab, selectedPointId, selectiveBrushSize, selectiveBrushHardness, selectiveIsErase, healingBrushSize, healingIsErase) {
                            if (activeTab == "selective") {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        val b = bitmap ?: return@detectDragGestures
                                        val fitScale = minOf(size.width / b.width.toFloat(), size.height / b.height.toFloat())
                                        val fitW = b.width * fitScale
                                        val fitH = b.height * fitScale
                                        val padX = (size.width - fitW) / 2f
                                        val padY = (size.height - fitH) / 2f

                                        val hitPoint = selectivePoints.firstOrNull { pt ->
                                            val ptScreenX = padX + pt.x * fitW
                                            val ptScreenY = padY + pt.y * fitH
                                            kotlin.math.hypot(offset.x - ptScreenX, offset.y - ptScreenY) <= 55f
                                        }
                                        if (hitPoint != null) {
                                            selectedPointId = hitPoint.id
                                        }
                                    },
                                    onDrag = { change, _ ->
                                        val b = bitmap ?: return@detectDragGestures
                                        val curId = selectedPointId ?: return@detectDragGestures
                                        val fitScale = minOf(size.width / b.width.toFloat(), size.height / b.height.toFloat())
                                        val fitW = b.width * fitScale
                                        val fitH = b.height * fitScale
                                        val padX = (size.width - fitW) / 2f
                                        val padY = (size.height - fitH) / 2f
                                        val imgX = (change.position.x - padX) / fitScale
                                        val imgY = (change.position.y - padY) / fitScale

                                        val normX = (imgX / b.width).coerceIn(0f, 1f)
                                        val normY = (imgY / b.height).coerceIn(0f, 1f)

                                        selectivePoints = selectivePoints.map {
                                            if (it.id == curId) it.copy(x = normX, y = normY) else it
                                        }
                                    }
                                )
                            } else if (activeTab == "brush") {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        val b = bitmap ?: return@detectDragGestures
                                        val fitScale = minOf(size.width / b.width.toFloat(), size.height / b.height.toFloat())
                                        val fitW = b.width * fitScale
                                        val fitH = b.height * fitScale
                                        val padX = (size.width - fitW) / 2f
                                        val padY = (size.height - fitH) / 2f
                                        val imgX = (offset.x - padX) / fitScale
                                        val imgY = (offset.y - padY) / fitScale

                                        activeStrokePoints.clear()
                                        activeStrokePoints.add(offset)
                                        activeStrokeRadius = selectiveBrushSize
                                        activeStrokeIsErase = selectiveIsErase

                                        val mappedOffset = Offset(imgX.coerceIn(0f, b.width.toFloat() - 1f), imgY.coerceIn(0f, b.height.toFloat() - 1f))
                                        val brushRadiusInBmp = (selectiveBrushSize / fitScale).coerceIn(4f, maxOf(b.width, b.height) * 0.25f)
                                        selectiveMaskEngine.startStroke(
                                            point = mappedOffset,
                                            radius = brushRadiusInBmp,
                                            hardness = selectiveBrushHardness,
                                            opacity = 1f,
                                            isErase = selectiveIsErase
                                        )
                                    },
                                    onDrag = { change, _ ->
                                        val b = bitmap ?: return@detectDragGestures
                                        val fitScale = minOf(size.width / b.width.toFloat(), size.height / b.height.toFloat())
                                        val fitW = b.width * fitScale
                                        val fitH = b.height * fitScale
                                        val padX = (size.width - fitW) / 2f
                                        val padY = (size.height - fitH) / 2f
                                        val imgX = (change.position.x - padX) / fitScale
                                        val imgY = (change.position.y - padY) / fitScale

                                        activeStrokePoints.add(change.position)

                                        val mappedOffset = Offset(imgX.coerceIn(0f, b.width.toFloat() - 1f), imgY.coerceIn(0f, b.height.toFloat() - 1f))
                                        val brushRadiusInBmp = (selectiveBrushSize / fitScale).coerceIn(4f, maxOf(b.width, b.height) * 0.25f)
                                        selectiveMaskEngine.continueStroke(
                                            currentPoint = mappedOffset,
                                            radius = brushRadiusInBmp,
                                            hardness = selectiveBrushHardness,
                                            opacity = 1f,
                                            isErase = selectiveIsErase
                                        )
                                    },
                                    onDragEnd = {
                                        selectiveMaskEngine.endStroke()
                                        activeStrokePoints.clear()
                                        maskVersion++
                                    }
                                )
                            } else if (activeTab == "object_remove") {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        val b = bitmap ?: return@detectDragGestures
                                        val fitScale = minOf(size.width / b.width.toFloat(), size.height / b.height.toFloat())
                                        val fitW = b.width * fitScale
                                        val fitH = b.height * fitScale
                                        val padX = (size.width - fitW) / 2f
                                        val padY = (size.height - fitH) / 2f
                                        val imgX = (offset.x - padX) / fitScale
                                        val imgY = (offset.y - padY) / fitScale

                                        activeStrokePoints.clear()
                                        activeStrokePoints.add(offset)
                                        activeStrokeRadius = healingBrushSize
                                        activeStrokeIsErase = healingIsErase

                                        val mappedOffset = Offset(imgX.coerceIn(0f, b.width.toFloat() - 1f), imgY.coerceIn(0f, b.height.toFloat() - 1f))
                                        val brushRadiusInBmp = (healingBrushSize / fitScale).coerceIn(4f, maxOf(b.width, b.height) * 0.25f)
                                        healingMaskEngine.startStroke(
                                            point = mappedOffset,
                                            radius = brushRadiusInBmp,
                                            hardness = 0.85f,
                                            opacity = 1f,
                                            isErase = healingIsErase
                                        )
                                    },
                                    onDrag = { change, _ ->
                                        val b = bitmap ?: return@detectDragGestures
                                        val fitScale = minOf(size.width / b.width.toFloat(), size.height / b.height.toFloat())
                                        val fitW = b.width * fitScale
                                        val fitH = b.height * fitScale
                                        val padX = (size.width - fitW) / 2f
                                        val padY = (size.height - fitH) / 2f
                                        val imgX = (change.position.x - padX) / fitScale
                                        val imgY = (change.position.y - padY) / fitScale

                                        activeStrokePoints.add(change.position)

                                        val mappedOffset = Offset(imgX.coerceIn(0f, b.width.toFloat() - 1f), imgY.coerceIn(0f, b.height.toFloat() - 1f))
                                        val brushRadiusInBmp = (healingBrushSize / fitScale).coerceIn(4f, maxOf(b.width, b.height) * 0.25f)
                                        healingMaskEngine.continueStroke(
                                            currentPoint = mappedOffset,
                                            radius = brushRadiusInBmp,
                                            hardness = 0.85f,
                                            opacity = 1f,
                                            isErase = healingIsErase
                                        )
                                    },
                                    onDragEnd = {
                                        healingMaskEngine.endStroke()
                                        activeStrokePoints.clear()
                                        healingMaskVersion++
                                    }
                                )
                            } else {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    canvasScale = (canvasScale * zoom).coerceIn(1f, 5f)
                                    if (canvasScale > 1f) {
                                        canvasOffset += pan
                                    } else {
                                        canvasOffset = androidx.compose.ui.geometry.Offset.Zero
                                    }
                                }
                            }
                        }
                ) {
                    val displayBmp = if (isComparingOriginal) (history.firstOrNull()?.bitmap ?: bitmap!!) else (liveDisplayBitmap ?: bitmap!!)
                    val activeColorMatrix = if (isComparingOriginal) ColorMatrix() else composeColorMatrix
                    Image(
                        bitmap = displayBmp.asImageBitmap(),
                        contentDescription = "Edit Canvas",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .drawWithContent {
                                // Draw transparency checkerboard tile pattern
                                val checkSize = 28f
                                val cols = (size.width / checkSize).toInt() + 1
                                val rows = (size.height / checkSize).toInt() + 1
                                for (r in 0 until rows) {
                                    for (c in 0 until cols) {
                                        val isEven = (r + c) % 2 == 0
                                        drawRect(
                                            color = if (isEven) Color(0xFF242424) else Color(0xFF1A1A1A),
                                            topLeft = androidx.compose.ui.geometry.Offset(c * checkSize, r * checkSize),
                                            size = androidx.compose.ui.geometry.Size(checkSize, checkSize)
                                        )
                                    }
                                }
                                drawContent()
                                val b = bitmap
                                if (!isComparingOriginal && b != null) {
                                    val fitScale = minOf(size.width / b.width.toFloat(), size.height / b.height.toFloat())
                                    val fitW = b.width * fitScale
                                    val fitH = b.height * fitScale
                                    val padX = (size.width - fitW) / 2f
                                    val padY = (size.height - fitH) / 2f

                                    if (doubleExposureBitmap != null) drawImage(doubleExposureBitmap!!, alpha = doubleExposureOpacity, blendMode = BlendMode.Screen)
                                    if (vignetteStrength > 0f) drawRect(androidx.compose.ui.graphics.Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = vignetteStrength)), center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f), radius = Math.min(size.width, size.height) / 1.2f))
                                    if (grainStrength > 0f) drawRect(Color.White.copy(alpha = grainStrength * 0.15f), blendMode = BlendMode.Overlay)
                                    if (lightLeakStrength > 0f) drawRect(androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFF5722).copy(alpha = lightLeakStrength * 0.4f), Color.Transparent)), blendMode = BlendMode.Screen)
                                    
                                    // Render Frame Styles
                                    when (frameStyle) {
                                        "white_border", "white" -> {
                                            val bw = size.width * 0.05f
                                            val bh = size.height * 0.05f
                                            drawRect(Color.White, topLeft = androidx.compose.ui.geometry.Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(size.width, bh))
                                            drawRect(Color.White, topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - bh), size = androidx.compose.ui.geometry.Size(size.width, bh))
                                            drawRect(Color.White, topLeft = androidx.compose.ui.geometry.Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(bw, size.height))
                                            drawRect(Color.White, topLeft = androidx.compose.ui.geometry.Offset(size.width - bw, 0f), size = androidx.compose.ui.geometry.Size(bw, size.height))
                                        }
                                        "polaroid" -> {
                                            val bw = size.width * 0.05f
                                            val topH = size.height * 0.05f
                                            val botH = size.height * 0.18f
                                            drawRect(Color.White, topLeft = androidx.compose.ui.geometry.Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(size.width, topH))
                                            drawRect(Color.White, topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - botH), size = androidx.compose.ui.geometry.Size(size.width, botH))
                                            drawRect(Color.White, topLeft = androidx.compose.ui.geometry.Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(bw, size.height))
                                            drawRect(Color.White, topLeft = androidx.compose.ui.geometry.Offset(size.width - bw, 0f), size = androidx.compose.ui.geometry.Size(bw, size.height))
                                        }
                                        "cinematic" -> {
                                            val barH = size.height * 0.14f
                                            drawRect(Color.Black, topLeft = androidx.compose.ui.geometry.Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(size.width, barH))
                                            drawRect(Color.Black, topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - barH), size = androidx.compose.ui.geometry.Size(size.width, barH))
                                        }
                                    }

                                    // Rule of Thirds & Compositional Grid for Crop & Rotate
                                    if (activeTab == "crop" || activeTab == "rotate") {
                                        val gridColor = Color.White.copy(alpha = 0.5f)
                                        drawLine(gridColor, Offset(size.width * 0.333f, 0f), Offset(size.width * 0.333f, size.height), strokeWidth = 1.5f)
                                        drawLine(gridColor, Offset(size.width * 0.666f, 0f), Offset(size.width * 0.666f, size.height), strokeWidth = 1.5f)
                                        drawLine(gridColor, Offset(0f, size.height * 0.333f), Offset(size.width, size.height * 0.333f), strokeWidth = 1.5f)
                                        drawLine(gridColor, Offset(0f, size.height * 0.666f), Offset(size.width, size.height * 0.666f), strokeWidth = 1.5f)
                                    }

                                    // Live Background Remover Crimson Highlight
                                    if (activeTab == "object_remove") {
                                        val v = healingMaskVersion
                                        if (v >= 0) {
                                            val maskBmp = healingMaskEngine.maskBitmap
                                            drawImage(
                                                image = maskBmp.asImageBitmap(),
                                                dstOffset = androidx.compose.ui.unit.IntOffset(padX.toInt(), padY.toInt()),
                                                dstSize = androidx.compose.ui.unit.IntSize(fitW.toInt(), fitH.toInt()),
                                                colorFilter = ColorFilter.tint(Color(0xFFFF2D55), BlendMode.SrcIn),
                                                alpha = 0.65f
                                            )
                                        }

                                        // Instant 120 FPS GPU Vector Active Stroke
                                        if (activeStrokePoints.size > 1) {
                                            val strokePath = androidx.compose.ui.graphics.Path()
                                            strokePath.moveTo(activeStrokePoints[0].x, activeStrokePoints[0].y)
                                            for (i in 1 until activeStrokePoints.size) {
                                                strokePath.lineTo(activeStrokePoints[i].x, activeStrokePoints[i].y)
                                            }
                                            drawPath(
                                                path = strokePath,
                                                color = if (activeStrokeIsErase) Color.Transparent else Color(0xFFFF2D55).copy(alpha = 0.65f),
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                    width = activeStrokeRadius * 2f,
                                                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                ),
                                                blendMode = if (activeStrokeIsErase) BlendMode.Clear else BlendMode.SrcOver
                                            )
                                        }

                                        // Live Circular Brush Reticle (Radius of selected brush)
                                        val currentPoint = activeStrokePoints.lastOrNull()
                                        if (currentPoint != null) {
                                            // Semi-transparent brush area fill
                                            drawCircle(
                                                color = if (activeStrokeIsErase) Color.White.copy(alpha = 0.15f) else Color(0xFFFF2D55).copy(alpha = 0.25f),
                                                radius = activeStrokeRadius,
                                                center = currentPoint
                                            )
                                            // Outer dark shadow ring for high contrast on light areas
                                            drawCircle(
                                                color = Color.Black.copy(alpha = 0.6f),
                                                radius = activeStrokeRadius + 1.5f,
                                                center = currentPoint,
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
                                            )
                                            // Primary glowing Amber / White ring matching brush radius
                                            drawCircle(
                                                color = if (activeStrokeIsErase) Color.White else Amber,
                                                radius = activeStrokeRadius,
                                                center = currentPoint,
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                            )
                                            // Center precision reticle dot
                                            drawCircle(
                                                color = Color.White,
                                                radius = 3.dp.toPx(),
                                                center = currentPoint
                                            )
                                        }
                                    }

                                    // Snapseed-Style Selective Control Points Overlay
                                    if (activeTab == "selective") {
                                        val selectedPoint = selectivePoints.firstOrNull { it.id == selectedPointId } ?: selectivePoints.lastOrNull()
                                        for (pt in selectivePoints) {
                                            val isSelected = pt.id == selectedPoint?.id
                                            val ptCenterX = padX + pt.x * fitW
                                            val ptCenterY = padY + pt.y * fitH
                                            val screenRadius = (pt.radius / (bitmap?.width?.toFloat() ?: 1f)) * fitW

                                            if (isSelected) {
                                                // 1. Translucent red rubylith fill inside circular radius
                                                drawCircle(
                                                    color = Color(0xFFFF2D55).copy(alpha = 0.22f),
                                                    radius = screenRadius,
                                                    center = Offset(ptCenterX, ptCenterY)
                                                )
                                                // 2. High contrast outer radius border
                                                drawCircle(
                                                    color = Color.Black.copy(alpha = 0.5f),
                                                    radius = screenRadius + 1.5f,
                                                    center = Offset(ptCenterX, ptCenterY),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
                                                )
                                                drawCircle(
                                                    color = Amber,
                                                    radius = screenRadius,
                                                    center = Offset(ptCenterX, ptCenterY),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = 2.dp.toPx(),
                                                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                                                    )
                                                )
                                                // 3. Center Control Pin Badge
                                                drawCircle(
                                                    color = Color.Black.copy(alpha = 0.7f),
                                                    radius = 16.dp.toPx(),
                                                    center = Offset(ptCenterX, ptCenterY)
                                                )
                                                drawCircle(
                                                    color = Amber,
                                                    radius = 13.dp.toPx(),
                                                    center = Offset(ptCenterX, ptCenterY)
                                                )
                                                drawCircle(
                                                    color = Ink900,
                                                    radius = 4.dp.toPx(),
                                                    center = Offset(ptCenterX, ptCenterY)
                                                )
                                            } else {
                                                // Unselected compact pin
                                                drawCircle(
                                                    color = Color.Black.copy(alpha = 0.6f),
                                                    radius = 11.dp.toPx(),
                                                    center = Offset(ptCenterX, ptCenterY)
                                                )
                                                drawCircle(
                                                    color = Color.White,
                                                    radius = 9.dp.toPx(),
                                                    center = Offset(ptCenterX, ptCenterY)
                                                )
                                                drawCircle(
                                                    color = Ink800,
                                                    radius = 3.dp.toPx(),
                                                    center = Offset(ptCenterX, ptCenterY)
                                                )
                                            }
                                        }
                                    }

                                    // Live Freehand Brush Crimson Highlight & Reticle
                                    if (activeTab == "brush") {
                                        val v = maskVersion
                                        if (v >= 0) {
                                            val maskBmp = selectiveMaskEngine.maskBitmap
                                            drawImage(
                                                image = maskBmp.asImageBitmap(),
                                                dstOffset = androidx.compose.ui.unit.IntOffset(padX.toInt(), padY.toInt()),
                                                dstSize = androidx.compose.ui.unit.IntSize(fitW.toInt(), fitH.toInt()),
                                                colorFilter = ColorFilter.tint(Color(0xFFFF2D55), BlendMode.SrcIn),
                                                alpha = 0.65f
                                            )
                                        }

                                        if (activeStrokePoints.size > 1) {
                                            val strokePath = androidx.compose.ui.graphics.Path()
                                            strokePath.moveTo(activeStrokePoints[0].x, activeStrokePoints[0].y)
                                            for (i in 1 until activeStrokePoints.size) {
                                                strokePath.lineTo(activeStrokePoints[i].x, activeStrokePoints[i].y)
                                            }
                                            drawPath(
                                                path = strokePath,
                                                color = if (activeStrokeIsErase) Color.Transparent else Color(0xFFFF2D55).copy(alpha = 0.65f),
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                    width = activeStrokeRadius * 2f,
                                                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                                                ),
                                                blendMode = if (activeStrokeIsErase) BlendMode.Clear else BlendMode.SrcOver
                                            )
                                        }

                                        // Live Circular Brush Reticle (Radius of selected brush)
                                        val currentPoint = activeStrokePoints.lastOrNull()
                                        if (currentPoint != null) {
                                            drawCircle(
                                                color = if (activeStrokeIsErase) Color.White.copy(alpha = 0.15f) else Color(0xFFFF2D55).copy(alpha = 0.25f),
                                                radius = activeStrokeRadius,
                                                center = currentPoint
                                            )
                                            drawCircle(
                                                color = Color.Black.copy(alpha = 0.6f),
                                                radius = activeStrokeRadius + 1.5f,
                                                center = currentPoint,
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f)
                                            )
                                            drawCircle(
                                                color = if (activeStrokeIsErase) Color.White else Amber,
                                                radius = activeStrokeRadius,
                                                center = currentPoint,
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                            )
                                            drawCircle(
                                                color = Color.White,
                                                radius = 3.dp.toPx(),
                                                center = currentPoint
                                            )
                                        }
                                    }
                                }
                            },
                        colorFilter = ColorFilter.colorMatrix(activeColorMatrix)
                    )
                    
                    if (!isComparingOriginal) {
                        overlays.forEach { item ->
                            val isActive = item.id == selectedOverlayId
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(item.x.roundToInt(), item.y.roundToInt()) }
                                    .graphicsLayer {
                                        scaleX = item.scale
                                        scaleY = item.scale
                                        rotationZ = item.rotation
                                    }
                                    .pointerInput(item.id) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            selectedOverlayId = item.id
                                            overlays = overlays.map {
                                                if (it.id == item.id) it.copy(x = it.x + dragAmount.x, y = it.y + dragAmount.y) else it
                                            }
                                        }
                                    }
                                    .pointerInput(item.id) {
                                        detectTransformGestures { _, pan, zoom, rotation ->
                                            selectedOverlayId = item.id
                                            overlays = overlays.map {
                                                if (it.id == item.id) it.copy(
                                                    x = it.x + pan.x,
                                                    y = it.y + pan.y,
                                                    scale = (it.scale * zoom).coerceIn(0.4f, 4.0f),
                                                    rotation = it.rotation + rotation
                                                ) else it
                                            }
                                        }
                                    }
                                    .clickable { selectedOverlayId = item.id }
                                    .then(if (isActive) Modifier.border(1.5.dp, Amber, RoundedCornerShape(4.dp)).padding(4.dp) else Modifier)
                            ) {
                                if (item.type == OverlayType.TEXT) {
                                    val fontFam = when(item.fontFamily) {
                                        "serif" -> androidx.compose.ui.text.font.FontFamily.Serif
                                        "mono" -> androidx.compose.ui.text.font.FontFamily.Monospace
                                        "cursive" -> androidx.compose.ui.text.font.FontFamily.Cursive
                                        else -> androidx.compose.ui.text.font.FontFamily.Default
                                    }
                                    Text(
                                        text = item.content,
                                        color = item.color,
                                        fontSize = 48.sp,
                                        fontFamily = fontFam,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .then(
                                                when (item.bgStyle) {
                                                    "box" -> Modifier.background(Color.Black.copy(alpha = 0.5f)).padding(horizontal = 8.dp, vertical = 4.dp)
                                                    "rounded" -> Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                                                    else -> Modifier
                                                }
                                            )
                                    )
                                } else if (item.type == OverlayType.STICKER) {
                                    Box(
                                        modifier = Modifier
                                            .border(1.5.dp, Color.White, RoundedCornerShape(6.dp))
                                            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = item.content,
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.5.sp
                                        )
                                    }
                                } else if (item.type == OverlayType.IMAGE_STICKER) {
                                    var stickerBmp by remember(item.content) { mutableStateOf<Bitmap?>(null) }
                                    LaunchedEffect(item.content) {
                                        withContext(Dispatchers.IO) {
                                            try {
                                                val stream = context.contentResolver.openInputStream(Uri.parse(item.content))
                                                stickerBmp = BitmapFactory.decodeStream(stream)
                                                stream?.close()
                                            } catch (e: Exception) { e.printStackTrace() }
                                        }
                                    }
                                    stickerBmp?.let {
                                        Image(bitmap = it.asImageBitmap(), contentDescription = "Image Sticker", modifier = Modifier.size(100.dp))
                                    }
                                }
                                if (isActive) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 12.dp, y = (-12).dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF3B30))
                                            .clickable {
                                                overlays = overlays.filter { it.id != item.id }
                                                selectedOverlayId = null
                                                commitHistory(overlays, bitmap)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Outlined.Close, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Top Badge when comparing original
                if (isComparingOriginal) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "ORIGINAL PHOTO",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp
                        )
                    }
                } else if (activeTab in listOf("tune_image", "adjust", "curves", "white_balance", "tonal_contrast")) {
                    StudioHistogram(
                        bitmap = liveDisplayBitmap ?: bitmap,
                        modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                    )
                }
            } else {
                CircularProgressIndicator(color = Amber, modifier = Modifier.size(36.dp))
            }
        }

        // Bottom Tools Drawer (When browsing all tools)
        AnimatedVisibility(
            visible = activeTab == "",
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Ink900)
            ) {
                CategoryTabRow(
                    activeCategory = activeCategory,
                    onCategorySelected = { activeCategory = it }
                )
                val visibleTools = remember(isVideo, activeCategory) {
                    if (isVideo) VIDEO_STUDIO_TOOLS.filter { activeCategory == StudioCategory.ALL || it.category == activeCategory }
                    else ALL_STUDIO_TOOLS.filter { activeCategory == StudioCategory.ALL || it.category == activeCategory }
                }
                ToolGrid(
                    tools = visibleTools,
                    onToolSelected = { openTool(it) },
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp, top = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = Ink800, contentColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Line.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 6.dp)
                    ) {
                        Text("CLOSE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 12.sp)
                    }
                }
            }
        }

        // Bottom Tool Adjustment Drawer (When a tool is selected)
        AnimatedVisibility(
            visible = activeTab != "",
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Ink850)
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().wrapContentHeight().heightIn(min = 190.dp), contentAlignment = Alignment.Center) {
                        when (activeTab) {
                            "selective" -> {
                                val selectedPoint = selectivePoints.firstOrNull { it.id == selectedPointId } ?: selectivePoints.lastOrNull()
                                com.snapstudio.app.ui.components.SelectivePointPanel(
                                    controlPoints = selectivePoints,
                                    selectedPoint = selectedPoint,
                                    onPointUpdated = { updated ->
                                        selectivePoints = selectivePoints.map { if (it.id == updated.id) updated else it }
                                    },
                                    onAddPointClicked = {
                                        val b = bitmap
                                        if (b != null) {
                                            val newPt = SelectiveControlPoint(
                                                x = 0.5f,
                                                y = 0.5f,
                                                radius = (b.width * 0.22f).coerceIn(80f, 400f),
                                                brightness = 0.25f
                                            )
                                            selectivePoints = selectivePoints + newPt
                                            selectedPointId = newPt.id
                                        }
                                    },
                                    onDeletePoint = {
                                        if (selectedPoint != null) {
                                            selectivePoints = selectivePoints.filter { it.id != selectedPoint.id }
                                            selectedPointId = selectivePoints.lastOrNull()?.id
                                        }
                                    },
                                    onResetPoint = {
                                        if (selectedPoint != null) {
                                            val reset = selectedPoint.copy(brightness = 0f, contrast = 1f, saturation = 1f, temperature = 0f)
                                            selectivePoints = selectivePoints.map { if (it.id == selectedPoint.id) reset else it }
                                        }
                                    }
                                )
                            }
                            "brush" -> com.snapstudio.app.ui.components.SelectiveBrushPanel(
                                activeMode = selectiveMode,
                                onModeChanged = { selectiveMode = it },
                                exposureEV = selectiveExposureEV,
                                onExposureChanged = { selectiveExposureEV = it },
                                temperature = selectiveTemperature,
                                onTemperatureChanged = { selectiveTemperature = it },
                                saturation = selectiveSaturation,
                                onSaturationChanged = { selectiveSaturation = it },
                                contrast = selectiveContrast,
                                onContrastChanged = { selectiveContrast = it },
                                brushSize = selectiveBrushSize,
                                onBrushSizeChanged = { selectiveBrushSize = it },
                                brushHardness = selectiveBrushHardness,
                                onBrushHardnessChanged = { selectiveBrushHardness = it },
                                isEraseMode = selectiveIsErase,
                                onToggleErase = { selectiveIsErase = it },
                                showMaskRubylith = selectiveShowRubylith,
                                onToggleMaskRubylith = { selectiveShowRubylith = it },
                                onInvertMask = { selectiveMaskEngine.invert(); maskVersion++ },
                                onClearMask = { selectiveMaskEngine.clear(); maskVersion++ },
                                onAiSelectSubject = {
                                    val cur = bitmap ?: return@SelectiveBrushPanel
                                    coroutineScope.launch {
                                        val subjectMask = AiSegmentationEngine.segmentSubject(cur)
                                        selectiveMaskEngine.restoreMask(subjectMask)
                                        maskVersion++
                                        selectiveShowRubylith = true
                                        Toast.makeText(context, "Subject Isolated for Grading", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onAiSelectBackground = {
                                    val cur = bitmap ?: return@SelectiveBrushPanel
                                    coroutineScope.launch {
                                        val bgMask = AiSegmentationEngine.segmentBackground(cur)
                                        selectiveMaskEngine.restoreMask(bgMask)
                                        maskVersion++
                                        selectiveShowRubylith = true
                                        Toast.makeText(context, "Background Isolated for Grading", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onAiSelectSky = {
                                    val cur = bitmap ?: return@SelectiveBrushPanel
                                    coroutineScope.launch {
                                        val skyMask = AiSegmentationEngine.segmentSky(cur)
                                        selectiveMaskEngine.restoreMask(skyMask)
                                        maskVersion++
                                        selectiveShowRubylith = true
                                        Toast.makeText(context, "Sky Isolated for Grading", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            "tune_image", "adjust", "colour" -> com.snapstudio.app.ui.components.TuneImagePanel(brightness, contrast, saturation, ambiance, highlights, shadows, warmth, {brightness=it}, {contrast=it}, {saturation=it}, {ambiance=it}, {highlights=it}, {shadows=it}, {warmth=it})
                            "details" -> com.snapstudio.app.ui.components.DetailsPanel(structure, sharpening, {structure=it}, {sharpening=it})
                            "tonal_contrast" -> com.snapstudio.app.ui.components.TonalContrastPanel(highTones, midTones, lowTones, protectShadows, protectHighlights, {highTones=it}, {midTones=it}, {lowTones=it}, {protectShadows=it}, {protectHighlights=it})
                            "curves" -> com.snapstudio.app.ui.components.CurvesPanel(luminance, redCurve, greenCurve, blueCurve, {luminance=it}, {redCurve=it}, {greenCurve=it}, {blueCurve=it})
                            "white_balance" -> com.snapstudio.app.ui.components.WhiteBalancePanel(temperature, tint, {temperature=it}, {tint=it})
                            "vignette" -> com.snapstudio.app.ui.components.VignettePanel(vignetteStrength, {vignetteStrength=it})
                            "dehaze" -> com.snapstudio.app.ui.components.DehazePanel(dehaze, {dehaze=it})
                            "grain" -> com.snapstudio.app.ui.components.GrainPanel(grainStrength, {grainStrength=it})
                            "light_leak" -> com.snapstudio.app.ui.components.LightLeakPanel(lightLeakStrength, {lightLeakStrength=it})
                            "frames" -> com.snapstudio.app.ui.components.FramesPanel(frameStyle, {frameStyle=it})
                            "double_exposure" -> com.snapstudio.app.ui.components.DoubleExposurePanel(doubleExposureOpacity, {doubleExposureOpacity=it}, { doubleExposurePicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })
                            "lens_blur" -> {
                                LensBlurPanel(
                                    blurStrength = lensBlurStrength,
                                    onBlurStrengthChanged = { lensBlurStrength = it },
                                    blurShape = lensBlurShape,
                                    onBlurShapeChanged = { lensBlurShape = it },
                                    focalSize = lensBlurFocalSize,
                                    onFocalSizeChanged = { lensBlurFocalSize = it }
                                )
                            }
                            "vintage", "bw", "noir", "drama", "hdr_scape", "retrolux", "grunge" -> {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(activeTab.uppercase(), color = Amber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Preview Active • Tap Apply to Confirm or Cancel", color = FgMuted, fontSize = 13.sp)
                                }
                            }
                            "object_remove" -> {
                                BackgroundRemoverPanel(
                                    brushSize = healingBrushSize,
                                    onBrushSizeChanged = { healingBrushSize = it },
                                    isEraseMode = healingIsErase,
                                    onToggleErase = { healingIsErase = it },
                                    isProcessing = isHealingInProgress,
                                    onRemoveBackground = {
                                        val cur = bitmap ?: return@BackgroundRemoverPanel
                                        if (!isHealingInProgress) {
                                            isHealingInProgress = true
                                            coroutineScope.launch {
                                                try {
                                                    val cutout = AiSegmentationEngine.removeBackground(cur)
                                                    bitmap = cutout
                                                    commitHistory(overlays, cutout)
                                                    isHealingInProgress = false
                                                    Toast.makeText(context, "Background Removed!", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    isHealingInProgress = false
                                                    Toast.makeText(context, "Failed to remove background", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    onApplyBrushErase = {
                                        val cur = bitmap
                                        if (cur != null && !isHealingInProgress) {
                                            isHealingInProgress = true
                                            coroutineScope.launch {
                                                try {
                                                    val erased = FastInpaintingEngine.inpaint(
                                                        source = cur,
                                                        mask = healingMaskEngine.maskBitmap,
                                                        radius = (healingBrushSize * 0.15f).toInt().coerceIn(4, 12)
                                                    )
                                                    bitmap = erased
                                                    healingMaskEngine.clear()
                                                    healingMaskVersion++
                                                    isHealingInProgress = false
                                                    commitHistory(overlays, erased)
                                                    Toast.makeText(context, "Brushed Area Erased!", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    isHealingInProgress = false
                                                    Toast.makeText(context, "Erase failed", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    onClearSelection = {
                                        healingMaskEngine.clear()
                                        healingMaskVersion++
                                    }
                                )
                            }
                            "face_restore" -> {
                                FaceRestorePanel(
                                    skinSmooth = faceSkinSmooth,
                                    onSkinSmoothChanged = { faceSkinSmooth = it },
                                    eyeClarity = faceEyeClarity,
                                    onEyeClarityChanged = { faceEyeClarity = it },
                                    teethWhiten = faceTeethWhiten,
                                    onTeethWhitenChanged = { faceTeethWhiten = it },
                                    relightIntensity = faceRelightIntensity,
                                    onRelightIntensityChanged = { faceRelightIntensity = it },
                                    relightAngle = faceRelightAngle,
                                    onRelightAngleChanged = { faceRelightAngle = it },
                                    isProcessing = isFaceProcessing,
                                    onApplyEnhance = {
                                        val cur = bitmap
                                        if (cur != null && !isFaceProcessing) {
                                            isFaceProcessing = true
                                            coroutineScope.launch {
                                                try {
                                                    val enhanced = AiFaceMeshEngine.retouchPortrait(
                                                        source = cur,
                                                        skinSmooth = faceSkinSmooth,
                                                        eyeClarity = faceEyeClarity,
                                                        teethWhiten = faceTeethWhiten,
                                                        relightIntensity = faceRelightIntensity,
                                                        relightAngleDeg = faceRelightAngle
                                                    )
                                                    bitmap = enhanced
                                                    commitHistory(overlays, enhanced)
                                                    isFaceProcessing = false
                                                    Toast.makeText(context, "468-Point AI Portrait Applied!", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    isFaceProcessing = false
                                                    Toast.makeText(context, "Retouch failed", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                            "text" -> {
                                var customText by remember { mutableStateOf("") }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(value = customText, onValueChange = { customText = it }, placeholder = { Text("Type custom text...", color = FgMuted) }, singleLine = true, modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Fg, unfocusedTextColor = Fg, focusedBorderColor = Amber, unfocusedBorderColor = Line))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(onClick = {
                                            if (customText.isNotBlank()) {
                                                val newItem = OverlayItem(
                                                    id = UUID.randomUUID().toString(),
                                                    type = OverlayType.TEXT,
                                                    content = customText,
                                                    x = 0f,
                                                    y = 0f
                                                )
                                                overlays = overlays + newItem
                                                selectedOverlayId = newItem.id
                                                commitHistory(overlays, bitmap)
                                                customText = ""
                                            }
                                        }, colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Ink900)) { Text("Add") }
                                    }
                                }
                            }
                            "crop" -> {
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    listOf("1:1" to 1f, "3:4" to 0.75f, "16:9" to 16f/9f).forEach { (label, ratio) ->
                                        Box(modifier = Modifier.background(Ink700, RoundedCornerShape(12.dp)).clickable {
                                            val cur = bitmap ?: return@clickable
                                            val cr = cur.width.toFloat() / cur.height.toFloat()
                                            val (nw, nh) = if (cr > ratio) (cur.height * ratio).toInt() to cur.height else cur.width to (cur.width / ratio).toInt()
                                            if (nw > 0 && nh > 0) {
                                                val cropped = Bitmap.createBitmap(cur, (cur.width - nw)/2, (cur.height - nh)/2, nw, nh)
                                                bitmap = cropped
                                                commitHistory(overlays, cropped)
                                            }
                                        }.padding(horizontal = 16.dp, vertical = 8.dp)) { Text(label, color = Fg, fontWeight = FontWeight.Bold) }
                                    }
                                    Box(modifier = Modifier.background(Ink700, RoundedCornerShape(12.dp)).clickable {
                                        val cur = bitmap ?: return@clickable
                                        val m = android.graphics.Matrix().apply { postRotate(90f) }
                                        val rotated = Bitmap.createBitmap(cur, 0, 0, cur.width, cur.height, m, true)
                                        bitmap = rotated
                                        commitHistory(overlays, rotated)
                                    }.padding(horizontal = 16.dp, vertical = 8.dp)) { Text("Rotate", color = Fg, fontWeight = FontWeight.Bold) }
                                }
                            }
                            "trim" -> {
                                if (isVideo) {
                                    var durationMs by remember { mutableStateOf(0L) }
                                    LaunchedEffect(exoPlayer) {
                                        while (durationMs == 0L) {
                                            val dur = exoPlayer?.duration ?: 0L
                                            if (dur > 0) durationMs = dur
                                            kotlinx.coroutines.delay(100)
                                        }
                                    }
                                    if (durationMs > 0L) {
                                        com.snapstudio.app.ui.components.VideoTimeline(durationMs, videoTrimStart, if (videoTrimEnd == -1L) durationMs else videoTrimEnd, onTrimChanged = { start, end -> videoTrimStart = start; videoTrimEnd = end; exoPlayer?.seekTo(start) })
                                    }
                                }
                            }
                            "filters" -> com.snapstudio.app.ui.components.FilterCarousel(
                                filters = com.snapstudio.app.filters.FilterPresetRepository.allFilters,
                                activeFilterId = null,
                                thumbnailBitmap = thumbnailImageBitmap,
                                onSelect = { if (it is com.snapstudio.app.filters.FilterPreset.ColorMatrix) selectedFilterMatrix = it.matrix }
                            )
                            "stickers" -> {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Badges & Watermarks", color = Fg, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Amber).clickable { customStickerPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }.padding(horizontal = 12.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = "Custom", tint = Ink900, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Custom", color = Ink900, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                            val stamps = listOf("RAW", "PRO", "35mm", "4K", "REC", "STUDIO", "ISO 100", "CINEMA", "LEICA", "HDR", "SNAP")
                                            items(stamps) { stamp ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Ink700)
                                                        .border(1.dp, Line.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                        .clickable {
                                                            val newItem = OverlayItem(
                                                                id = UUID.randomUUID().toString(),
                                                                type = OverlayType.STICKER,
                                                                content = stamp,
                                                                x = 0f,
                                                                y = 0f
                                                            )
                                                            overlays = overlays + newItem
                                                            selectedOverlayId = newItem.id
                                                            commitHistory(overlays, bitmap)
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = stamp, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else -> com.snapstudio.app.ui.components.GenericToolPanel(activeTab, genericIntensity, { genericIntensity = it })
                        }
                    }
                    Divider(color = Line, thickness = 1.dp)
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Close, contentDescription = "Cancel", tint = Color(0xFFFF5252), modifier = Modifier.size(28.dp).clickable { cancelTool() })
                        Text(text = if (activeTab == "object_remove") "BACKGROUND REMOVER" else activeTab.replace("_", " ").uppercase(), color = Fg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Outlined.Check, contentDescription = "Apply", tint = Amber, modifier = Modifier.size(28.dp).clickable { applyTool() })
                    }
                }
            }
        }

        // Cloud Generative AI Replace Dialog
        if (showGenerativeReplaceDialog) {
            AlertDialog(
                onDismissRequest = { if (!isGenerativeProcessing) showGenerativeReplaceDialog = false },
                containerColor = Ink850,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Amber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini Generative Fill", color = Fg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column {
                        Text(
                            "Enter a prompt to photorealistically generate or replace the masked region:",
                            color = FgMuted,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = generativePrompt,
                            onValueChange = { generativePrompt = it },
                            placeholder = { Text("e.g. A vintage camera on a wooden table", color = FgMuted, fontSize = 13.sp) },
                            singleLine = false,
                            maxLines = 3,
                            enabled = !isGenerativeProcessing,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Fg,
                                unfocusedTextColor = Fg,
                                focusedBorderColor = Amber,
                                unfocusedBorderColor = Ink700
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (isGenerativeProcessing) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = Amber, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Synthesizing neural textures...", color = Amber, fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cur = bitmap
                            if (cur != null && generativePrompt.isNotBlank() && !isGenerativeProcessing) {
                                isGenerativeProcessing = true
                                coroutineScope.launch {
                                    try {
                                        val replaced = GeminiGenerativeEngine.generativeReplace(
                                            source = cur,
                                            mask = healingMaskEngine.maskBitmap,
                                            prompt = generativePrompt
                                        )
                                        bitmap = replaced
                                        healingMaskEngine.clear()
                                        healingMaskVersion++
                                        commitHistory(overlays, replaced)
                                        isGenerativeProcessing = false
                                        showGenerativeReplaceDialog = false
                                        generativePrompt = ""
                                        Toast.makeText(context, "Generative Replacement Applied!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        isGenerativeProcessing = false
                                        Toast.makeText(context, "Generation failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = !isGenerativeProcessing && generativePrompt.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Ink900),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Generate", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showGenerativeReplaceDialog = false },
                        enabled = !isGenerativeProcessing
                    ) {
                        Text("Cancel", color = FgMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun ImageStickerView(uriString: String) {
    val context = LocalContext.current
    var stickerBitmap by remember(uriString) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(uriString) {
        withContext(Dispatchers.IO) {
            try {
                val stream = context.contentResolver.openInputStream(Uri.parse(uriString))
                stickerBitmap = BitmapFactory.decodeStream(stream)
                stream?.close()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
    stickerBitmap?.let { b ->
        Image(
            bitmap = b.asImageBitmap(),
            contentDescription = "Custom Sticker",
            modifier = Modifier.size(100.dp),
            contentScale = ContentScale.Fit
        )
    }
}
