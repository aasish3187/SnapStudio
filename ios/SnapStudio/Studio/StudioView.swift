import SwiftUI

struct StudioView: View {
    let imageURL: URL
    @Environment(\.presentationMode) var presentationMode
    
    @State private var brightness: Double = 0.0
    @State private var contrast: Double = 1.0
    @State private var saturation: Double = 1.0
    
    @State private var isShareSheetPresented = false
    
    @State private var activeMode = "Adjust"
    
    var body: some View {
        VStack {
            HStack {
                Button("Close") {
                    presentationMode.wrappedValue.dismiss()
                }
                .padding()
                .accessibilityLabel("Close Editor")
                Spacer()
                Button("Undo") {
                    // C++ interop undo
                }.padding()
                .accessibilityLabel("Undo Edit")
                Button("Redo") {
                    // C++ interop redo
                }.padding()
                .accessibilityLabel("Redo Edit")
                Button("Save/Share") {
                    ExportHelper.flattenAndExport(inputURL: imageURL) { exportedURL in
                        // We would use exportedURL, but we just trigger the sheet for now.
                        // The sheet uses imageURL as defined in the state.
                        isShareSheetPresented = true
                    }
                }
                .padding()
                .accessibilityLabel("Save or Share Image")
            }
            .sheet(isPresented: $isShareSheetPresented) {
                ShareSheet(items: [imageURL])
            }
            
            Spacer()
            
            // Preview Image
            ZStack {
                Text("Preview of: \(imageURL.lastPathComponent)")
                    .padding()
                
                if activeMode == "Crop" {
                    CropToolView()
                } else if activeMode == "Draw" {
                    DrawToolView()
                } else if activeMode == "Text" {
                    TextToolView()
                }
            }
            
            Spacer()
            
            if activeMode == "Adjust" {
                AdjustmentPanelView(brightness: $brightness, contrast: $contrast, saturation: $saturation)
            }
            
            HStack {
                Button("Adjust") { activeMode = "Adjust" }
                Spacer()
                Button("Crop") { activeMode = "Crop" }
                Spacer()
                Button("Draw") { activeMode = "Draw" }
                Spacer()
                Button("Text") { activeMode = "Text" }
            }
            .padding()
        }
    }
}
