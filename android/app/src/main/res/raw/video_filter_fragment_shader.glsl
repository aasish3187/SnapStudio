#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES tex_sampler;
uniform mat4 color_matrix;
uniform vec4 color_offset;

// vTexCoords is passed from the Media3 default vertex shader
varying vec2 vTexCoords;

void main() {
    vec4 color = texture2D(tex_sampler, vTexCoords);
    
    // android.graphics.ColorMatrix is a 4x5 matrix.
    // The first 4 columns map to color_matrix (scale/rotation), and the last column maps to color_offset.
    vec4 new_color = (color_matrix * color) + color_offset;
    
    gl_FragColor = vec4(new_color.rgb, color.a);
}
