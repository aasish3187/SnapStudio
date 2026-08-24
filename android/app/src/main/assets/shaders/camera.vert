#version 300 es
in vec4 aPosition;
in vec4 aTexCoord;
out vec2 vTexCoord;
uniform mat4 uSTMatrix;
void main() {
    gl_Position = aPosition;
    vTexCoord = (uSTMatrix * aTexCoord).xy;
}
