package com.snapstudio.app.editing

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.InputStream

class AIStudioActivity : ComponentActivity() {

    private val viewModel = PhotoEditViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val uriStr = intent.getStringExtra("IMAGE_URI")
        if (uriStr != null) {
            val uri = Uri.parse(uriStr)
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            viewModel.setOriginalImage(bitmap)
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                    val layers by viewModel.layers.collectAsState()
                    val selectedLayerId by viewModel.selectedLayerId.collectAsState()
                    val isProcessing by viewModel.isProcessing.collectAsState()

                    Column(modifier = Modifier.fillMaxSize()) {
                        
                        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            // Top: Canvas Compositor
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                LayerCompositor(
                                    layers = layers,
                                    selectedLayerId = selectedLayerId,
                                    onTransform = { id, pan, zoom, rot ->
                                        viewModel.updateLayerTransform(id, pan, zoom, rot)
                                    }
                                )
                                
                                if (isProcessing) {
                                    CircularProgressIndicator(color = Color(0xFFF5A623))
                                }
                            }
                            
                            // Bottom: Layer Stack Panel (Horizontal)
                            LayerStackPanel(
                                layers = layers,
                                selectedLayerId = selectedLayerId,
                                onSelectLayer = { viewModel.selectLayer(it) },
                                onToggleVisibility = { viewModel.toggleLayerVisibility(it) }
                            )
                        }

                        // Bottom Controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.segmentSubject() },
                                enabled = !isProcessing && layers.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                            ) {
                                Text("Magic Cutout")
                            }
                            
                            Button(
                                onClick = { finish() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5A623))
                            ) {
                                Text("Export", color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
