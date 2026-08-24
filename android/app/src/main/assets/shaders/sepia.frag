#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;
in vec2 vTexCoord;
out vec4 outColor;
uniform samplerExternalOES sTexture;
void main() {
    vec4 c = texture(sTexture, vTexCoord);
    float r = dot(c.rgb, vec3(0.393, 0.769, 0.189));
    float g = dot(c.rgb, vec3(0.349, 0.686, 0.168));
    float b = dot(c.rgb, vec3(0.272, 0.534, 0.131));
    outColor = vec4(min(r, 1.0), min(g, 1.0), min(b, 1.0), c.a);
}
