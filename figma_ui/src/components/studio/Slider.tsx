import { useRef } from "react"

// Bespoke dark slider with a live numeric readout and double-tap-to-reset.
// The Astra kit ships no slider/range primitive, so this is hand-built on kit tokens.
export default function Slider({
  label,
  value,
  min,
  max,
  onChange,
  onReset,
  dragging,
  onDragStart,
  onDragEnd,
}: {
  label: string
  value: number
  min: number
  max: number
  onChange: (v: number) => void
  onReset: () => void
  dragging: boolean
  onDragStart: () => void
  onDragEnd: () => void
}) {
  const trackRef = useRef<HTMLDivElement>(null)
  const lastTap = useRef(0)

  const pct = (value - min) / (max - min)
  // Center-origin fill for bipolar ranges (min < 0), left-origin otherwise.
  const bipolar = min < 0
  const zeroPct = bipolar ? (0 - min) / (max - min) : 0

  const setFromClientX = (clientX: number) => {
    const rect = trackRef.current?.getBoundingClientRect()
    if (!rect) return
    const p = Math.min(1, Math.max(0, (clientX - rect.left) / rect.width))
    onChange(Math.round(min + p * (max - min)))
  }

  const handleReset = () => {
    const now = Date.now()
    if (now - lastTap.current < 300) onReset()
    lastTap.current = now
  }

  return (
    <div className="select-none">
      <div className="mb-2 flex items-baseline justify-between">
        <span className="text-sm font-medium text-fg" onClick={handleReset}>
          {label}
        </span>
        <span
          className="tabular text-sm font-semibold transition-colors"
          style={{ color: value !== 0 ? "var(--color-amber)" : "var(--color-fg-faint)" }}
        >
          {value > 0 && bipolar ? "+" : ""}
          {value}
        </span>
      </div>

      <div
        ref={trackRef}
        className="relative h-9 cursor-pointer"
        onPointerDown={(e) => {
          ;(e.target as HTMLElement).setPointerCapture?.(e.pointerId)
          onDragStart()
          setFromClientX(e.clientX)
        }}
        onPointerMove={(e) => {
          if (dragging) setFromClientX(e.clientX)
        }}
        onPointerUp={onDragEnd}
        onDoubleClick={onReset}
      >
        {/* Track */}
        <div
          className="absolute left-0 right-0 top-1/2 h-[3px] -translate-y-1/2 rounded-full"
          style={{ background: "var(--color-line-strong)" }}
        />
        {/* Fill */}
        <div
          className="absolute top-1/2 h-[3px] -translate-y-1/2 rounded-full"
          style={{
            background: "var(--color-amber)",
            left: `${Math.min(zeroPct, pct) * 100}%`,
            right: `${(1 - Math.max(zeroPct, pct)) * 100}%`,
          }}
        />
        {/* Zero tick for bipolar */}
        {bipolar && (
          <div
            className="absolute top-1/2 h-2 w-px -translate-y-1/2"
            style={{ left: `${zeroPct * 100}%`, background: "var(--color-fg-faint)" }}
          />
        )}
        {/* Thumb */}
        <div
          className="absolute top-1/2 -translate-x-1/2 -translate-y-1/2 rounded-full transition-transform"
          style={{
            left: `${pct * 100}%`,
            width: dragging ? 22 : 18,
            height: dragging ? 22 : 18,
            background: "var(--color-fg)",
            boxShadow: dragging ? "0 0 14px var(--color-amber-glow)" : "0 1px 4px rgba(0,0,0,0.5)",
          }}
        />
      </div>
    </div>
  )
}
