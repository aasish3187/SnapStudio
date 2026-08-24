# SnapStudio — Core Flow Build Plan

## Context

The brief (`src/imports/FIGMA_DESIGN_BRIEF.md`) describes **SnapStudio**, a camera-first photo app: full-bleed live viewfinder, swipeable filter tray, a Studio editor (Filters/Adjust/Crop/Text/Stickers/Draw), a Gallery, and export/settings. The user wants a stunning, exhibition-quality result.

Two things shaped this plan via clarifying questions:
- **Design authority:** Follow the SnapStudio brief's *dark camera aesthetic*; use Astra kit components only where they genuinely fit. (Astra is a light/lavender SaaS kit that otherwise conflicts with a dark camera app.)
- **Scope for this pass:** Core flow with deep polish — **Camera/Viewfinder + Studio editor (Filters, Adjust, Crop) + Gallery**. Text/Stickers/Draw, full Settings, and Share are out of scope this pass (Studio will show the remaining tabs as present-but-inactive so the structure reads complete).

Note: the user mentioned adding design-system tokens to `styles/global.css` / Tailwind, but no such file exists — `src/index.css` only has `@import 'tailwindcss';`. This plan **creates** that token layer in `src/index.css` so the design system is real and editable in one place, honoring the intent.

## Design system (established in `src/index.css`)

Single source of truth via Tailwind v4 `@theme` tokens + CSS variables, so the user can retune by editing CSS:
- **Color:** near-black chrome ramp (`--color-ink-900 #0A0A0A` → `--color-ink-800 #141414` → elevated `#1C1C1E`), neutral text ramp, translucent scrim tokens, hairline border token. Accent = warm signal-amber `#FF9F1C` (`--color-amber`) used *only* on shutter, selection states, primary actions (avoids Snapchat yellow + acid-green defaults per brief).
- **Radius / spacing:** 8pt scale exposed as tokens; `corner` radii for chips/sheets.
- **Typography:** one shared custom font — **Inter** (variable, via Google Fonts CSS2 `@import` at top of `index.css`) with a tight numeric/tabular treatment for readouts. Type scale defined as tokens; all generated text uses these faces only (honors "ONLY use font faces defined in the css").
- **Elevation:** soft scrim shadows for controls over live video.

## Kit reconciliation

`package.json` already pins `@figma/astraui-kit@0.1.3` (matches `<figma_selected_make_kits>`). Astra's `setup.md` additionally requires the base package `@figma/astraui` — add `"@figma/astraui": "1.0.0"` to `dependencies` and install. Import `@figma/astraui/styles.css` and wrap the app in `ThemeProvider` (forced dark). Astra components will be used where they fit neutrally; bespoke dark controls elsewhere carry one-line justification comments per kit stop-rules (Astra ships no slider, and its light SaaS surfaces don't suit the viewfinder).

**Astra components used:** `Toast` (save confirmation), `SegmentedControl` (Photo/Video top switch), `Modal` (permission-request), `SelectField`/`Badge` where they read neutrally in Gallery. Everything on the live viewfinder is bespoke.

## Files

- `src/index.css` — add font `@import`, `@theme` tokens, base dark body styling. (No unlayered `*` reset.)
- `src/App.tsx` — replace placeholder with a centered mobile device frame + a lightweight screen state machine (`camera | studio | gallery`) sharing a selected-photo/edit state; wraps in Astra `ThemeProvider`.
- `src/components/CameraScreen.tsx` — full-bleed preview (Unsplash/gradient stand-in for live feed), top scrim bar (flash toggle, flip, `SegmentedControl` photo/video), tap-to-focus reticle, pinch/zoom pill, bottom `FilterTray`, `ShutterButton`, gallery thumbnail shortcut → Gallery. Capture flash + scale feedback.
- `src/components/FilterTray.tsx` — horizontally swipeable rounded-square live-filtered mini-previews (CSS filter matrices over the feed image), selected item ~18% larger, filter-name label fades in/out (~1s), leftmost "None" default.
- `src/components/ShutterButton.tsx` — amber ring shutter with press scale/flash animation.
- `src/components/StudioScreen.tsx` — centered canvas, top bar (Cancel / Undo-Redo / primary Save), bottom tool tab bar (Filters, Adjust, Crop active; Text/Stickers/Draw present-inactive). Context panels slide up with spring ease. Save triggers Astra `Toast`.
- `src/components/studio/FiltersPanel.tsx` — filter strip reusing filter defs.
- `src/components/studio/AdjustPanel.tsx` — one `Slider` per row (Brightness, Contrast, Saturation, Exposure, Temperature, Highlights, Shadows, Vignette, Sharpen), double-tap-to-reset, live numeric readout; live CSS-filter application to canvas.
- `src/components/studio/Slider.tsx` — bespoke dark slider (Astra has none) with tabular value readout + reset.
- `src/components/studio/CropPanel.tsx` — aspect chips (Free/1:1/4:5/16:9/Original), rule-of-thirds grid overlay + draggable handles, straighten slider.
- `src/components/GalleryScreen.tsx` — masonry/grid of captured+edited photos, tap → Studio, long-press → multi-select mode with batch action bar (`Badge` count). Header with back-to-camera.
- `src/lib/filters.ts` — shared filter definitions (name + CSS filter string) used by tray, camera, and Studio.
- `src/lib/photos.ts` — seed photo set (Unsplash via `mcp__plugin_make_unsplash__search_photos`) with per-photo edit state.

## Interaction / motion

- Filter swipe: crossfade between filtered previews (no black flash).
- Shutter: brief scale + white flash overlay on capture, then thumbnail pop into gallery shortcut.
- Studio panels: spring/ease slide-up (CSS transitions with cubic-bezier), not linear.
- Tap-to-focus reticle animates in at tap point and fades; pinch-zoom shows a value pill.

All state is local React (`useState`/`useReducer`); no backend. Undo/redo backed by an edit-history stack in Studio.

## Verification

- Rely on the already-running Vite dev server (hot reload); no manual start.
- After install, confirm `@figma/astraui` resolves and `styles.css` imports without error; check `figma logs` only if a concrete failure appears.
- Manually walk the flow in the preview: Camera → swipe filters → shoot (flash + thumbnail) → open Gallery → tap photo → Studio → Adjust sliders live-update canvas → Crop aspect + grid → Save shows Toast. Verify long-press multi-select in Gallery.
- Confirm dark aesthetic + amber-only accent, all text in Inter, no raw hex in components (tokens only), no console errors.
