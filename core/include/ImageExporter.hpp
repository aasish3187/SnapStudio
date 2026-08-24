#pragma once

#include <string>
#include "EditState.hpp"

namespace snapstudio {

class ImageExporter {
public:
    // Platform-specific implementations will define this.
    // Stub definition for cross-platform interface.
    static std::string flattenImage(const std::string& inputPath, const EditState& state);
};

} // namespace snapstudio
