#pragma once
#include <string>

namespace snapstudio {

enum class FilterType {
    NONE = 0,
    GRAYSCALE = 1,
    SEPIA = 2
};

struct FilterNode {
    FilterType type;
    float intensity; // 0.0 to 1.0
    
    // Adjustments
    float brightness; // -1.0 to 1.0 (0.0 is default)
    float contrast;   // 0.0 to 2.0 (1.0 is default)
    float saturation; // 0.0 to 2.0 (1.0 is default)
    
    // Transforms
    float cropX, cropY, cropW, cropH; // 0.0 to 1.0 normalized
    float rotationAngle; // in degrees
    
    // Overlays
    std::string textContent;
    float textX, textY;

    FilterNode() : type(FilterType::NONE), intensity(1.0f), brightness(0.0f), contrast(1.0f), saturation(1.0f),
                   cropX(0.0f), cropY(0.0f), cropW(1.0f), cropH(1.0f), rotationAngle(0.0f),
                   textContent(""), textX(0.5f), textY(0.5f) {}
    
    FilterNode(FilterType t, float i) : type(t), intensity(i), brightness(0.0f), contrast(1.0f), saturation(1.0f),
                                        cropX(0.0f), cropY(0.0f), cropW(1.0f), cropH(1.0f), rotationAngle(0.0f),
                                        textContent(""), textX(0.5f), textY(0.5f) {}
};

} // namespace snapstudio
