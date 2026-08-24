package com.snapstudio.app.editing

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhotoEditViewModel(
    private val onDeviceRepo: AIEditingRepository = OnDeviceEditingRepositoryImpl(),
    private val cloudRepo: AIEditingRepository = CloudEditingRepositoryImpl("YOUR_API_KEY")
) : ViewModel() {

    private val _originalBitmap = MutableStateFlow<Bitmap?>(null)
    val originalBitmap: StateFlow<Bitmap?> = _originalBitmap.asStateFlow()

    private val _currentBitmap = MutableStateFlow<Bitmap?>(null)
    val currentBitmap: StateFlow<Bitmap?> = _currentBitmap.asStateFlow()
    
    private val _layers = MutableStateFlow<List<Layer>>(emptyList())
    val layers: StateFlow<List<Layer>> = _layers.asStateFlow()
    
    private val _selectedLayerId = MutableStateFlow<String?>(null)
    val selectedLayerId: StateFlow<String?> = _selectedLayerId.asStateFlow()
    
    private val _segmentedMask = MutableStateFlow<Bitmap?>(null)
    val segmentedMask: StateFlow<Bitmap?> = _segmentedMask.asStateFlow()

    private val _editStack = MutableStateFlow<List<EditOperation>>(emptyList())
    val editStack: StateFlow<List<EditOperation>> = _editStack.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun setOriginalImage(bitmap: Bitmap) {
        _originalBitmap.value = bitmap
        _currentBitmap.value = bitmap
        
        // Initialize with a single background layer
        val bgLayer = Layer(
            bitmap = bitmap,
            name = "Background",
            zIndex = 0
        )
        _layers.value = listOf(bgLayer)
        _selectedLayerId.value = bgLayer.id
        
        _editStack.value = emptyList()
    }

    fun segmentSubject() {
        val original = _originalBitmap.value ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val mask = onDeviceRepo.segmentSubject(original)
                _segmentedMask.value = mask
                
                // Extract foreground using PorterDuff SRC_IN
                val extractedSubject = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(extractedSubject)
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                
                canvas.drawBitmap(mask, 0f, 0f, null)
                paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
                canvas.drawBitmap(original, 0f, 0f, paint)
                
                val newLayer = Layer(
                    bitmap = extractedSubject,
                    mask = mask,
                    name = "Cutout",
                    zIndex = _layers.value.size
                )
                
                _layers.value = _layers.value + newLayer
                _selectedLayerId.value = newLayer.id
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun applyBackgroundBlur(blurRadius: Float) {
        val original = _originalBitmap.value ?: return
        val mask = _segmentedMask.value ?: return
        
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                // Basic fast blur trick via downscaling (fallback for all APIs)
                val scaleRatio = Math.max(1, (100 / blurRadius).toInt())
                val downscaled = Bitmap.createScaledBitmap(original, original.width / scaleRatio, original.height / scaleRatio, true)
                val blurredBg = Bitmap.createScaledBitmap(downscaled, original.width, original.height, true)
                downscaled.recycle()

                val resultBitmap = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(resultBitmap)
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                
                // 1. Draw blurred background
                canvas.drawBitmap(blurredBg, 0f, 0f, paint)
                blurredBg.recycle()
                
                // 2. Extract foreground subject
                val tempForeground = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
                val maskCanvas = android.graphics.Canvas(tempForeground)
                // Draw mask (which is white foreground, transparent background)
                maskCanvas.drawBitmap(mask, 0f, 0f, null)
                // SRC_IN draws the original pixels only where the mask pixels exist
                paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
                maskCanvas.drawBitmap(original, 0f, 0f, paint)
                
                // 3. Composite subject over blurred background
                paint.xfermode = null
                canvas.drawBitmap(tempForeground, 0f, 0f, paint)
                tempForeground.recycle()

                _currentBitmap.value = resultBitmap
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    fun selectLayer(id: String) {
        _selectedLayerId.value = id
    }
    
    fun toggleLayerVisibility(id: String) {
        _layers.value = _layers.value.map {
            if (it.id == id) it.copy(isVisible = !it.isVisible) else it
        }
    }
    
    fun updateLayerTransform(id: String, offset: androidx.compose.ui.geometry.Offset, scale: Float, rotation: Float) {
        _layers.value = _layers.value.map {
            if (it.id == id) {
                it.copy(
                    offset = it.offset + offset,
                    scale = it.scale * scale,
                    rotation = it.rotation + rotation
                )
            } else it
        }
    }
}
