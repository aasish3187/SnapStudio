import { useState } from "react"

// Bespoke shutter control: the accent-amber ring is the single most important
// affordance on the viewfinder. Astra's <Button>/<IconButton> are rectangular/
// SaaS-styled and cannot express the concentric capture ring + press feedback.
export default function ShutterButton({ onCapture }: { onCapture: () => void }) {
  const [pressed, setPressed] = useState(false)

  return (
    <button
      aria-label="Capture photo"
      onPointerDown={() => setPressed(true)}
      onPointerUp={() => setPressed(false)}
      onPointerLeave={() => setPressed(false)}
      onClick={onCapture}
      className="relative grid place-items-center outline-none"
      style={{ width: 78, height: 78 }}
    >
      {/* Outer ring */}
      <span
        className="absolute inset-0 rounded-full border-[3px] transition-transform duration-200"
        style={{
          borderColor: "var(--color-fg)",
          transform: pressed ? "scale(0.92)" : "scale(1)",
        }}
      />
      {/* Inner amber disc */}
      <span
        className="rounded-full transition-all duration-150"
        style={{
          width: pressed ? 52 : 60,
          height: pressed ? 52 : 60,
          background: "var(--color-amber)",
          boxShadow: pressed ? "none" : "0 0 22px var(--color-amber-glow)",
        }}
      />
    </button>
  )
}
