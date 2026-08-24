package com.snapstudio.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.snapstudio.app.camera.CameraPreview
import com.snapstudio.app.camera.PhotoCaptureHelper
import com.snapstudio.app.ui.components.ChromeButton
import com.snapstudio.app.ui.components.ShutterButton
import com.snapstudio.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.setBackgroundColor(android.graphics.Color.BLACK)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    CameraScreen()
                }
            }
        }
    }

    external fun getCoreVersion(): String

    companion object {
        init {
            System.loadLibrary("snapstudio")
        }
    }
}

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var activeFilter by remember { mutableStateOf<com.snapstudio.app.filters.FilterPreset?>(null) }
    var filterIntensity by remember { mutableFloatStateOf(1.0f) }
    var flashOn by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf("photo") }

    var lastCapturedPhotoUri by remember { mutableStateOf<String?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var captureNotification by remember { mutableStateOf<String?>(null) }

    fun showCaptureAlert(message: String) {
        captureNotification = message
        coroutineScope.launch {
            delay(2600)
            if (captureNotification == message) {
                captureNotification = null
            }
        }
    }

    val settingsManager = remember { com.snapstudio.app.settings.SettingsManager(context) }
    
    var showGridLines by remember { mutableStateOf(settingsManager.showGridLines) }
    var lensFacing by remember { mutableIntStateOf(settingsManager.defaultLensFacing) }
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                showGridLines = settingsManager.showGridLines
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (hasPermission) {
        val imageCapture = remember { 
            ImageCapture.Builder()
                .setTargetRotation(android.view.Surface.ROTATION_0)
                .build() 
        }

        DisposableEffect(Unit) {
            val orientationEventListener = object : android.view.OrientationEventListener(context) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) return
                    val rotation = when (orientation) {
                        in 45..134 -> android.view.Surface.ROTATION_270
                        in 135..224 -> android.view.Surface.ROTATION_180
                        in 225..314 -> android.view.Surface.ROTATION_90
                        else -> android.view.Surface.ROTATION_0
                    }
                    imageCapture.targetRotation = rotation
                }
            }
            orientationEventListener.enable()
            onDispose {
                orientationEventListener.disable()
            }
        }

        val videoCaptureHelper = remember { com.snapstudio.app.camera.VideoCaptureHelper(context) }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // Live GL Camera Preview with Double Tap Flip, Pinch Zoom, and Tap to Focus
            CameraPreview(
                imageCapture = imageCapture, 
                videoCapture = videoCaptureHelper.videoCapture,
                mode = mode,
                activeFilter = activeFilter,
                filterIntensity = filterIntensity,
                lensFacing = lensFacing,
                onFlipCamera = {
                    lensFacing = if (lensFacing == androidx.camera.core.CameraSelector.LENS_FACING_BACK) {
                        androidx.camera.core.CameraSelector.LENS_FACING_FRONT
                    } else {
                        androidx.camera.core.CameraSelector.LENS_FACING_BACK
                    }
                }
            )

            // Grid Lines Overlay
            if (showGridLines) {
                com.snapstudio.app.ui.components.CameraGrid()
            }

            // Recording Status Badge
            if (isCapturing && mode == "video") {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 70.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color.Red)
                    )
                    Text(
                        text = "RECORDING",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            // Top Scrim + Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(ScrimStrong, Color.Transparent)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChromeButton(
                        icon = if (flashOn) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                        contentDescription = "Toggle Flash",
                        active = flashOn,
                        onClick = { 
                            if (settingsManager.hapticFeedback) haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            flashOn = !flashOn 
                        }
                    )

                    // Mode Toggle
                    Row(
                        modifier = Modifier
                            .background(ScrimStrong, RoundedCornerShape(20.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (mode == "photo") Ink700 else Color.Transparent)
                            .clickable { 
                                if (settingsManager.hapticFeedback) haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                mode = "photo" 
                            }
                            .padding(8.dp)
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, "Photo Mode", tint = if (mode == "photo") Fg else FgMuted, modifier = Modifier.size(20.dp))
                        }
                        Box(modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (mode == "video") Ink700 else Color.Transparent)
                            .clickable { 
                                if (settingsManager.hapticFeedback) haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                mode = "video" 
                            }
                            .padding(8.dp)
                        ) {
                            Icon(Icons.Outlined.Videocam, "Video Mode", tint = if (mode == "video") Fg else FgMuted, modifier = Modifier.size(20.dp))
                        }
                    }

                    ChromeButton(
                        icon = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        onClick = { context.startActivity(android.content.Intent(context, com.snapstudio.app.settings.SettingsActivity::class.java)) }
                    )
                }
            }

            // Capture Saved Alert / Notification Pill at Top
            AnimatedVisibility(
                visible = captureNotification != null,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 70.dp)
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .border(1.dp, Amber.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .clickable {
                            context.startActivity(android.content.Intent(context, com.snapstudio.app.gallery.GalleryActivity::class.java))
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Saved",
                        tint = Amber,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = captureNotification ?: "",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Bottom Controls Scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, ScrimStrong),
                            startY = 0f
                        )
                    )
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    
                    // Rotary Filter Carousel with Integrated Intensity Pill Header
                    com.snapstudio.app.ui.components.FilterCarousel(
                        filters = com.snapstudio.app.filters.FilterPresetRepository.allFilters,
                        activeFilterId = activeFilter?.id,
                        thumbnailBitmap = null,
                        filterIntensity = filterIntensity,
                        onIntensityChanged = { filterIntensity = it },
                        onSelect = { preset -> 
                            if (activeFilter?.id == preset.id) {
                                activeFilter = null // Toggle off
                            } else {
                                activeFilter = preset
                                filterIntensity = 1.0f
                            }
                        }
                    )
                    
                    // Shutter Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 16.dp)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gallery Shortcut
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .border(2.dp, LineStrong, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Ink700)
                                .clickable { 
                                    context.startActivity(android.content.Intent(context, com.snapstudio.app.gallery.GalleryActivity::class.java))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoLibrary,
                                contentDescription = "Gallery",
                                tint = Fg,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Shutter
                        ShutterButton(
                            isRecording = isCapturing && mode == "video",
                            mode = mode,
                            onCapture = {
                                if (settingsManager.hapticFeedback) {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                }
                                if (mode == "photo") {
                                    isCapturing = true
                                    PhotoCaptureHelper.takePhoto(
                                        context = context,
                                        imageCapture = imageCapture,
                                        activeFilter = activeFilter,
                                        saveToGallery = settingsManager.autoSaveToGallery,
                                        onPhotoCaptured = { uriString ->
                                            lastCapturedPhotoUri = uriString
                                            isCapturing = false
                                            showCaptureAlert("Photo saved to Gallery")
                                        },
                                        onError = {
                                            Toast.makeText(context, "Capture failed", Toast.LENGTH_SHORT).show()
                                            isCapturing = false
                                        }
                                    )
                                } else {
                                    if (isCapturing) {
                                        videoCaptureHelper.stopRecording()
                                        isCapturing = false
                                    } else {
                                        isCapturing = true
                                        videoCaptureHelper.startRecording(
                                            executor = ContextCompat.getMainExecutor(context),
                                            onVideoSaved = { uri ->
                                                lastCapturedPhotoUri = uri.toString()
                                                isCapturing = false
                                                showCaptureAlert("Video saved to Gallery")
                                            },
                                            onError = { error ->
                                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                                isCapturing = false
                                            }
                                        )
                                    }
                                }
                            }
                        )

                        // Flip Camera Shortcut
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(ScrimStrong)
                                .clickable {
                                    if (settingsManager.hapticFeedback) haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    lensFacing = if (lensFacing == androidx.camera.core.CameraSelector.LENS_FACING_BACK) {
                                        androidx.camera.core.CameraSelector.LENS_FACING_FRONT
                                    } else {
                                        androidx.camera.core.CameraSelector.LENS_FACING_BACK
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.FlipCameraAndroid,
                                contentDescription = "Flip Camera",
                                tint = Fg,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().background(Ink900), contentAlignment = Alignment.Center) {
            Text(text = "Camera permission is required.", color = Fg)
        }
    }
}
