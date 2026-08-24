import Slider from "./Slider"
import { useState } from "react"

export type CropState = { aspect: string; straighten: number }

const ASPECTS = [
  { id: "free", label: "Free" },
  { id: "1:1", label: "1:1" },
  { id: "4:5", label: "4:5" },
  { id: "16:9", label: "16:9" },
  { id: "original", label: "Original" },
]

export default function CropPanel({
  crop,
  onChange,
}: {
  crop: CropState
  onChange: (next: CropState) => void
}) {
  const [dragging, setDragging] = useState(false)

  return (
    <div className="px-6 pb-7 pt-4">
      <div className="no-scrollbar mb-6 flex gap-2 overflow-x-auto">
        {ASPECTS.map((a) => {
          const active = a.id === crop.aspect
          return (
            <button
              key={a.id}
              onClick={() => onChange({ ...crop, aspect: a.id })}
              className="tabular shrink-0 rounded-chip px-4 py-2 text-sm font-medium transition-colors"
              style={{
                background: active ? "var(--color-amber)" : "var(--color-ink-700)",
                color: active ? "#0a0a0a" : "var(--color-fg-muted)",
              }}
            >
              {a.label}
            </button>
          )
        })}
      </div>

      <Slider
        label="Straighten"
        value={crop.straighten}
        min={-45}
        max={45}
        dragging={dragging}
        onDragStart={() => setDragging(true)}
        onDragEnd={() => setDragging(false)}
        onChange={(v) => onChange({ ...crop, straighten: v })}
        onReset={() => onChange({ ...crop, straighten: 0 })}
      />
    </div>
  )
}

export function aspectRatio(aspect: string): number | null {
  switch (aspect) {
    case "1:1":
      return 1
    case "4:5":
      return 4 / 5
    case "16:9":
      return 16 / 9
    default:
      return null // free / original → follow image
  }
}
