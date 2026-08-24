import SwiftUI

struct GalleryView: View {
    @State private var images: [URL] = []
    
    let columns = [
        GridItem(.flexible()),
        GridItem(.flexible()),
        GridItem(.flexible())
    ]
    
    var body: some View {
        NavigationView {
            ScrollView {
                if images.isEmpty {
                    Text("No photos found.")
                        .padding()
                } else {
                    LazyVGrid(columns: columns, spacing: 8) {
                        ForEach(images, id: \.self) { url in
                            Rectangle()
                                .fill(Color.gray)
                                .aspectRatio(1, contentMode: .fit)
                                .overlay(Text(url.lastPathComponent).font(.caption).lineLimit(1))
                            // In real app: AsyncImage or custom Image loader
                        }
                    }
                    .padding()
                }
            }
            .navigationTitle("Gallery")
            .onAppear {
                loadImages()
            }
        }
    }
    
    private func loadImages() {
        let tempDir = FileManager.default.temporaryDirectory
        do {
            let files = try FileManager.default.contentsOfDirectory(at: tempDir, includingPropertiesForKeys: [.creationDateKey])
            images = files.filter { $0.lastPathComponent.starts(with: "SnapStudio-") && $0.pathExtension == "jpg" }
                .sorted {
                    let d1 = (try? $0.resourceValues(forKeys: [.creationDateKey]).creationDate) ?? Date.distantPast
                    let d2 = (try? $1.resourceValues(forKeys: [.creationDateKey]).creationDate) ?? Date.distantPast
                    return d1 > d2
                }
        } catch {
            print("Error loading images: \(error)")
        }
    }
}
