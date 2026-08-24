import { useRef, useState } from "react"
import { ChevronLeft, Camera, Trash2, Download, Check } from "lucide-react"
import { Badge } from "@figma/astraui"
import { FILTERS, buildFilter } from "../lib/filters"
import { type Photo } from "../lib/photos"

export default function GalleryScreen({
  photos,
  onBack,
  onOpen,
  onDelete,
}: {
  photos: Photo[]
  onBack: () => void
  onOpen: (photo: Photo) => void
  onDelete: (ids: string[]) => void
}) {
  const [selectMode, setSelectMode] = useState(false)
  const [selected, setSelected] = useState<Set<string>>(new Set())
  const pressTimer = useRef<number | null>(null)

  const toggle = (id: string) => {
    setSelected((prev) => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })
  }

  const startPress = (id: string) => {
    pressTimer.current = window.setTimeout(() => {
      setSelectMode(true)
      toggle(id)
    }, 420)
  }
  const endPress = () => {
    if (pressTimer.current) window.clearTimeout(pressTimer.current)
  }

  const exitSelect = () => {
    setSelectMode(false)
    setSelected(new Set())
  }

  const handleTap = (photo: Photo) => {
    if (selectMode) toggle(photo.id)
    else onOpen(photo)
  }

  return (
    <div className="flex h-full w-full flex-col bg-ink-900">
      {/* Header */}
      <div className="flex items-center justify-between px-4 pb-3 pt-6">
        {selectMode ? (
          <>
            <button onClick={exitSelect} className="text-sm font-medium text-fg-muted">
              Done
            </button>
            <span className="tabular text-sm font-semibold text-fg">
              {selected.size} selected
            </span>
            <div className="w-12" />
          </>
        ) : (
          <>
            <button
              onClick={onBack}
              aria-label="Back to camera"
              className="grid h-10 w-10 place-items-center rounded-full text-fg"
              style={{ background: "var(--color-ink-750)" }}
            >
              <ChevronLeft size={22} />
            </button>
            <div className="flex flex-col items-center">
              <span className="text-base font-semibold text-fg">Library</span>
              <span className="tabular text-xs text-fg-faint">{photos.length} photos</span>
            </div>
            <button
              onClick={onBack}
              aria-label="Camera"
              className="grid h-10 w-10 place-items-center rounded-full"
              style={{ background: "var(--color-amber)", color: "#0a0a0a" }}
            >
              <Camera size={20} />
            </button>
          </>
        )}
      </div>

      {/* Grid */}
      <div className="no-scrollbar flex-1 overflow-y-auto px-1 pb-32">
        <div className="grid grid-cols-3 gap-1">
          {photos.map((photo) => {
            const isSel = selected.has(photo.id)
            const css = buildFilter(FILTERS.find((f) => f.id === photo.filterId)?.css ?? "none", photo.adjust)
            return (
              <button
                key={photo.id}
                onClick={() => handleTap(photo)}
                onPointerDown={() => startPress(photo.id)}
                onPointerUp={endPress}
                onPointerLeave={endPress}
                className="relative aspect-[3/4] overflow-hidden"
              >
                <img
                  src={photo.src}
                  alt={photo.alt}
                  draggable={false}
                  className="h-full w-full object-cover transition-transform duration-300"
                  style={{ filter: css, transform: isSel ? "scale(0.9)" : "scale(1)" }}
                />
                {photo.filterId !== "none" && !selectMode && (
                  <span className="absolute bottom-1.5 left-1.5">
                    <Badge label={FILTERS.find((f) => f.id === photo.filterId)?.name ?? ""} variant="secondary" />
                  </span>
                )}
                {selectMode && (
                  <span
                    className="absolute right-2 top-2 grid h-6 w-6 place-items-center rounded-full border-2 transition-colors"
                    style={{
                      background: isSel ? "var(--color-amber)" : "rgba(0,0,0,0.35)",
                      borderColor: isSel ? "var(--color-amber)" : "rgba(255,255,255,0.7)",
                      color: "#0a0a0a",
                    }}
                  >
                    {isSel && <Check size={15} strokeWidth={3} />}
                  </span>
                )}
              </button>
            )
          })}
        </div>
      </div>

      {/* Batch action bar */}
      {selectMode && selected.size > 0 && (
        <div className="absolute inset-x-0 bottom-0 border-t border-line bg-ink-850 px-6 pb-8 pt-4">
          <div className="flex items-center justify-around">
            <BatchAction label="Export" onClick={() => exitSelect()}>
              <Download size={22} />
            </BatchAction>
            <BatchAction
              label="Delete"
              danger
              onClick={() => {
                onDelete([...selected])
                exitSelect()
              }}
            >
              <Trash2 size={22} />
            </BatchAction>
          </div>
        </div>
      )}
    </div>
  )
}

function BatchAction({
  children,
  label,
  onClick,
  danger,
}: {
  children: React.ReactNode
  label: string
  onClick: () => void
  danger?: boolean
}) {
  return (
    <button
      onClick={onClick}
      className="flex flex-col items-center gap-1.5"
      style={{ color: danger ? "#ff5a5a" : "var(--color-fg)" }}
    >
      {children}
      <span className="text-xs font-medium">{label}</span>
    </button>
  )
}
