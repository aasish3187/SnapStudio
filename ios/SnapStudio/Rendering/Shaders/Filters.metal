#include <metal_stdlib>
using namespace metal;

struct VertexOut {
    float4 position [[position]];
    float2 texCoord;
};

// Passthrough vertex
vertex VertexOut vertexShader(uint vertexID [[vertex_id]]) {
    float2 positions[4] = { float2(-1, -1), float2(1, -1), float2(-1, 1), float2(1, 1) };
    float2 texCoords[4] = { float2(0, 1), float2(1, 1), float2(0, 0), float2(1, 0) };
    
    VertexOut out;
    out.position = float4(positions[vertexID], 0.0, 1.0);
    out.texCoord = texCoords[vertexID];
    return out;
}

// Basic Passthrough Fragment
fragment float4 fragmentShader(VertexOut in [[stage_in]],
                               texture2d<float> colorTexture [[texture(0)]]) {
    constexpr sampler textureSampler (mag_filter::linear, min_filter::linear);
    return colorTexture.sample(textureSampler, in.texCoord);
}

// Grayscale
fragment float4 grayscaleShader(VertexOut in [[stage_in]],
                                texture2d<float> colorTexture [[texture(0)]]) {
    constexpr sampler textureSampler (mag_filter::linear, min_filter::linear);
    float4 color = colorTexture.sample(textureSampler, in.texCoord);
    float gray = dot(color.rgb, float3(0.299, 0.587, 0.114));
    return float4(gray, gray, gray, color.a);
}

// Sepia
fragment float4 sepiaShader(VertexOut in [[stage_in]],
                            texture2d<float> colorTexture [[texture(0)]]) {
    constexpr sampler textureSampler (mag_filter::linear, min_filter::linear);
    float4 c = colorTexture.sample(textureSampler, in.texCoord);
    float r = dot(c.rgb, float3(0.393, 0.769, 0.189));
    float g = dot(c.rgb, float3(0.349, 0.686, 0.168));
    float b = dot(c.rgb, float3(0.272, 0.534, 0.131));
    return float4(min(r, 1.0), min(g, 1.0), min(b, 1.0), c.a);
}
