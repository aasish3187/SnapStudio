import { useRef } from "react"
import { FILTERS } from "../lib/filters"

// Horizontally-swipeable tray of live-filtered mini-previews. Each thumbnail
// shows the actual feed under that filter (not a static icon). Selected item is
// enlarged ~18%; the filter name flashes in on selection (handled by parent).
// Bespoke: Astra has no filmstrip/thumbnail-carousel primitive.
export default function FilterTray({
  feedSrc,
  selectedId,
  onSelect,
}: {
  feedSrc: string
  selectedId: string
  onSelect: (id: string) => void
}) {
  const trayRef = useRef<HTMLDivElement>(null)

  return (
    <div
      ref={trayRef}
      className="no-scrollbar flex items-end gap-3 overflow-x-auto px-6 pb-1"
      style={{ scrollSnapType: "x proximity" }}
    >
      {FILTERS.map((f) => {
        const active = f.id === selectedId
        const size = active ? 66 : 56
        return (
          <button
            key={f.id}
            onClick={() => onSelect(f.id)}
            className="relative shrink-0 outline-none transition-all duration-200"
            style={{ width: size, scrollSnapAlign: "center" }}
            aria-label={f.name}
            aria-pressed={active}
          >
            <span
              className="block overflow-hidden transition-all duration-200"
              style={{
                width: size,
                height: size,
                borderRadius: 18,
                border: active ? "2.5px solid var(--color-amber)" : "1.5px solid var(--color-line-strong)",
                boxShadow: active ? "0 0 18px var(--color-amber-glow)" : "none",
              }}
            >
              <img
                src={feedSrc}
                alt=""
                draggable={false}
                className="h-full w-full object-cover"
                style={{ filter: f.css === "none" ? undefined : f.css }}
              />
              {f.id === "none" && (
                <span className="absolute inset-0 grid place-items-center bg-scrim">
                  <span className="text-[10px] font-medium tracking-wide text-fg">OFF</span>
                </span>
              )}
            </span>
          </button>
        )
      })}
    </div>
  )
}
