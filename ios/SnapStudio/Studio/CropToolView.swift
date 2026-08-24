import SwiftUI

struct CropToolView: View {
    @State private var cropRect = CGRect(x: 100, y: 100, width: 200, height: 200)
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                Rectangle()
                    .stroke(Color.white, lineWidth: 2)
                    .frame(width: cropRect.width, height: cropRect.height)
                    .position(x: cropRect.midX, y: cropRect.midY)
                    .gesture(
                        DragGesture()
                            .onChanged { value in
                                // Stub: move the whole rect
                                cropRect.origin.x = value.location.x - cropRect.width / 2
                                cropRect.origin.y = value.location.y - cropRect.height / 2
                            }
                    )
            }
        }
    }
}
