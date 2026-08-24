import os
from PIL import Image, ImageEnhance, ImageOps

def create_identity_lut(size=64):
    img = Image.new('RGB', (size * 8, size * 8))
    pixels = img.load()
    for b in range(size):
        block_x = (b % 8) * size
        block_y = (b // 8) * size
        for g in range(size):
            for r in range(size):
                pixels[block_x + r, block_y + g] = (
                    int(r * 255 / (size - 1)),
                    int(g * 255 / (size - 1)),
                    int(b * 255 / (size - 1))
                )
    return img

def process_pixels(img, func):
    pixels = img.load()
    for y in range(img.height):
        for x in range(img.width):
            pixels[x, y] = func(*pixels[x, y])
    return img

def clamp(v):
    return max(0, min(255, int(v)))

# 23 Kodak Gold (Warm, slightly grainy 90s)
def apply_kodak_gold(img):
    img = process_pixels(img, lambda r, g, b: (
        clamp(r * 1.08 + 15),
        clamp(g * 1.03 + 5),
        clamp(b * 0.92 - 10)
    ))
    return ImageEnhance.Color(img).enhance(1.1)

# 24 Portra 400 (Soft, natural skin tones)
def apply_portra(img):
    img = process_pixels(img, lambda r, g, b: (
        clamp(r * 1.02),
        clamp(g * 1.01),
        clamp(b * 0.98)
    ))
    img = ImageEnhance.Contrast(img).enhance(0.95)
    return ImageEnhance.Color(img).enhance(0.9)

# 25 Fuji Velvia (Ultra-saturated landscape)
def apply_velvia(img):
    img = process_pixels(img, lambda r, g, b: (
        clamp(r * 1.05 - 5),
        clamp(g * 1.1 + 10),
        clamp(b * 1.1 + 10)
    ))
    img = ImageEnhance.Contrast(img).enhance(1.2)
    return ImageEnhance.Color(img).enhance(1.4)

# 26 Cinematic Teal & Orange
def apply_teal_orange(img):
    def teal_orange(r, g, b):
        luma = 0.2126 * r + 0.7152 * g + 0.0722 * b
        if luma > 128:
            # Highlights -> Orange
            return clamp(r * 1.2), clamp(g * 1.1), clamp(b * 0.8)
        else:
            # Shadows -> Teal
            return clamp(r * 0.8), clamp(g * 1.1), clamp(b * 1.3)
    return process_pixels(img, teal_orange)

# 27 Polaroid (Faded, green-yellow cast, soft contrast)
def apply_polaroid(img):
    img = process_pixels(img, lambda r, g, b: (
        clamp(r * 0.9 + 20),
        clamp(g * 0.95 + 30),
        clamp(b * 0.8 + 10)
    ))
    return ImageEnhance.Contrast(img).enhance(0.8)

# 28 Noir Film Classic (Deep blacks, silver tone B&W)
def apply_noir(img):
    def noir(r, g, b):
        luma = 0.299 * r + 0.587 * g + 0.114 * b
        # Increase contrast heavily
        luma = (luma - 128) * 1.5 + 128
        val = clamp(luma)
        # Slight silver-blue tint to highlights
        return val, val, clamp(val * 1.05)
    return process_pixels(img, noir)

# 29 Pastel Dream (Soft, low-contrast, light pastel tones)
def apply_pastel(img):
    img = process_pixels(img, lambda r, g, b: (
        clamp(r * 0.8 + 60),
        clamp(g * 0.85 + 50),
        clamp(b * 0.9 + 70)
    ))
    img = ImageEnhance.Contrast(img).enhance(0.7)
    return ImageEnhance.Color(img).enhance(0.8)

# 30 Moody Editorial (Dark, desaturated, high-contrast)
def apply_moody(img):
    img = process_pixels(img, lambda r, g, b: (
        clamp(r * 0.9 - 10),
        clamp(g * 0.9 - 10),
        clamp(b * 0.9 - 5)
    ))
    img = ImageEnhance.Contrast(img).enhance(1.3)
    return ImageEnhance.Color(img).enhance(0.6)


filters = {
    "lut_kodak_gold.png": apply_kodak_gold,
    "lut_portra_400.png": apply_portra,
    "lut_fuji_velvia.png": apply_velvia,
    "lut_cinematic_teal_orange.png": apply_teal_orange,
    "lut_polaroid.png": apply_polaroid,
    "lut_noir_classic.png": apply_noir,
    "lut_pastel_dream.png": apply_pastel,
    "lut_moody_editorial.png": apply_moody,
}

assets_dir = "android/app/src/main/assets/luts"
os.makedirs(assets_dir, exist_ok=True)

print("Generating 8 LUTs...")
for filename, func in filters.items():
    print(f"Generating {filename}...")
    base_lut = create_identity_lut()
    final_lut = func(base_lut)
    final_lut.save(os.path.join(assets_dir, filename))
print("Done!")
