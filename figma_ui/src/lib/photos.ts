import { type Adjustments, ZERO_ADJUST } from "./filters"

export type Photo = {
  id: string
  src: string
  alt: string
  filterId: string
  adjust: Adjustments
  aspect: string // e.g. "3/4"
  createdAt: number
}

// Seed library — Unsplash imagery standing in for captured/edited photos.
const SEED: Omit<Photo, "adjust" | "filterId" | "createdAt">[] = [
  { id: "p1", src: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", alt: "Portrait, pinstriped top", aspect: "3/4" },
  { id: "p2", src: "https://images.unsplash.com/photo-1604223190546-a43e4c7f29d7?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", alt: "Layered mountain ridges at sunset", aspect: "3/4" },
  { id: "p3", src: "https://images.unsplash.com/photo-1558507652-2d9626c4e67a?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", alt: "Girl by a tree", aspect: "3/4" },
  { id: "p4", src: "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", alt: "Rock formation under blue sky", aspect: "3/4" },
  { id: "p5", src: "https://images.unsplash.com/photo-1526835746352-0b9da4054862?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", alt: "Woman at sunset", aspect: "3/4" },
  { id: "p6", src: "https://images.unsplash.com/photo-1573126617899-41f1dffb196c?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", alt: "Mountains with fog", aspect: "3/4" },
  { id: "p7", src: "https://images.unsplash.com/photo-1676732331165-61bd1e55494a?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", alt: "Woman in tall grass", aspect: "3/4" },
  { id: "p8", src: "https://images.unsplash.com/photo-1477346611705-65d1883cee1e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080", alt: "Aerial brown mountains", aspect: "3/4" },
]

const PRESET_FILTERS = ["golden", "none", "noir", "cool", "warm", "none", "vivid", "fade"]

export function seedLibrary(): Photo[] {
  const now = Date.now()
  return SEED.map((p, i) => ({
    ...p,
    filterId: PRESET_FILTERS[i % PRESET_FILTERS.length],
    adjust: { ...ZERO_ADJUST },
    createdAt: now - i * 1000 * 60 * 37,
  }))
}

// A live-feed stand-in for the viewfinder.
export const VIEWFINDER_SRC =
  "https://images.unsplash.com/photo-1526927071144-dbe4c41835e4?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080"
