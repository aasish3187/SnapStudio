import { FILTERS } from "../../lib/filters"

export default function FiltersPanel({
  src,
  selectedId,
  onSelect,
}: {
  src: string
  selectedId: string
  onSelect: (id: string) => void
}) {
  return (
    <div className="no-scrollbar flex gap-3 overflow-x-auto px-6 pb-7 pt-4">
      {FILTERS.map((f) => {
        const active = f.id === selectedId
        return (
          <button key={f.id} onClick={() => onSelect(f.id)} className="shrink-0 outline-none" aria-pressed={active}>
            <span
              className="block overflow-hidden transition-all duration-200"
              style={{
                width: 72,
                height: 90,
                borderRadius: 14,
                border: active ? "2.5px solid var(--color-amber)" : "1.5px solid var(--color-line)",
                boxShadow: active ? "0 0 16px var(--color-amber-glow)" : "none",
              }}
            >
              <img
                src={src}
                alt=""
                className="h-full w-full object-cover"
                style={{ filter: f.css === "none" ? undefined : f.css }}
              />
            </span>
            <span
              className="mt-2 block text-center text-xs font-medium"
              style={{ color: active ? "var(--color-amber)" : "var(--color-fg-muted)" }}
            >
              {f.name}
            </span>
          </button>
        )
      })}
    </div>
  )
}
