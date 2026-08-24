import { useEffect, useState } from "react"
import { ThemeProvider } from "@figma/astraui"
import CameraScreen from "./components/CameraScreen"
import StudioScreen from "./components/StudioScreen"
import GalleryScreen from "./components/GalleryScreen"
import { seedLibrary, VIEWFINDER_SRC, type Photo } from "./lib/photos"
import { ZERO_ADJUST } from "./lib/filters"

type Screen = "camera" | "studio" | "gallery"

export default function App() {
  const [screen, setScreen] = useState<Screen>("camera")
  const [photos, setPhotos] = useState<Photo[]>(() => seedLibrary())
  const [editing, setEditing] = useState<Photo | null>(null)

  // This app forces dark chrome regardless of the kit's theme persistence.
  useEffect(() => {
    document.documentElement.classList.add("dark")
  }, [])

  const capture = (filterId: string) => {
    const shot: Photo = {
      id: "cap-" + Date.now(),
      src: VIEWFINDER_SRC,
      alt: "Captured photo",
      filterId,
      adjust: { ...ZERO_ADJUST },
      aspect: "3/4",
      createdAt: Date.now(),
    }
    setPhotos((prev) => [shot, ...prev])
  }

  const saveEdit = (next: Photo) => {
    setPhotos((prev) => prev.map((p) => (p.id === next.id ? next : p)))
  }

  const deletePhotos = (ids: string[]) => {
    const set = new Set(ids)
    setPhotos((prev) => prev.filter((p) => !set.has(p.id)))
  }

  return (
    <ThemeProvider>
      <div className="dark flex h-full w-full items-center justify-center bg-[#050505] p-0 sm:p-6">
        {/* Mobile device frame */}
        <div
          className="relative overflow-hidden bg-ink-900 shadow-2xl"
          style={{
            width: "min(100vw, 402px)",
            height: "min(100dvh, 858px)",
            borderRadius: 46,
          }}
        >
          <div className="h-full w-full overflow-hidden rounded-[inherit]">
            {screen === "camera" && (
              <CameraScreen
                lastPhoto={photos[0]}
                onCapture={capture}
                onOpenGallery={() => setScreen("gallery")}
              />
            )}
            {screen === "gallery" && (
              <GalleryScreen
                photos={photos}
                onBack={() => setScreen("camera")}
                onOpen={(p) => {
                  setEditing(p)
                  setScreen("studio")
                }}
                onDelete={deletePhotos}
              />
            )}
            {screen === "studio" && editing && (
              <StudioScreen
                photo={editing}
                onCancel={() => setScreen("gallery")}
                onSave={(next) => {
                  saveEdit(next)
                  setEditing(next)
                }}
              />
            )}
          </div>
        </div>
      </div>
    </ThemeProvider>
  )
}
