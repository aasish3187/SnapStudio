import SwiftUI

struct AdjustmentPanelView: View {
    @Binding var brightness: Double
    @Binding var contrast: Double
    @Binding var saturation: Double
    
    var body: some View {
        VStack(alignment: .leading) {
            Text(String(format: "Brightness: %.2f", brightness))
            Slider(value: $brightness, in: -1.0...1.0)
            
            Text(String(format: "Contrast: %.2f", contrast))
            Slider(value: $contrast, in: 0.0...2.0)
            
            Text(String(format: "Saturation: %.2f", saturation))
            Slider(value: $saturation, in: 0.0...2.0)
        }
        .padding()
        .background(Color.black.opacity(0.8))
        .foregroundColor(.white)
    }
}
