import { useState } from "react"
import { X, Undo2, Redo2, Check, SlidersHorizontal, Crop as CropIcon, Wand2, Type, Sticker, Pencil } from "lucide-react"
import { Toast } from "@figma/astraui"
import FiltersPanel from "./studio/FiltersPanel"
import AdjustPanel from "./studio/AdjustPanel"
import CropPanel, { aspectRatio, type CropState } from "./studio/CropPanel"
import { FILTERS, buildFilter, ZERO_ADJUST, type Adjustments } from "../lib/filters"
import { type Photo } from "../lib/photos"

type Tab = "filters" | "adjust" | "crop" | "text" | "stickers" | "draw"
type EditState = { filterId: string; adjust: Adjustments; crop: CropState }

const TOOLS: { id: Tab; label: string; icon: typeof CropIcon; live: boolean }[] = [
  { id: "filters", label: "Filters", icon: Wand2, live: true },
  { id: "adjust", label: "Adjust", icon: SlidersHorizontal, live: true },
  { id: "crop", label: "Crop", icon: CropIcon, live: true },
  { id: "text", label: "Text", icon: Type, live: false },
  { id: "stickers", label: "Stickers", icon: Sticker, live: false },
  { id: "draw", label: "Draw", icon: Pencil, live: false },
]

export default function StudioScreen({
  photo,
  onCancel,
  onSave,
}: {
  photo: Photo
  onCancel: () => void
  onSave: (next: Photo) => void
}) {
  const initial: EditState = {
    filterId: photo.filterId,
    adjust: { ...photo.adjust },
    crop: { aspect: "original", straighten: 0 },
  }
  const [history, setHistory] = useState<EditState[]>([initial])
  const [cursor, setCursor] = useState(0)
  const [tab, setTab] = useState<Tab>("filters")
  const [saved, setSaved] = useState(false)

  const state = history[cursor]
  const canUndo = cursor > 0
  const canRedo = cursor < history.length - 1

  const commit = (next: EditState) => {
    const trimmed = history.slice(0, cursor + 1)
    setHistory([...trimmed, next])
    setCursor(trimmed.length)
  }

  const baseCss = FILTERS.find((f) => f.id === state.filterId)?.css ?? "none"
  const canvasFilter = buildFilter(baseCss, state.adjust)
  const ratio = aspectRatio(state.crop.aspect)
  const showGrid = tab === "crop"

  const save = () => {
    onSave({ ...photo, filterId: state.filterId, adjust: state.adjust })
    setSaved(true)
    window.setTimeout(() => setSaved(false), 2200)
  }

  return (
    <div className="flex h-full w-full flex-col bg-ink-900">
      {/* Top bar */}
      <div className="flex items-center justify-between px-4 pb-3 pt-5">
        <button
          onClick={onCancel}
          className="rounded-full px-3 py-2 text-sm font-medium text-fg-muted transition-colors hover:text-fg"
        >
          Cancel
        </button>

        <div className="flex items-center gap-1">
          <IconBtn label="Undo" disabled={!canUndo} onClick={() => canUndo && setCursor(cursor - 1)}>
            <Undo2 size={19} />
          </IconBtn>
          <IconBtn label="Redo" disabled={!canRedo} onClick={() => canRedo && setCursor(cursor + 1)}>
            <Redo2 size={19} />
          </IconBtn>
        </div>

        <button
          onClick={save}
          className="flex items-center gap-1.5 rounded-full px-4 py-2 text-sm font-semibold"
          style={{ background: "var(--color-amber)", color: "#0a0a0a" }}
        >
          <Check size={16} />
          Save
        </button>
      </div>

      {/* Canvas */}
      <div className="relative flex flex-1 items-center justify-center overflow-hidden px-4">
        <div
          className="relative overflow-hidden rounded-panel"
          style={{
            maxHeight: "100%",
            maxWidth: "100%",
            aspectRatio: ratio ? String(ratio) : photo.aspect.replace("/", " / "),
            height: ratio && ratio > 1 ? "auto" : "100%",
            width: ratio && ratio > 1 ? "100%" : "auto",
          }}
        >
          <img
            src={photo.src}
            alt={photo.alt}
            draggable={false}
            className="h-full w-full object-cover transition-[filter,transform] duration-200"
            style={{ filter: canvasFilter, transform: `rotate(${state.crop.straighten}deg) scale(${1 + Math.abs(state.crop.straighten) / 90})` }}
          />
          {state.adjust.vignette > 0 && (
            <div
              className="pointer-events-none absolute inset-0"
              style={{ boxShadow: `inset 0 0 ${60 + state.adjust.vignette}px ${20 + state.adjust.vignette / 2}px rgba(0,0,0,${state.adjust.vignette / 130})` }}
            />
          )}

          {/* Rule-of-thirds grid + crop handles */}
          {showGrid && (
            <div className="pointer-events-none absolute inset-0">
              <div className="absolute inset-0" style={{ border: "1px solid rgba(255,255,255,0.9)" }} />
              {[1, 2].map((i) => (
                <div key={"v" + i} className="absolute top-0 bottom-0 w-px" style={{ left: `${(i * 100) / 3}%`, background: "rgba(255,255,255,0.4)" }} />
              ))}
              {[1, 2].map((i) => (
                <div key={"h" + i} className="absolute left-0 right-0 h-px" style={{ top: `${(i * 100) / 3}%`, background: "rgba(255,255,255,0.4)" }} />
              ))}
              {["-top-px -left-px", "-top-px -right-px", "-bottom-px -left-px", "-bottom-px -right-px"].map((pos, i) => (
                <div
                  key={i}
                  className={`absolute h-6 w-6 ${pos}`}
                  style={{
                    borderTop: pos.includes("top") ? "3px solid var(--color-amber)" : undefined,
                    borderBottom: pos.includes("bottom") ? "3px solid var(--color-amber)" : undefined,
                    borderLeft: pos.includes("left") ? "3px solid var(--color-amber)" : undefined,
                    borderRight: pos.includes("right") ? "3px solid var(--color-amber)" : undefined,
                  }}
                />
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Context panel (slides up) */}
      <div key={tab} className="animate-sheet-up border-t border-line bg-ink-850">
        {tab === "filters" && (
          <FiltersPanel src={photo.src} selectedId={state.filterId} onSelect={(id) => commit({ ...state, filterId: id })} />
        )}
        {tab === "adjust" && <AdjustPanel adjust={state.adjust} onChange={(adjust) => commit({ ...state, adjust })} />}
        {tab === "crop" && <CropPanel crop={state.crop} onChange={(crop) => commit({ ...state, crop })} />}
        {!TOOLS.find((t) => t.id === tab)?.live && (
          <div className="flex h-40 flex-col items-center justify-center gap-2 px-6 text-center">
            <span className="text-sm font-medium text-fg">{TOOLS.find((t) => t.id === tab)?.label} tool</span>
            <span className="text-xs text-fg-faint">Coming in the next pass of the Studio.</span>
          </div>
        )}
      </div>

      {/* Tool tab bar */}
      <div className="no-scrollbar flex items-stretch justify-between gap-1 overflow-x-auto border-t border-line bg-ink-900 px-3 pb-6 pt-3">
        {TOOLS.map((t) => {
          const active = t.id === tab
          const Icon = t.icon
          return (
            <button
              key={t.id}
              onClick={() => setTab(t.id)}
              className="flex flex-1 min-w-[60px] flex-col items-center gap-1.5 rounded-chip py-2 transition-colors"
              style={{ color: active ? "var(--color-amber)" : "var(--color-fg-faint)" }}
            >
              <Icon size={21} />
              <span className="text-[11px] font-medium">{t.label}</span>
            </button>
          )
        })}
      </div>

      {/* Save confirmation (Astra Toast) */}
      {saved && (
        <div className="pointer-events-none absolute inset-x-0 bottom-28 z-10 flex justify-center px-4">
          <div className="pointer-events-auto">
            <Toast message="Saved to Library" variant="success" progress={100} showCancel={false} />
          </div>
        </div>
      )}
    </div>
  )
}

function IconBtn({
  children,
  label,
  onClick,
  disabled,
}: {
  children: React.ReactNode
  label: string
  onClick: () => void
  disabled?: boolean
}) {
  return (
    <button
      aria-label={label}
      onClick={onClick}
      disabled={disabled}
      className="grid h-10 w-10 place-items-center rounded-full transition-colors disabled:opacity-30"
      style={{ color: "var(--color-fg)" }}
    >
      {children}
    </button>
  )
}
