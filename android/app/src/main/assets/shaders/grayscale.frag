#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;
in vec2 vTexCoord;
out vec4 outColor;
uniform samplerExternalOES sTexture;
void main() {
    vec4 c = texture(sTexture, vTexCoord);
    float gray = dot(c.rgb, vec3(0.299, 0.587, 0.114));
    outColor = vec4(gray, gray, gray, c.a);
}
