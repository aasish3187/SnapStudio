#include "EditState.hpp"

namespace snapstudio {

EditState::EditState() {
    currentFilter = FilterNode();
    pushState(); // Save initial state
}

void EditState::pushState() {
    undoStack.push_back(currentFilter);
    redoStack.clear();
}

void EditState::setActiveFilter(FilterType type) {
    currentFilter.type = type;
    pushState();
}

FilterType EditState::getActiveFilter() const {
    return currentFilter.type;
}

void EditState::setBrightness(float b) {
    currentFilter.brightness = b;
    pushState();
}

void EditState::setContrast(float c) {
    currentFilter.contrast = c;
    pushState();
}

void EditState::setSaturation(float s) {
    currentFilter.saturation = s;
    pushState();
}

void EditState::setCropRect(float x, float y, float w, float h) {
    currentFilter.cropX = x;
    currentFilter.cropY = y;
    currentFilter.cropW = w;
    currentFilter.cropH = h;
    pushState();
}

void EditState::setRotation(float angle) {
    currentFilter.rotationAngle = angle;
    pushState();
}

void EditState::setText(const std::string& text, float x, float y) {
    currentFilter.textContent = text;
    currentFilter.textX = x;
    currentFilter.textY = y;
    pushState();
}

FilterNode EditState::getCurrentState() const {
    return currentFilter;
}

bool EditState::canUndo() const {
    return undoStack.size() > 1;
}

bool EditState::canRedo() const {
    return !redoStack.empty();
}

void EditState::undo() {
    if (canUndo()) {
        redoStack.push_back(currentFilter);
        undoStack.pop_back();
        currentFilter = undoStack.back();
    }
}

void EditState::redo() {
    if (canRedo()) {
        currentFilter = redoStack.back();
        undoStack.push_back(currentFilter);
        redoStack.pop_back();
    }
}

std::string EditState::serializeToJson() const {
    // Basic stub for serialization
    return "{ \"type\": " + std::to_string(static_cast<int>(currentFilter.type)) + " }";
}

void EditState::deserializeFromJson(const std::string& json) {
    // Basic stub for deserialization
}

} // namespace snapstudio
