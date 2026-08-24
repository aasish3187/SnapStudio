# SnapStudio — Implementation Plan

Phased so each phase is a bounded, independently verifiable unit of work. **Feed Antigravity one phase at a time** (plus `ARCHITECTURE.md` as standing context) rather than the whole plan at once — each phase ends with something you can actually run and check, which is what lets an agent verify its own work instead of drifting.

Reference `ARCHITECTURE.md` for module names, folder layout, and the "what's shared vs. what's native" split — don't let an agent route per-frame GPU work through the C++ bridge.

## Phase 0 — Scaffolding

- [ ] Monorepo structure per `ARCHITECTURE.md` §7
- [ ] `core/`: CMake project building a static lib, exposing one trivial function
- [ ] `ios/`: Xcode project, SwiftUI app shell, links `core` via Swift/C++ interop, calls the trivial function and prints the result
- [ ] `android/`: Gradle project (Kotlin DSL), Compose app shell, NDK/CMake linking `core` via JNI, calls the trivial function and prints the result
- [ ] **Verify:** both apps launch and successfully call into the shared C++ core

## Phase 1 — Camera capture (no filters yet)

- [ ] iOS: `AVCaptureSession` setup, permission request flow, live passthrough preview
- [ ] Android: CameraX setup, permission request flow, live passthrough preview
- [ ] Both: capture a still photo, save to app temp storage
- [ ] **Verify:** camera preview runs at native resolution on both platforms; a captured photo round-trips to disk correctly

## Phase 2 — GPU filter engine

- [ ] C++: implement `FilterGraph`, `FilterNode`, `EditState` per `ARCHITECTURE.md` §2
- [ ] iOS: Metal multi-pass renderer; implement 3 starter filters as `.metal` shaders + LUT loading
- [ ] Android: OpenGL ES renderer (external OES texture path); same 3 filters as GLSL
- [ ] Both: filter tray UI, live swap between filters on the camera preview
- [ ] **Verify:** switching filters updates the live preview with no dropped frames visible; the same filter looks visually equivalent on both platforms

## Phase 3 — Studio editor

- [ ] `EditState` persistence: serialize/deserialize via the C++ core
- [ ] Crop / rotate / straighten tool
- [ ] Adjustment sliders (brightness / contrast / saturation / exposure / temp / highlights / shadows / vignette) wired to shader uniforms
- [ ] Text overlay tool; sticker + freehand drawing tool
- [ ] Re-open a saved edit and confirm the exact same pipeline restores
- [ ] **Verify:** every adjustment is undoable/redoable; closing and reopening a photo preserves the full edit stack

## Phase 4 — Gallery, export, settings

- [ ] Local gallery/library grid (captured + edited photos)
- [ ] Export: flatten `EditState` onto the source image at full resolution, save to system photo library
- [ ] Native OS share sheet integration (`UIActivityViewController` / `Intent.ACTION_SEND`) — **not** a custom send flow
- [ ] Settings: capture quality, default filter, storage/cache management
- [ ] **Verify:** exported image visually matches the Studio preview (pixel-for-pixel or as close as format compression allows); share sheet opens the OS-native picker

## Phase 5 — Polish & performance

- [ ] Frame-rate profiling under sustained filter preview; thermal throttling behavior
- [ ] Memory profiling for large images (watch for texture leaks across filter switches)
- [ ] Accessibility pass: VoiceOver / TalkBack labels on all camera and Studio controls
- [ ] App icon, launch screen, store listing assets

## Later / not in this plan

- AR face-tracking lenses (ARKit / ML Kit Face Detection) — architecturally deferred, see `PRD.md` §9
- Video capture and video-specific Studio tools
- Cloud sync / accounts
