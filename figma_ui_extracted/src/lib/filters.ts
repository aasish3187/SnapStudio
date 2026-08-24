// Shared filter definitions used by the camera tray, viewfinder, gallery, and Studio.
// Each `css` value is a CSS `filter` string applied over the live/photo image.

export type FilterDef = {
  id: string
  name: string
  css: string
}

export const FILTERS: FilterDef[] = [
  { id: "none", name: "Original", css: "none" },
  { id: "golden", name: "Golden Hour", css: "sepia(0.35) saturate(1.45) contrast(1.05) brightness(1.06) hue-rotate(-12deg)" },
  { id: "noir", name: "Noir", css: "grayscale(1) contrast(1.32) brightness(0.94)" },
  { id: "vivid", name: "Vivid", css: "saturate(1.7) contrast(1.12) brightness(1.03)" },
  { id: "fade", name: "Fade", css: "contrast(0.86) brightness(1.1) saturate(0.82) sepia(0.12)" },
  { id: "cool", name: "Arctic", css: "saturate(1.15) contrast(1.06) brightness(1.02) hue-rotate(14deg)" },
  { id: "warm", name: "Ember", css: "sepia(0.22) saturate(1.35) contrast(1.08) hue-rotate(-8deg)" },
  { id: "mono", name: "Silver", css: "grayscale(1) contrast(1.08) brightness(1.08)" },
  { id: "dream", name: "Dream", css: "saturate(1.3) brightness(1.08) contrast(0.94) blur(0.3px) sepia(0.08)" },
]

// The nine adjustable parameters shown in the Adjust panel.
export type AdjustKey =
  | "brightness"
  | "contrast"
  | "saturation"
  | "exposure"
  | "temperature"
  | "highlights"
  | "shadows"
  | "vignette"
  | "sharpen"

export type Adjustments = Record<AdjustKey, number>

export const ADJUST_META: { key: AdjustKey; label: string; min: number; max: number }[] = [
  { key: "brightness", label: "Brightness", min: -100, max: 100 },
  { key: "contrast", label: "Contrast", min: -100, max: 100 },
  { key: "saturation", label: "Saturation", min: -100, max: 100 },
  { key: "exposure", label: "Exposure", min: -100, max: 100 },
  { key: "temperature", label: "Temperature", min: -100, max: 100 },
  { key: "highlights", label: "Highlights", min: -100, max: 100 },
  { key: "shadows", label: "Shadows", min: -100, max: 100 },
  { key: "vignette", label: "Vignette", min: 0, max: 100 },
  { key: "sharpen", label: "Sharpen", min: 0, max: 100 },
]

export const ZERO_ADJUST: Adjustments = {
  brightness: 0,
  contrast: 0,
  saturation: 0,
  exposure: 0,
  temperature: 0,
  highlights: 0,
  shadows: 0,
  vignette: 0,
  sharpen: 0,
}

// Compose a CSS filter string from a base filter + numeric adjustments.
export function buildFilter(baseCss: string, adj: Adjustments): string {
  const parts: string[] = []
  if (baseCss && baseCss !== "none") parts.push(baseCss)
  parts.push(`brightness(${1 + adj.brightness / 200 + adj.exposure / 260})`)
  parts.push(`contrast(${1 + adj.contrast / 200 - adj.shadows / 500 + adj.highlights / 600})`)
  parts.push(`saturate(${1 + adj.saturation / 130})`)
  if (adj.temperature !== 0) parts.push(`sepia(${Math.max(0, adj.temperature) / 220}) hue-rotate(${-adj.temperature / 7}deg)`)
  if (adj.sharpen > 0) parts.push(`contrast(${1 + adj.sharpen / 400})`)
  return parts.join(" ")
}

export function isEdited(adj: Adjustments, filterId: string): boolean {
  return filterId !== "none" || Object.values(adj).some((v) => v !== 0)
}
