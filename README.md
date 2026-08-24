# SnapStudio - AI Photo & Video Studio

SnapStudio is a modern mobile photo and video camera and creative studio application built with Kotlin, Jetpack Compose, CameraX, and real-time GPU-accelerated processing pipelines.

## Features

- 📸 **CameraX Pro Camera Pipeline**
  - Tap-to-focus and auto-exposure metering with animated visual feedback.
  - Smooth pinch-to-zoom (1.0x to 10.0x) with dynamic floating indicator.
  - Double-tap gesture to flip between front and rear cameras with haptic feedback.
  - Real-time live camera filter previews powered by hardware-accelerated `ColorMatrix` transformations.

- 🎨 **Creative Editing Studio**
  - Full-featured photo & video adjustment studio (Tone, Curves, Ambiance, Details, Tonal Contrast, White Balance, Vignette, Dehaze, Grain, Light Leaks, Frames).
  - Arc Dome filter wheel with real-time intensity adjustments.
  - Multi-layer overlay engine supporting text stickers, graphic badges, and custom image stickers with transform gestures (drag, scale, rotate).
  - Pinch-to-zoom and canvas panning for precise editing.
  - Complete multi-step Undo/Redo history stack.

- 🤖 **AI Enhancements & Segmentation**
  - On-device subject segmentation using Google ML Kit Selfie Segmentation.
  - Architecture ready for AI generative expand, super-resolution (ESRGAN), and object removal.

- 📱 **Gallery & Media Player**
  - Clean preview viewer with pinch-to-zoom and high-res media inspection.
  - ExoPlayer-powered video playback with seamless filter rendering.
  - Built-in video trimming and export engine.

## Architecture & Tech Stack

- **Android App:** Kotlin, Jetpack Compose, CameraX, Media3 / ExoPlayer, ML Kit
- **Core Native Layer:** C++20 image processing pipeline & Metal/OpenGL shaders
- **UI Design System:** Glassmorphism, tailored HSL color tokens, dark mode palette

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/aasish3187/<repository-name>.git
   ```
2. Open the `android` folder in **Android Studio**.
3. Sync Gradle and run on an Android device running Android 8.0 (API 26) or higher.
