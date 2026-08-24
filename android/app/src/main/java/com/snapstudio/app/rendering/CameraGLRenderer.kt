package com.snapstudio.app.rendering

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraGLRenderer(
    private val context: Context,
    private val onSurfaceTextureAvailable: (CameraGLRenderer, SurfaceTexture) -> Unit
) : GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    companion object {
        private const val TAG = "CameraGLRenderer"
    }

    private var textureId: Int = 0
    private var surfaceTexture: SurfaceTexture? = null
    private var updateTexture = false
    private val stTransformMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) } // Raw SurfaceTexture transform

    private var programHandle: Int = 0
    private var positionHandle: Int = 0
    private var texCoordHandle: Int = 0
    private var texMatrixHandle: Int = 0
    private var projMatrixHandle: Int = 0
    private val projMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }

    @Volatile private var viewWidth = 0
    @Volatile private var viewHeight = 0
    @Volatile private var cameraWidth = 0
    @Volatile private var cameraHeight = 0
    @Volatile private var cameraRotation = 90 // Default for most phones

    @Volatile private var needsMatrixUpdate = true

    fun setCameraResolution(w: Int, h: Int) {
        Log.d(TAG, "setCameraResolution: ${w}x${h}")
        cameraWidth = w
        cameraHeight = h
        needsMatrixUpdate = true
    }

    fun setCameraRotation(rotation: Int) {
        Log.d(TAG, "setCameraRotation: $rotation")
        cameraRotation = rotation
        needsMatrixUpdate = true
    }

    private val texRotationMatrix = FloatArray(16)
    private val combinedTexMatrix = FloatArray(16)

    /**
     * Builds a projection matrix that:
     * Scales the quad so the camera image fills the view (center-crop)
     * (Rotation is handled in texture coordinate space).
     */
    private fun updateProjMatrix() {
        if (viewWidth == 0 || viewHeight == 0 || cameraWidth == 0 || cameraHeight == 0) return

        // After rotating the camera buffer by cameraRotation, these are the
        // effective pixel dimensions as they appear on screen.
        val isSensorRotated = cameraRotation % 180 != 0
        val effCamW = if (isSensorRotated) cameraHeight else cameraWidth
        val effCamH = if (isSensorRotated) cameraWidth  else cameraHeight

        val viewAspect   = viewWidth.toFloat()  / viewHeight.toFloat()
        val cameraAspect = effCamW.toFloat() / effCamH.toFloat()

        Log.d(TAG, "updateProjMatrix: view=${viewWidth}x${viewHeight} cam=${cameraWidth}x${cameraHeight} rot=$cameraRotation effCam=${effCamW}x${effCamH} viewAspect=$viewAspect camAspect=$cameraAspect")

        Matrix.setIdentityM(projMatrix, 0)

        // Center-crop: scale whichever axis is "too wide" so the image fills the viewport
        if (viewAspect < cameraAspect) {
            // Camera is wider than view → scale X up to crop the sides
            val scale = cameraAspect / viewAspect
            Matrix.scaleM(projMatrix, 0, scale, 1f, 1f)
        } else {
            // Camera is taller than view → scale Y up to crop top/bottom
            val scale = viewAspect / cameraAspect
            Matrix.scaleM(projMatrix, 0, 1f, scale, 1f)
        }
    }

    // ---- Shaders ----

    private val vertexShaderCode = """
        attribute vec4 aPosition;
        attribute vec4 aTexCoord;
        varying vec2 vTexCoord;
        uniform mat4 uTexMatrix;
        uniform mat4 uProjMatrix;
        void main() {
            gl_Position = uProjMatrix * aPosition;
            vTexCoord = (uTexMatrix * aTexCoord).xy;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTexCoord;
        uniform samplerExternalOES uTexture;
        
        uniform int uTier;
        uniform float uIntensity;

        // Tier 1
        uniform float uCM[20];
        
        // Tier 2
        uniform float uVignette;
        uniform float uGrain;
        uniform float uChromaticPop;
        
        // Tier 3
        uniform sampler2D uLutTexture;
        
        vec4 sampleLut(vec4 color) {
            float blueColor = color.b * 63.0;
            
            vec2 quad1;
            quad1.y = floor(floor(blueColor) / 8.0);
            quad1.x = floor(blueColor) - (quad1.y * 8.0);
            
            vec2 quad2;
            quad2.y = floor(ceil(blueColor) / 8.0);
            quad2.x = ceil(blueColor) - (quad2.y * 8.0);
            
            vec2 texPos1;
            texPos1.x = (quad1.x * 64.0 + 0.5 + 63.0 * color.r) / 512.0;
            texPos1.y = (quad1.y * 64.0 + 0.5 + 63.0 * color.g) / 512.0;
            
            vec2 texPos2;
            texPos2.x = (quad2.x * 64.0 + 0.5 + 63.0 * color.r) / 512.0;
            texPos2.y = (quad2.y * 64.0 + 0.5 + 63.0 * color.g) / 512.0;
            
            vec4 newColor1 = texture2D(uLutTexture, texPos1);
            vec4 newColor2 = texture2D(uLutTexture, texPos2);
            
            vec4 newColor = mix(newColor1, newColor2, fract(blueColor));
            return vec4(newColor.rgb, color.a);
        }
        
        float rand(vec2 co){
            return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
        }
        
        void main() {
            vec4 baseColor = texture2D(uTexture, vTexCoord);
            vec4 finalColor = baseColor;
            
            if (uTier == 1) {
                float r = baseColor.r * uCM[0] + baseColor.g * uCM[1] + baseColor.b * uCM[2] + baseColor.a * uCM[3] + uCM[4] / 255.0;
                float g = baseColor.r * uCM[5] + baseColor.g * uCM[6] + baseColor.b * uCM[7] + baseColor.a * uCM[8] + uCM[9] / 255.0;
                float b = baseColor.r * uCM[10] + baseColor.g * uCM[11] + baseColor.b * uCM[12] + baseColor.a * uCM[13] + uCM[14] / 255.0;
                float a = baseColor.r * uCM[15] + baseColor.g * uCM[16] + baseColor.b * uCM[17] + baseColor.a * uCM[18] + uCM[19] / 255.0;
                finalColor = vec4(r, g, b, a);
            } 
            else if (uTier == 2) {
                vec4 cColor = baseColor;
                if (uChromaticPop > 0.0) {
                    vec2 rOffset = vec2(uChromaticPop, 0.0);
                    vec2 bOffset = vec2(-uChromaticPop, 0.0);
                    float r = texture2D(uTexture, vTexCoord + rOffset).r;
                    float b = texture2D(uTexture, vTexCoord + bOffset).b;
                    cColor = vec4(r, baseColor.g, b, baseColor.a);
                }
                if (uVignette > 0.0) {
                    vec2 center = vec2(0.5, 0.5);
                    float dist = distance(vTexCoord, center);
                    cColor.rgb *= smoothstep(0.8, uVignette * 0.4, dist * (1.0 + uVignette));
                }
                if (uGrain > 0.0) {
                    float noise = (rand(vTexCoord * 100.0) - 0.5) * uGrain;
                    cColor.rgb += noise;
                }
                finalColor = cColor;
            }
            else if (uTier == 3) {
                finalColor = sampleLut(baseColor);
            }
            
            gl_FragColor = mix(baseColor, finalColor, uIntensity);
        }
    """.trimIndent()

    // ---- Geometry ----

    private val vertexCoords = floatArrayOf(
        -1.0f, -1.0f,   // bottom left
         1.0f, -1.0f,   // bottom right
        -1.0f,  1.0f,   // top left
         1.0f,  1.0f    // top right
    )

    private val texCoords = floatArrayOf(
        0.0f, 0.0f,     // bottom left
        1.0f, 0.0f,     // bottom right
        0.0f, 1.0f,     // top left
        1.0f, 1.0f      // top right
    )

    private val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertexCoords.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexCoords).apply { position(0) }

    private val texBuffer: FloatBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer().put(texCoords).apply { position(0) }

    // ---- Filter State ----

    var activePreset: com.snapstudio.app.filters.FilterPreset? = null
        set(value) {
            field = value
            if (value is com.snapstudio.app.filters.FilterPreset.LutEffect) {
                // Load LUT texture if needed. We'll handle this in the render loop or a dedicated thread.
                needsLutLoad = true
            }
        }
        
    var filterIntensity: Float = 1.0f

    private var lutTextureId: Int = 0
    @Volatile private var needsLutLoad = false

    // Uniform handles
    private var uTierHandle: Int = 0
    private var uIntensityHandle: Int = 0
    private var uCMHandle: Int = 0
    private var uTextureHandle: Int = 0
    private var uVignetteHandle: Int = 0
    private var uGrainHandle: Int = 0
    private var uChromaticPopHandle: Int = 0
    private var uLutTextureHandle: Int = 0

    // ---- GLSurfaceView.Renderer implementation ----

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        programHandle = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
            val linked = IntArray(1)
            GLES20.glGetProgramiv(it, GLES20.GL_LINK_STATUS, linked, 0)
            if (linked[0] == 0) {
                Log.e(TAG, "Program link FAILED: ${GLES20.glGetProgramInfoLog(it)}")
                GLES20.glDeleteProgram(it)
            } else {
                Log.d(TAG, "Shader program linked successfully")
            }
        }

        positionHandle = GLES20.glGetAttribLocation(programHandle, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(programHandle, "aTexCoord")
        texMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uTexMatrix")
        projMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uProjMatrix")
        
        uTierHandle = GLES20.glGetUniformLocation(programHandle, "uTier")
        uIntensityHandle = GLES20.glGetUniformLocation(programHandle, "uIntensity")
        uTextureHandle = GLES20.glGetUniformLocation(programHandle, "uTexture")
        uCMHandle = GLES20.glGetUniformLocation(programHandle, "uCM")
        uVignetteHandle = GLES20.glGetUniformLocation(programHandle, "uVignette")
        uGrainHandle = GLES20.glGetUniformLocation(programHandle, "uGrain")
        uChromaticPopHandle = GLES20.glGetUniformLocation(programHandle, "uChromaticPop")
        uLutTextureHandle = GLES20.glGetUniformLocation(programHandle, "uLutTexture")

        // 1. Create and setup OES camera texture FIRST
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        
        Log.d(TAG, "onSurfaceCreated: generated textureId=$textureId")
        
        // 2. Create and setup 2D texture for LUTs
        val lutTextures = IntArray(1)
        GLES20.glGenTextures(1, lutTextures, 0)
        lutTextureId = lutTextures[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, lutTextureId)
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())

        // 3. Re-bind OES texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)

        surfaceTexture = SurfaceTexture(textureId)
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        surfaceTexture?.setOnFrameAvailableListener(this, mainHandler)

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        onSurfaceTextureAvailable(this, surfaceTexture!!)
    }

    fun getSurfaceTexture(): SurfaceTexture? = surfaceTexture

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged: $width x $height")
        GLES20.glViewport(0, 0, width, height)
        viewWidth = width
        viewHeight = height
        needsMatrixUpdate = true
    }

    private fun updateTexCoords() {
        val coords = floatArrayOf(
            0.0f, 0.0f, // bottom left
            1.0f, 0.0f, // bottom right
            0.0f, 1.0f, // top left
            1.0f, 1.0f  // top right
        )
        
        texBuffer.clear()
        texBuffer.put(coords)
        texBuffer.position(0)
    }

    private var drawCount = 0
    override fun onDrawFrame(gl: GL10?) {
        drawCount++
        if (drawCount == 1 || drawCount % 60 == 0) {
            Log.d(TAG, "onDrawFrame: called $drawCount times")
        }

        if (needsMatrixUpdate) {
            updateProjMatrix()
            updateTexCoords()
            needsMatrixUpdate = false
        }

        synchronized(this) {
            if (updateTexture) {
                try {
                    surfaceTexture?.updateTexImage()
                    surfaceTexture?.getTransformMatrix(stTransformMatrix)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in updateTexImage", e)
                }
                updateTexture = false
            }
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glUseProgram(programHandle)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)

        // Vertex position
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)

        // Texture coordinates
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texBuffer)

        // Pass uniforms — pass SurfaceTexture matrix directly
        GLES20.glUniformMatrix4fv(texMatrixHandle, 1, false, stTransformMatrix, 0)
        GLES20.glUniformMatrix4fv(projMatrixHandle, 1, false, projMatrix, 0)
        GLES20.glUniform1i(uTextureHandle, 0)
        
        // Setup Filter Uniforms
        GLES20.glUniform1f(uIntensityHandle, filterIntensity)
        
        val preset = activePreset
        if (preset == null) {
            GLES20.glUniform1i(uTierHandle, 1)
            GLES20.glUniform1fv(uCMHandle, 20, com.snapstudio.app.filters.ColorMatrixUtils.NORMAL, 0)
        } else {
            when (preset) {
                is com.snapstudio.app.filters.FilterPreset.ColorMatrix -> {
                    GLES20.glUniform1i(uTierHandle, 1)
                    GLES20.glUniform1fv(uCMHandle, 20, preset.matrix, 0)
                }
                is com.snapstudio.app.filters.FilterPreset.ShaderEffect -> {
                    GLES20.glUniform1i(uTierHandle, 2)
                    GLES20.glUniform1f(uVignetteHandle, preset.vignette)
                    GLES20.glUniform1f(uGrainHandle, preset.grain)
                    GLES20.glUniform1f(uChromaticPopHandle, preset.chromaticPop)
                }
                is com.snapstudio.app.filters.FilterPreset.LutEffect -> {
                    GLES20.glUniform1i(uTierHandle, 3)
                    
                    if (needsLutLoad) {
                        needsLutLoad = false
                        loadLutTexture(preset.lutAssetName)
                    }
                    
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, lutTextureId)
                    GLES20.glUniform1i(uLutTextureHandle, 1)
                    
                    // Rebind camera texture to GL_TEXTURE0 just in case
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                    GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
                }
            }
        }

        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun loadLutTexture(assetName: String) {
        try {
            val stream = context.assets.open(assetName)
            val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
            stream.close()
            if (bitmap != null) {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, lutTextureId)
                android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
                bitmap.recycle()
                Log.d(TAG, "Loaded LUT: $assetName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load LUT: $assetName", e)
        }
    }

    var requestRender: (() -> Unit)? = null
    
    private var frameCount = 0
    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        synchronized(this) {
            updateTexture = true
            frameCount++
            if (frameCount == 1 || frameCount % 60 == 0) {
                Log.d(TAG, "onFrameAvailable: $frameCount frames received")
            }
        }
        requestRender?.invoke()
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                val typeName = if (type == GLES20.GL_VERTEX_SHADER) "VERTEX" else "FRAGMENT"
                Log.e(TAG, "$typeName shader compile FAILED: ${GLES20.glGetShaderInfoLog(shader)}")
                GLES20.glDeleteShader(shader)
            }
        }
    }
}
