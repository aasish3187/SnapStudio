#pragma once
#include "FilterNode.hpp"
#include <vector>
#include <string>

namespace snapstudio {

class EditState {
public:
    EditState();
    ~EditState() = default;

    void setActiveFilter(FilterType type);
    FilterType getActiveFilter() const;
    
    // Adjustments
    void setBrightness(float b);
    void setContrast(float c);
    void setSaturation(float s);
    
    // Transforms
    void setCropRect(float x, float y, float w, float h);
    void setRotation(float angle);
    
    // Overlays
    void setText(const std::string& text, float x, float y);
    
    FilterNode getCurrentState() const;

    // Undo / Redo
    void pushState();
    bool canUndo() const;
    bool canRedo() const;
    void undo();
    void redo();

    // Persistence
    std::string serializeToJson() const;
    void deserializeFromJson(const std::string& json);

private:
    FilterNode currentFilter;
    std::vector<FilterNode> undoStack;
    std::vector<FilterNode> redoStack;
};

} // namespace snapstudio
