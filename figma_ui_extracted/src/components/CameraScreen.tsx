import { useRef, useState } from "react"
import { Zap, ZapOff, SwitchCamera, Settings } from "lucide-react"
import { SegmentedControl } from "@figma/astraui"
import { Camera, Video } from "lucide-react"
import FilterTray from "./FilterTray"
import ShutterButton from "./ShutterButton"
import { FILTERS } from "../lib/filters"
import { VIEWFINDER_SRC, type Photo } from "../lib/photos"

type Reticle = { x: number; y: number; key: number }

export default function CameraScreen({
  lastPhoto,
  onCapture,
  onOpenGallery,
}: {
  lastPhoto?: Photo
  onCapture: (filterId: string) => void
  onOpenGallery: () => void
}) {
  const [filterId, setFilterId] = useState("golden")
  const [flash, setFlash] = useState(false)
  const [mode, setMode] = useState("photo")
  const [zoom, setZoom] = useState(1)
  const [reticle, setReticle] = useState<Reticle | null>(null)
  const [labelKey, setLabelKey] = useState(0)
  const [capturing, setCapturing] = useState(false)
  const frameRef = useRef<HTMLDivElement>(null)

  const activeFilter = FILTERS.find((f) => f.id === filterId) ?? FILTERS[0]

  const selectFilter = (id: string) => {
    setFilterId(id)
    setLabelKey((k) => k + 1)
  }

  const tapFocus = (e: React.PointerEvent) => {
    const rect = frameRef.current?.getBoundingClientRect()
    if (!rect) return
    setReticle({ x: e.clientX - rect.left, y: e.clientY - rect.top, key: Date.now() })
  }

  const capture = () => {
    setCapturing(true)
    onCapture(filterId)
    window.setTimeout(() => setCapturing(false), 380)
  }

  const zoomSteps = [1, 1.5, 2, 3]

  return (
    <div ref={frameRef} className="relative h-full w-full overflow-hidden bg-ink-900" onPointerDown={tapFocus}>
      {/* Live feed stand-in */}
      <img
        src={VIEWFINDER_SRC}
        alt="Live viewfinder"
        draggable={false}
        className="absolute inset-0 h-full w-full object-cover transition-[filter,transform] duration-500 ease-out"
        style={{ filter: activeFilter.css === "none" ? undefined : activeFilter.css, transform: `scale(${zoom})` }}
      />

      {/* Capture flash */}
      {capturing && <div className="animate-capture-flash pointer-events-none absolute inset-0 bg-white" />}

      {/* Tap-to-focus reticle */}
      {reticle && (
        <div
          key={reticle.key}
          className="animate-reticle pointer-events-none absolute"
          style={{ left: reticle.x - 34, top: reticle.y - 34 }}
        >
          <div className="h-[68px] w-[68px] rounded-md border-2" style={{ borderColor: "var(--color-amber)" }} />
        </div>
      )}

      {/* Top scrim + controls */}
      <div
        className="absolute inset-x-0 top-0 flex items-center justify-between px-5 pb-8 pt-5"
        style={{ background: "linear-gradient(to bottom, var(--color-scrim-strong), transparent)" }}
        onPointerDown={(e) => e.stopPropagation()}
      >
        <ChromeButton label={flash ? "Flash on" : "Flash off"} onClick={() => setFlash((v) => !v)} active={flash}>
          {flash ? <Zap size={20} /> : <ZapOff size={20} />}
        </ChromeButton>

        <div className="scale-90">
          <SegmentedControl
            segments={[
              { id: "photo", icon: <Camera size={24} /> },
              { id: "video", icon: <Video size={24} /> },
            ]}
            selectedSegment={mode}
            onChange={setMode}
          />
        </div>

        <div className="flex items-center gap-2">
          <ChromeButton label="Settings" onClick={() => {}}>
            <Settings size={20} />
          </ChromeButton>
          <ChromeButton label="Flip camera" onClick={() => {}}>
            <SwitchCamera size={20} />
          </ChromeButton>
        </div>
      </div>

      {/* Zoom pill */}
      <div
        className="absolute left-1/2 -translate-x-1/2 rounded-full px-1 py-1"
        style={{ bottom: 220, background: "var(--color-scrim-strong)", backdropFilter: "blur(8px)" }}
        onPointerDown={(e) => e.stopPropagation()}
      >
        <div className="flex items-center gap-1">
          {zoomSteps.map((z) => {
            const active = z === zoom
            return (
              <button
                key={z}
                onClick={() => setZoom(z)}
                className="tabular grid h-8 min-w-8 place-items-center rounded-full px-2 text-xs font-semibold transition-colors"
                style={{
                  color: active ? "var(--color-amber)" : "var(--color-fg-muted)",
                  background: active ? "var(--color-amber-soft)" : "transparent",
                }}
              >
                {z}×
              </button>
            )
          })}
        </div>
      </div>

      {/* Bottom controls */}
      <div
        className="absolute inset-x-0 bottom-0 pt-14"
        style={{ background: "linear-gradient(to top, var(--color-scrim-strong) 55%, transparent)" }}
        onPointerDown={(e) => e.stopPropagation()}
      >
        {/* Filter name label */}
        <div className="mb-3 flex h-5 items-center justify-center">
          {filterId !== "none" && (
            <span key={labelKey} className="animate-label-flash text-sm font-medium tracking-wide text-fg">
              {activeFilter.name}
            </span>
          )}
        </div>

        <FilterTray feedSrc={VIEWFINDER_SRC} selectedId={filterId} onSelect={selectFilter} />

        {/* Shutter row */}
        <div className="grid grid-cols-3 items-center px-8 pb-8 pt-5">
          {/* Gallery shortcut */}
          <div className="justify-self-start">
            <button
              onClick={onOpenGallery}
              aria-label="Open library"
              className="h-12 w-12 overflow-hidden rounded-xl border-2"
              style={{ borderColor: "var(--color-line-strong)" }}
            >
              {lastPhoto ? (
                <img
                  src={lastPhoto.src}
                  alt="Last capture"
                  className="h-full w-full object-cover"
                  style={{ filter: FILTERS.find((f) => f.id === lastPhoto.filterId)?.css }}
                />
              ) : (
                <span className="block h-full w-full bg-ink-700" />
              )}
            </button>
          </div>

          <div className="justify-self-center">
            <ShutterButton onCapture={capture} />
          </div>

          {/* Balance / mode hint */}
          <div className="tabular justify-self-end text-xs font-medium uppercase tracking-widest text-fg-faint">
            {mode}
          </div>
        </div>
      </div>
    </div>
  )
}

// Translucent circular chrome button — legible over any live background.
// Astra's IconButton uses light SaaS surfaces that don't recede over video.
function ChromeButton({
  children,
  label,
  onClick,
  active,
}: {
  children: React.ReactNode
  label: string
  onClick: () => void
  active?: boolean
}) {
  return (
    <button
      aria-label={label}
      onClick={onClick}
      className="grid h-10 w-10 place-items-center rounded-full transition-colors"
      style={{
        background: active ? "var(--color-amber)" : "var(--color-scrim-strong)",
        color: active ? "#0a0a0a" : "var(--color-fg)",
        backdropFilter: "blur(8px)",
      }}
    >
      {children}
    </button>
  )
}
