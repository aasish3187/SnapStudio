# SnapStudio — Figma Design Brief

## Product context

Camera-first, not feed-first: the viewfinder is the home screen. Speed and legibility during shooting matter more than density. The Studio should feel like a focused, professional tool — closer to Lightroom Mobile or Darkroom than a social app. Nothing in the UI should look like it's waiting for a friend to reply: no avatars, no chat bubbles, no notification badges tied to other people.

## Design principles for this brief

- **Earn the chrome.** Every control visible over the live camera feed competes with the photo itself. Default to fewer, larger, higher-contrast controls over a dense toolbar.
- **One gesture per core action.** Swipe to change filter, tap to shoot, pinch to zoom — no menus for the things people do every session.
- **Distinct, not derivative.** Don't inherit Snapchat's specific yellow or icon language — see the naming note in `README.md`; this is both legal hygiene and a differentiation opportunity.
- **The photo is the content.** UI chrome should recede — translucent scrims, generous negative space, restrained color in the controls themselves so the captured image stays the visual focus.

## Screens to design

**1. Camera / Viewfinder (home).** Full-bleed live preview. Bottom-anchored filter tray — horizontally swipeable thumbnails, each showing a *live-filtered* mini-preview, not a static icon. Shutter button bottom-center. Flash toggle, camera flip, and a photo/video mode switch (if video ships) along the top edge with a translucent scrim behind them for legibility over any background. Bottom-left corner: a small gallery thumbnail shortcut into Library. Tap-to-focus reticle, pinch-to-zoom.

**2. Filter tray** (component within Camera). Circular or rounded-square thumbnails, the selected one sized ~15–20% larger. Filter name label fades in on selection, fades out after about a second. Needs a "no filter" state as the default, leftmost option.

**3. Studio editor.** Image canvas centered, bottom tab bar switching tool panels: **Filters / Adjust / Crop / Text / Stickers / Draw**. Each tab slides up a context panel without leaving the canvas. Top bar: Cancel (left), Undo/Redo (center or right cluster), Save/Export (right, primary emphasis).

**4. Adjust panel.** One slider per row — Brightness, Contrast, Saturation, Exposure, Temperature, Highlights, Shadows, Vignette, Sharpen. Each row needs a double-tap-to-reset affordance and a numeric readout while dragging.

**5. Crop panel.** Aspect ratio chips: Free, 1:1, 4:5, 16:9, Original. Draggable crop handles with a rule-of-thirds grid overlay while dragging. Rotate/straighten as a wheel or slider.

**6. Text tool.** Tap-to-place text box; font family, size, color, alignment controls; drag to reposition; pinch to resize/rotate.

**7. Sticker / Draw tool.** Sticker picker grid; freehand draw with brush color + size picker; both need a delete gesture (drag to a trash target, or a delete button on selection).

**8. Gallery / Library.** Grid of captured + edited photos, tap to reopen in Studio, long-press for multi-select (batch export/delete).

**9. Export / Share.** Save confirmation as a toast or brief inline state, not a blocking modal, then trigger the native OS share sheet. A quick "Save to Photos" action that skips the full share sheet.

**10. Settings.** Capture resolution/quality, default filter, storage/cache management, permission shortcuts, about/version.

## Design system — a starting point, not a mandate

**Color.** Dark chrome (`#0A0A0A`–`#141414`) isn't a stylistic default here, it's the functional convention for camera apps — Halide, Lightroom Mobile, the stock iOS/Android camera all land here because controls need to recede against unpredictable live video, and dark UI is easier on OLED battery during long capture sessions. For the accent, rather than leave that axis undecided: one concrete starting point is a warm signal-amber (around `#FF9F1C`), echoing a flash / golden-hour cue — it also pairs nicely with a filter like "Golden Hour." Use it sparingly: shutter button, selection states, primary actions only. Make it yours — the two things worth actively avoiding are Snapchat's exact yellow (`#FFFC00`) and a generic acid-green-on-black combination, which reads as an AI-default rather than a choice made for this product.

**Type.** Platform-native system fonts (SF Pro / Roboto) are a deliberate choice here, not a fallback — for a utility tool where the photo is the content, fast native text rendering and a familiar feel beat typographic personality. If cross-platform brand consistency matters more to you than that native feel, pick one shared custom font instead — just pick one; mixing native and custom reads as unfinished.

**Grid.** 8pt spacing scale.

**Icons.** Single-weight outline set. Anything sitting over live video needs a translucent scrim or drop shadow to stay legible against unpredictable backgrounds.

## Component inventory

Shutter button · filter thumbnail chip · bottom tool tab bar · slider control (with reset + value readout) · aspect-ratio chip · color swatch picker · sticker grid item · top nav bar · slide-up sheet · toast/confirmation · permission-request modal

## Motion notes (for prototyping / handoff, not literal Figma layers)

- Filter swipe: crossfade between live-filtered previews, no black flash between frames
- Shutter: brief scale/flash feedback on capture
- Studio panels: spring/ease slide-up, not a linear frame-based feel
- Undo/redo pairs well with haptic feedback on-device — a platform capability, not something to depict in Figma itself

## File organization suggestion

Pages: **Design System** · **Camera Flow** · **Studio Flow** · **Gallery & Export** · **Settings** · **Components**. Use Figma variables for color/spacing tokens so iOS and Android frames can share one token set. Lay out iOS (390×844) and Android (360×800 or 412×915) frames side-by-side per screen so platform parity is easy to eyeball.

## If you use Figma's AI drafting tools

Each screen section above is written to double as a prompt: it names the screen, its job, and its controls, which is what First Draft / the Figma agent needs to produce something more useful than a generic layout. Treat what it generates as a rough frame to rebuild with real components and tokens — current tooling is good for a fast start and weak at finishing inside a real design system, so budget time to restructure rather than expecting a final screen.
