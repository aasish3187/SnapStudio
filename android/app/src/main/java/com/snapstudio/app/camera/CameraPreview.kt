package com.snapstudio.app.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.snapstudio.app.filters.FilterPreset
import com.snapstudio.app.ui.theme.Amber
import com.snapstudio.app.ui.theme.Line
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt

private const val TAG = "SnapStudioCam"

@Composable
fun CameraPreview(
    imageCapture: ImageCapture,
    videoCapture: androidx.camera.video.VideoCapture<androidx.camera.video.Recorder>?,
    mode: String = "photo",
    activeFilter: FilterPreset?,
    filterIntensity: Float,
    lensFacing: Int,
    onFlipCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var activeCamera by remember { mutableStateOf<Camera?>(null) }

    // Focus & Metering Animation State
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    var focusVisible by remember { mutableStateOf(false) }

    LaunchedEffect(focusPoint) {
        if (focusPoint != null) {
            focusVisible = true
            delay(1200)
            focusVisible = false
        }
    }

    val focusScale by animateFloatAsState(
        targetValue = if (focusVisible) 1f else 1.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "focusScale"
    )

    // Pinch-to-Zoom State
    var zoomRatio by remember { mutableFloatStateOf(1.0f) }
    var minZoomRatio by remember { mutableFloatStateOf(1.0f) }
    var maxZoomRatio by remember { mutableFloatStateOf(8.0f) }
    var zoomRatioText by remember { mutableStateOf<String?>(null) }
    var zoomVisible by remember { mutableStateOf(false) }

    LaunchedEffect(zoomRatioText) {
        if (zoomRatioText != null) {
            zoomVisible = true
            delay(1200)
            zoomVisible = false
        }
    }

    // Update live hardware filter layer whenever activeFilter or intensity changes
    LaunchedEffect(activeFilter, filterIntensity, previewView) {
        val view = previewView ?: return@LaunchedEffect
        if (activeFilter == null || activeFilter.id == "original" || filterIntensity <= 0f) {
            view.setLayerType(android.view.View.LAYER_TYPE_NONE, null)
        } else {
            when (activeFilter) {
                is FilterPreset.ColorMatrix -> {
                    val targetMatrix = activeFilter.matrix
                    val identity = com.snapstudio.app.filters.ColorMatrixUtils.NORMAL
                    val interpolated = FloatArray(20) { i ->
                        identity[i] + (targetMatrix[i] - identity[i]) * filterIntensity
                    }
                    val paint = android.graphics.Paint().apply {
                        colorFilter = android.graphics.ColorMatrixColorFilter(android.graphics.ColorMatrix(interpolated))
                    }
                    view.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, paint)
                }
                is FilterPreset.ShaderEffect -> {
                    view.setLayerType(android.view.View.LAYER_TYPE_NONE, null)
                }
                is FilterPreset.LutEffect -> {
                    view.setLayerType(android.view.View.LAYER_TYPE_NONE, null)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(activeCamera) {
                // Pinch to Zoom Gesture
                detectTransformGestures { _, _, zoom, _ ->
                    val cam = activeCamera ?: return@detectTransformGestures
                    val current = zoomRatio
                    val target = (current * zoom).coerceIn(minZoomRatio, maxZoomRatio)
                    cam.cameraControl.setZoomRatio(target)
                    zoomRatio = target
                    zoomRatioText = String.format("%.1fx", target)
                }
            }
            .pointerInput(activeCamera, previewView) {
                // Tap to Focus & Double Tap to Flip Camera
                detectTapGestures(
                    onDoubleTap = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        onFlipCamera()
                    },
                    onTap = { offset ->
                        focusPoint = offset
                        val view = previewView
                        val cam = activeCamera
                        if (view != null && cam != null) {
                            try {
                                val factory = view.meteringPointFactory
                                val point = factory.createPoint(offset.x, offset.y)
                                val action = FocusMeteringAction.Builder(
                                    point,
                                    FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                                )
                                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
                                    .build()
                                cam.cameraControl.startFocusAndMetering(action)
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                )
            }
    ) {
        // Native Hardware-Accelerated CameraX PreviewView
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }.also {
                    previewView = it
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Real-time Visual Filter Overlay on Preview for Vignette / Tint / Atmospheric Effects
        if (activeFilter != null && filterIntensity > 0f) {
            when (activeFilter) {
                is FilterPreset.ShaderEffect -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (activeFilter.vignette > 0f) {
                            val radius = size.minDimension / 1.15f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f * activeFilter.vignette * filterIntensity)),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    radius = radius
                                )
                            )
                        }
                    }
                }
                is FilterPreset.LutEffect -> {
                    val tintColor = activeFilter.thumbnailColors.firstOrNull() ?: Color(0xFFD4A373)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            color = tintColor.copy(alpha = 0.28f * filterIntensity)
                        )
                    }
                }
                else -> {}
            }
        }

        // Tap-to-Detail Concentrate Indicator Ring / Brackets
        if (focusVisible && focusPoint != null) {
            val point = focusPoint!!
            Box(
                modifier = Modifier
                    .offset { IntOffset((point.x - 34.dp.toPx()).roundToInt(), (point.y - 34.dp.toPx()).roundToInt()) }
                    .size(68.dp)
                    .graphicsLayer {
                        scaleX = focusScale
                        scaleY = focusScale
                        alpha = if (focusVisible) 1f else 0f
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 2.dp.toPx()
                    val bracketLen = 14.dp.toPx()
                    val w = size.width
                    val h = size.height

                    // Top Left Bracket
                    drawLine(Amber, Offset(0f, 0f), Offset(bracketLen, 0f), stroke)
                    drawLine(Amber, Offset(0f, 0f), Offset(0f, bracketLen), stroke)

                    // Top Right Bracket
                    drawLine(Amber, Offset(w, 0f), Offset(w - bracketLen, 0f), stroke)
                    drawLine(Amber, Offset(w, 0f), Offset(w, bracketLen), stroke)

                    // Bottom Left Bracket
                    drawLine(Amber, Offset(0f, h), Offset(bracketLen, h), stroke)
                    drawLine(Amber, Offset(0f, h), Offset(0f, h - bracketLen), stroke)

                    // Bottom Right Bracket
                    drawLine(Amber, Offset(w, h), Offset(w - bracketLen, h), stroke)
                    drawLine(Amber, Offset(w, h), Offset(w, h - bracketLen), stroke)

                    // Center Focus Dot
                    drawCircle(Amber, radius = 2.5.dp.toPx())
                }
            }
        }

        // Floating Zoom Indicator Badge
        AnimatedVisibility(
            visible = zoomVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 64.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, Line.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = zoomRatioText ?: "1.0x",
                    color = Amber,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // CameraX Binding
        LaunchedEffect(lensFacing, previewView, mode, videoCapture) {
            val view = previewView ?: return@LaunchedEffect
            val cameraProvider = context.getCameraProvider()

            try {
                cameraProvider.unbindAll()

                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build()

                preview.setSurfaceProvider(view.surfaceProvider)

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                val useCases = mutableListOf<androidx.camera.core.UseCase>(preview)
                if (mode == "video" && videoCapture != null) {
                    useCases.add(videoCapture)
                } else {
                    useCases.add(imageCapture)
                }

                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    *useCases.toTypedArray()
                )
                activeCamera = camera
                
                // Track zoom state range
                camera.cameraInfo.zoomState.observe(lifecycleOwner) { state ->
                    minZoomRatio = state.minZoomRatio
                    maxZoomRatio = state.maxZoomRatio.coerceAtMost(10.0f)
                    zoomRatio = state.zoomRatio
                }
                
                Log.d(TAG, "CameraX bound successfully (mode: $mode)")
            } catch (exc: Exception) {
                Log.e(TAG, "CameraX bind failed", exc)
            }
        }
    }
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}
