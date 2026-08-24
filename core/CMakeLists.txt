cmake_minimum_required(VERSION 3.10)
project(SnapCore)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)

# Include directory
include_directories(include)

# Add shared library
add_library(SnapCore STATIC
    src/pipeline/SnapCore.cpp
    src/pipeline/EditState.cpp
)

# Target include directories
target_include_directories(SnapCore PUBLIC include)
