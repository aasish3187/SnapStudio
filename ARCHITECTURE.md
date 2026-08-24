# SnapStudio — Technical Architecture

Native throughout. No cross-platform UI framework (no React Native/Flutter). iOS and Android each get a fully native app; a shared C++ core carries the platform-agnostic business logic so filter and edit behavior doesn't drift between the two.

## 1. Tech stack summary

| Layer | iOS | Android | Shared |
|---|---|---|---|
| UI | Swift, SwiftUI | Kotlin, Jetpack Compose | — |
| Camera capture | AVFoundation (`AVCaptureSession`) | CameraX (Camera2 under the hood) | — |
| GPU rendering | Metal + MSL shaders | OpenGL ES 3.0 + GLSL shaders | — |
| Edit state, filter graph, project model, LUT parsing, math | — | — | C++ (CMake) |
| Bridge | Swift/C++ direct interop | JNI (Kotlin `external fun`) | — |
| Persistence | Core Data or flat files + C++ serializer | Room or flat files + C++ serializer | JSON schema defined once in C++ |

## 2. What's actually shared in C++ — and what isn't

This is the part worth reading carefully, because "shared C++ filters" is easy to over-promise. **GLSL and MSL are different languages — you cannot literally share compiled shader code between Metal and OpenGL ES.** What you *can* and should share is everything around the shaders:

**Lives in the C++ core:**
- `EditState` — the ordered, serializable stack of operations applied to a photo (crop rect, filter id + params, adjustment values, text/sticker layer data)
- `FilterGraph` / `FilterNode` — the abstract definition of what filters exist and what parameters they take (not their shader implementation)
- LUT file parsing (`.cube` → raw lookup data, which both platforms feed into their own 3D texture)
- Undo/redo command stack
- Project (photo + edit state) serialization to JSON
- Shared color-space math / matrix utilities

**Stays platform-native** (hand-maintained in parallel, driven by the same C++ parameter contracts):
- The actual shader source: `.metal` files on iOS, `.glsl`/`.frag`/`.vert` files on Android
- The camera capture session and its delegate/callback plumbing
- The GPU context and per-frame render loop
- All UI code

**Why:** per-frame camera and GPU work should never cross the JNI or Swift/C++ boundary. That boundary has real overhead, and paying it 30–60 times a second per frame is how "shared code" turns into a framerate problem. The C++ core gets called when *user intent* changes — a slider moved, a filter got picked, an undo happened — not every frame.

If true single-source shaders matter to you later, look at a shader cross-compilation path (writing in a common shading language and transpiling to MSL/GLSL via something like SPIRV-Cross). Worth knowing about, not worth the toolchain complexity for v1.

## 3. Camera → GPU pipeline

**iOS:**
1. `AVCaptureSession` + `AVCaptureVideoDataOutput` delivers `CMSampleBuffer` / `CVPixelBuffer` frames
2. `CVMetalTextureCache` wraps the pixel buffer as an `MTLTexture` — zero-copy
3. A chain of offscreen render passes (one per active effect: LUT → adjustments → vignette → …) composites into the final preview texture
4. `MTKView` presents the result
5. Capture: run the same pipeline once at full sensor resolution against the `AVCapturePhotoOutput` frame

**Android:**
1. CameraX `Preview` / `ImageAnalysis` use case streams frames into a `SurfaceTexture`
2. The texture is bound as `GL_TEXTURE_EXTERNAL_OES` inside a custom `GLSurfaceView.Renderer`
3. Fragment shaders sample it via `samplerExternalOES` (needs `#extension GL_OES_EGL_image_external : require`)
4. Same multi-pass compositing idea: a chain of FBOs, one per active effect
5. Capture: run the same GL pipeline against the `ImageCapture` use case's full-resolution output, so the saved photo matches what was previewed

Multi-pass compositing (rather than one monolithic shader per filter) is what lets Studio stack a filter *and* adjustments *and* a vignette without a combinatorial explosion of shader variants.

> Aside: OpenGL ES 3.0 is mature, well-documented, and directly matches what you asked for. Worth knowing Google has been steering new graphics-heavy work toward Vulkan — if you ever want that path, it would replace this GL layer without touching the C++ core or app architecture. Not a v1 concern.

## 4. Filter catalog (starter set — rename freely)

- **LUT / color-grade:** Mono, Fade, Vivid Pop, Golden Hour, Cool Blue, Vintage Film, Noir
- **Effect:** Soft Blur, Grain, Duotone, Light Leak

3D LUT textures (32³ or 64³) are the standard technique here — sampled directly in the fragment shader, one texture per preset, trivial to add more later without new shader code.

## 5. Studio edit pipeline

Non-destructive stack, in order: **Crop/Rotate → Filter → Adjustments → Overlay layers (text/stickers/drawing) → Export flatten.**

`EditState` (C++) is the single source of truth. Both platforms' renderers read the same `EditState` to reproduce an identical pipeline — this is what makes "reopen a photo and keep editing" work, and what keeps the live camera preview and the Studio preview visually consistent when the same filter is used in both places.

## 6. State management

- **iOS:** MVVM, `@Observable` / Combine ViewModels wrapping the C++ core via Swift/C++ interop
- **Android:** MVVM, `StateFlow` ViewModels calling the C++ core via JNI
- Business logic — what "Vivid Pop" does numerically, which crop ratios are valid, undo/redo rules — lives once, in C++. Both platforms just react to state and issue render calls.

## 7. Repo layout

```
snapstudio/
├── core/                        # shared C++ engine
│   ├── CMakeLists.txt
│   ├── include/
│   └── src/{pipeline,io,math}/
├── ios/
│   └── SnapStudio/{App,Camera,Rendering,Studio,Gallery,Export,SharedCore}/
│       └── Rendering/Shaders/*.metal
├── android/
│   └── app/src/main/
│       ├── java/com/snapstudio/app/{camera,rendering,studio,gallery,export,core}/
│       ├── cpp/                 # JNI glue + CMakeLists linking /core
│       └── assets/shaders/*.glsl
└── docs/                        # this doc set
```

## 8. Permissions

- Camera: `NSCameraUsageDescription` (iOS) / `CAMERA` (Android manifest)
- Save to library: `NSPhotoLibraryAddUsageDescription` / `READ_MEDIA_IMAGES` (Android 13+) or scoped storage write
- Microphone, only if video capture ships: `NSMicrophoneUsageDescription` / `RECORD_AUDIO`

## 9. Design tokens (fill in once Figma work starts)

```
// color, type, and spacing tokens go here once FIGMA_DESIGN_BRIEF.md work
// produces real values — keep this in sync so Antigravity implements
// against actual numbers, not placeholders
```
