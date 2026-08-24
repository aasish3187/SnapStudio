package com.snapstudio.app.filters

import android.content.Context
import android.opengl.GLES20
import androidx.media3.common.VideoFrameProcessingException
import androidx.media3.common.util.GlProgram
import androidx.media3.common.util.GlUtil
import androidx.media3.effect.BaseGlShaderProgram
import androidx.media3.effect.GlEffect
import androidx.media3.effect.GlShaderProgram
import com.snapstudio.app.R
import androidx.media3.common.util.Size

class VideoFilterEffect(private val colorMatrix: FloatArray) : GlEffect {
    override fun toGlShaderProgram(context: Context, useHdr: Boolean): GlShaderProgram {
        return VideoFilterShaderProgram(context, colorMatrix, useHdr)
    }
}

class VideoFilterShaderProgram(
    context: Context,
    private val colorMatrix: FloatArray,
    useHdr: Boolean
) : BaseGlShaderProgram(useHdr, 1) {

    private val glProgram: GlProgram

    init {
        try {
            val vertexShader = """
                attribute vec4 aFramePosition;
                attribute vec4 aTexCoords;
                varying vec2 vTexCoords;
                void main() {
                  gl_Position = aFramePosition;
                  vTexCoords = aTexCoords.xy;
                }
            """.trimIndent()
            
            val fragmentShader = context.resources.openRawResource(R.raw.video_filter_fragment_shader)
                .bufferedReader().use { it.readText() }

            glProgram = GlProgram(context, vertexShader, fragmentShader)
            
            glProgram.setBufferAttribute(
                "aFramePosition",
                GlUtil.getNormalizedCoordinateBounds(),
                GlUtil.HOMOGENEOUS_COORDINATE_VECTOR_SIZE
            )
            glProgram.setBufferAttribute(
                "aTexCoords",
                GlUtil.getTextureCoordinateBounds(),
                GlUtil.HOMOGENEOUS_COORDINATE_VECTOR_SIZE
            )
        } catch (e: Exception) {
            throw VideoFrameProcessingException(e)
        }
    }

    override fun drawFrame(texId: Int, presentationTimeUs: Long) {
        try {
            glProgram.use()
            glProgram.setSamplerTexIdUniform("tex_sampler", texId, 0)

            val mat4 = FloatArray(16)
            val offset = FloatArray(4)

            // GLSL mat4 is column-major. ColorMatrix is row-major.
            mat4[0] = colorMatrix[0]
            mat4[1] = colorMatrix[5]
            mat4[2] = colorMatrix[10]
            mat4[3] = colorMatrix[15]
            
            mat4[4] = colorMatrix[1]
            mat4[5] = colorMatrix[6]
            mat4[6] = colorMatrix[11]
            mat4[7] = colorMatrix[16]
            
            mat4[8] = colorMatrix[2]
            mat4[9] = colorMatrix[7]
            mat4[10] = colorMatrix[12]
            mat4[11] = colorMatrix[17]
            
            mat4[12] = colorMatrix[3]
            mat4[13] = colorMatrix[8]
            mat4[14] = colorMatrix[13]
            mat4[15] = colorMatrix[18]

            offset[0] = colorMatrix[4] / 255f
            offset[1] = colorMatrix[9] / 255f
            offset[2] = colorMatrix[14] / 255f
            offset[3] = colorMatrix[19] / 255f

            glProgram.setFloatsUniform("color_matrix", mat4)
            glProgram.setFloatsUniform("color_offset", offset)

            glProgram.bindAttributesAndUniforms()
            
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        } catch (e: GlUtil.GlException) {
            throw VideoFrameProcessingException(e)
        }
    }
    
    override fun configure(inputWidth: Int, inputHeight: Int): Size {
        return Size(inputWidth, inputHeight)
    }
}
