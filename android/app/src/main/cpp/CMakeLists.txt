cmake_minimum_required(VERSION 3.22.1)

project("snapstudio")

# Include the shared core
add_subdirectory(../../../../../core ${CMAKE_CURRENT_BINARY_DIR}/core)

add_library(
        snapstudio
        SHARED
        native-lib.cpp)

target_link_libraries(
        snapstudio
        SnapCore
        log)
