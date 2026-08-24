import { useState } from "react"
import Slider from "./Slider"
import { ADJUST_META, type AdjustKey, type Adjustments } from "../../lib/filters"

export default function AdjustPanel({
  adjust,
  onChange,
}: {
  adjust: Adjustments
  onChange: (next: Adjustments) => void
}) {
  const [dragKey, setDragKey] = useState<AdjustKey | null>(null)

  const set = (key: AdjustKey, v: number) => onChange({ ...adjust, [key]: v })
  const reset = (key: AdjustKey) => onChange({ ...adjust, [key]: 0 })

  return (
    <div className="no-scrollbar max-h-[46vh] overflow-y-auto px-6 pb-6 pt-2">
      <div className="flex flex-col gap-4">
        {ADJUST_META.map((m) => (
          <Slider
            key={m.key}
            label={m.label}
            value={adjust[m.key]}
            min={m.min}
            max={m.max}
            dragging={dragKey === m.key}
            onDragStart={() => setDragKey(m.key)}
            onDragEnd={() => setDragKey(null)}
            onChange={(v) => set(m.key, v)}
            onReset={() => reset(m.key)}
          />
        ))}
      </div>
    </div>
  )
}
