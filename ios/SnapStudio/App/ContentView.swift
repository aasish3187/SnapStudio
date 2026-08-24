import SwiftUI

struct ContentView: View {
    @StateObject private var cameraManager = CameraManager()
    @State private var selectedFilterIndex = 0
    @State private var showGallery = false
    @State private var showSettings = false
    let filters = ["Normal", "Grayscale", "Sepia"]
    
    var body: some View {
        ZStack {
            if cameraManager.isPermissionGranted {
                CameraPreviewView(session: cameraManager.session, currentFilter: selectedFilterIndex)
                    .ignoresSafeArea()
                
                VStack {
                    // Top Navigation
                    HStack {
                        Button("Gallery") {
                            showGallery = true
                        }
                        .padding()
                        .foregroundColor(.white)
                        .accessibilityLabel("Open Gallery")
                        
                        Spacer()
                        
                        Button("Settings") {
                            showSettings = true
                        }
                        .padding()
                        .foregroundColor(.white)
                        .accessibilityLabel("Open Settings")
                    }
                    
                    Spacer()
                    
                    // Filter Tray
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 16) {
                            ForEach(0..<filters.count, id: \.self) { index in
                                Text(filters[index])
                                    .foregroundColor(.white)
                                    .padding()
                                    .background(index == selectedFilterIndex ? Color.orange : Color.black.opacity(0.7))
                                    .cornerRadius(16)
                                    .onTapGesture {
                                        selectedFilterIndex = index
                                    }
                            }
                        }
                        .padding(.horizontal)
                    }
                    .padding(.bottom, 20)
                    
                    // Shutter
                    Button(action: {
                        cameraManager.takePhoto()
                    }) {
                        Circle()
                            .fill(Color.white)
                            .frame(width: 72, height: 72)
                            .overlay(
                                Circle()
                                    .stroke(Color.black.opacity(0.2), lineWidth: 2)
                            )
                    }
                    .padding(.bottom, 32)
                    .accessibilityLabel("Take Photo")
                }
                .sheet(isPresented: $showGallery) {
                    GalleryView()
                }
                .sheet(isPresented: $showSettings) {
                    SettingsView()
                }
                .fullScreenCover(item: Binding(
                    get: { cameraManager.capturedImageURL.map { IdentifiableURL(url: $0) } },
                    set: { cameraManager.capturedImageURL = $0?.url }
                )) { identifiableURL in
                    StudioView(imageURL: identifiableURL.url)
                }
            } else {
                Text("Camera permission is required.")
            }
        }
        .preferredColorScheme(.dark)
    }
    
    func getCoreVersion() -> String {
        return String(cString: snapstudio.SnapCore.getEngineVersion().c_str())
    }
}

struct IdentifiableURL: Identifiable {
    let id = UUID()
    let url: URL
}

#Preview {
    ContentView()
}
