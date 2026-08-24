package com.snapstudio.app.settings

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapstudio.app.ui.theme.Amber
import com.snapstudio.app.ui.theme.Fg
import com.snapstudio.app.ui.theme.FgMuted
import com.snapstudio.app.ui.theme.Ink700
import com.snapstudio.app.ui.theme.Ink900
import com.snapstudio.app.ui.components.ChromeButton

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsManager = SettingsManager(this)
        
        setContent {
            MaterialTheme {
                SettingsScreen(
                    settingsManager = settingsManager,
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(settingsManager: SettingsManager, onBack: () -> Unit) {
    val context = LocalContext.current
    
    // State variables tied to SettingsManager
    var defaultLensFacing by remember { mutableIntStateOf(settingsManager.defaultLensFacing) }
    var showGridLines by remember { mutableStateOf(settingsManager.showGridLines) }
    var saveOriginals by remember { mutableStateOf(settingsManager.saveOriginals) }
    var videoExportQuality by remember { mutableIntStateOf(settingsManager.videoExportQuality) }
    var addWatermark by remember { mutableStateOf(settingsManager.addWatermark) }
    var hapticFeedback by remember { mutableStateOf(settingsManager.hapticFeedback) }
    var autoSaveToGallery by remember { mutableStateOf(settingsManager.autoSaveToGallery) }
    
    // Effect block to update SettingsManager whenever state changes
    LaunchedEffect(defaultLensFacing, showGridLines, saveOriginals, videoExportQuality, addWatermark, hapticFeedback, autoSaveToGallery) {
        settingsManager.defaultLensFacing = defaultLensFacing
        settingsManager.showGridLines = showGridLines
        settingsManager.saveOriginals = saveOriginals
        settingsManager.videoExportQuality = videoExportQuality
        settingsManager.addWatermark = addWatermark
        settingsManager.hapticFeedback = hapticFeedback
        settingsManager.autoSaveToGallery = autoSaveToGallery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink900)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChromeButton(
                icon = Icons.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Settings",
                color = Fg,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // Scrollable settings list
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            SettingsCategoryTitle("CAMERA")
            
            SettingsSegmentedControl(
                label = "Default Camera Lens",
                options = listOf("Back", "Front"),
                selectedIndex = if (defaultLensFacing == androidx.camera.core.CameraSelector.LENS_FACING_BACK) 0 else 1,
                onSelected = { index ->
                    defaultLensFacing = if (index == 0) androidx.camera.core.CameraSelector.LENS_FACING_BACK else androidx.camera.core.CameraSelector.LENS_FACING_FRONT
                }
            )
            SettingsSwitch(
                label = "Camera Grid Lines",
                description = "Show 3x3 grid to compose shots",
                checked = showGridLines,
                onCheckedChange = { showGridLines = it }
            )
            SettingsSwitch(
                label = "Save Originals",
                description = "Keep a copy of unedited media",
                checked = saveOriginals,
                onCheckedChange = { saveOriginals = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            SettingsCategoryTitle("EXPORT")
            
            SettingsSegmentedControl(
                label = "Video Export Quality",
                options = listOf("1080p", "720p", "480p"),
                selectedIndex = when(videoExportQuality) {
                    1080 -> 0
                    720 -> 1
                    480 -> 2
                    else -> 0
                },
                onSelected = { index ->
                    videoExportQuality = when(index) {
                        0 -> 1080
                        1 -> 720
                        2 -> 480
                        else -> 1080
                    }
                }
            )
            SettingsSwitch(
                label = "Auto-Save to Gallery",
                description = "Automatically save new media upon capture",
                checked = autoSaveToGallery,
                onCheckedChange = { autoSaveToGallery = it }
            )
            SettingsSwitch(
                label = "SnapStudio Watermark",
                description = "Add logo to exported media",
                checked = addWatermark,
                onCheckedChange = { addWatermark = it }
            )

            Spacer(modifier = Modifier.height(24.dp))
            SettingsCategoryTitle("APP EXPERIENCE")
            
            SettingsSwitch(
                label = "Haptic Feedback",
                description = "Vibrate on UI interactions",
                checked = hapticFeedback,
                onCheckedChange = { hapticFeedback = it }
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            // Clear Cache Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Ink700)
                    .clickable {
                        settingsManager.clearCache(context)
                        Toast.makeText(context, "Cache Cleared!", Toast.LENGTH_SHORT).show()
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Clear Internal Cache", color = Amber, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun SettingsCategoryTitle(title: String) {
    Text(
        text = title,
        color = FgMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun SettingsSwitch(label: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Fg, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(description, color = FgMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Fg,
                checkedTrackColor = Amber,
                uncheckedThumbColor = FgMuted,
                uncheckedTrackColor = Ink700
            )
        )
    }
}

@Composable
fun SettingsSegmentedControl(label: String, options: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(label, color = Fg, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Ink700)
                .padding(4.dp)
        ) {
            options.forEachIndexed { index, option ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedIndex == index) Ink900 else Color.Transparent)
                        .clickable { onSelected(index) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (selectedIndex == index) Fg else FgMuted,
                        fontSize = 14.sp,
                        fontWeight = if (selectedIndex == index) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
