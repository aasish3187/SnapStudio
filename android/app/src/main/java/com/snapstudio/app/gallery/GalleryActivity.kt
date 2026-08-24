package com.snapstudio.app.gallery

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.snapstudio.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GalleryMediaItem(
    val uri: Uri,
    val isVideo: Boolean,
    val dateAdded: Long
)

class GalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Ink900) {
                    GalleryScreen(onBack = { finish() })
                }
            }
        }
    }
}

@Composable
fun GalleryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var mediaList by remember { mutableStateOf<List<GalleryMediaItem>>(emptyList()) }
    var selectedMediaItem by remember { mutableStateOf<GalleryMediaItem?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val list = mutableListOf<GalleryMediaItem>()

            // 1. Query Images
            try {
                val imgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val imgProjection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED)
                val imgSelection = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
                val imgArgs = arrayOf("SnapStudio-%")
                context.contentResolver.query(imgUri, imgProjection, imgSelection, imgArgs, null)?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        val date = cursor.getLong(dateCol)
                        list.add(GalleryMediaItem(ContentUris.withAppendedId(imgUri, id), false, date))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Query Videos
            try {
                val vidUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                val vidProjection = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATE_ADDED)
                val vidSelection = "${MediaStore.Video.Media.DISPLAY_NAME} LIKE ?"
                val vidArgs = arrayOf("SnapStudio-%")
                context.contentResolver.query(vidUri, vidProjection, vidSelection, vidArgs, null)?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                    val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        val date = cursor.getLong(dateCol)
                        list.add(GalleryMediaItem(ContentUris.withAppendedId(vidUri, id), true, date))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            mediaList = list.sortedByDescending { it.dateAdded }
        }
    }

    // Intercept hardware / gesture back button when viewing full media
    BackHandler(enabled = selectedMediaItem != null) {
        selectedMediaItem = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedMediaItem != null) {
            // Full-screen Clean Preview Viewer
            MediaDetailViewer(
                item = selectedMediaItem!!,
                onBack = { selectedMediaItem = null },
                onEdit = {
                    val intent = Intent(context, com.snapstudio.app.studio.StudioActivity::class.java)
                    if (selectedMediaItem!!.isVideo) {
                        intent.putExtra("video_uri", selectedMediaItem!!.uri.toString())
                    } else {
                        intent.putExtra("IMAGE_URI", selectedMediaItem!!.uri.toString())
                    }
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(intent)
                },
                onShare = {
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = if (selectedMediaItem!!.isVideo) "video/*" else "image/*"
                            putExtra(Intent.EXTRA_STREAM, selectedMediaItem!!.uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
        } else {
            // Grid View
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Ink750)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.ChevronLeft, contentDescription = "Back", tint = Fg)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Photos & Videos", color = Fg, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("${mediaList.size} items", color = FgFaint, fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Amber)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = "Camera", tint = Ink900, modifier = Modifier.size(20.dp))
                    }
                }

                // Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(mediaList) { item ->
                        var bitmap by remember(item.uri) { mutableStateOf<android.graphics.Bitmap?>(null) }
                        LaunchedEffect(item.uri) {
                            withContext(Dispatchers.IO) {
                                try {
                                    if (item.isVideo && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        bitmap = context.contentResolver.loadThumbnail(item.uri, Size(300, 400), null)
                                    } else {
                                        val stream = context.contentResolver.openInputStream(item.uri)
                                        bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                                        stream?.close()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(3f / 4f)
                                .background(Ink800)
                                .clickable {
                                    selectedMediaItem = item
                                }
                        ) {
                            bitmap?.let { b ->
                                Image(
                                    bitmap = b.asImageBitmap(),
                                    contentDescription = if (item.isVideo) "Video" else "Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            if (item.isVideo) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.PlayArrow,
                                        contentDescription = "Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaDetailViewer(
    item: GalleryMediaItem,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember(item.uri) { mutableStateOf<android.graphics.Bitmap?>(null) }

    val exoPlayer = remember(item.uri) {
        if (item.isVideo) {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(item.uri)
                setMediaItem(mediaItem)
                repeatMode = Player.REPEAT_MODE_ALL
                prepare()
                playWhenReady = true
            }
        } else {
            null
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.release()
        }
    }

    LaunchedEffect(item.uri) {
        if (!item.isVideo) {
            withContext(Dispatchers.IO) {
                try {
                    val stream = context.contentResolver.openInputStream(item.uri)
                    bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                    stream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (item.isVideo && exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (bitmap != null) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Full Photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offset += pan
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    }
            )
        } else {
            CircularProgressIndicator(
                color = Amber,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Bottom Action Bar with "Edit" and "Share"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Share Button
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ink800.copy(alpha = 0.85f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Edit Button
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber,
                    contentColor = Ink900
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    Icons.Outlined.AutoFixHigh,
                    contentDescription = "Edit",
                    tint = Ink900,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (item.isVideo) "Edit Video" else "Edit Photo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink900
                )
            }
        }
    }
}


