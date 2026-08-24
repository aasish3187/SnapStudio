import SwiftUI

struct TextToolView: View {
    @State private var text: String = "Tap to edit"
    @State private var position = CGPoint(x: 150, y: 150)
    
    var body: some View {
        GeometryReader { geometry in
            TextField("", text: $text)
                .textFieldStyle(PlainTextFieldStyle())
                .foregroundColor(.white)
                .font(.system(size: 32, weight: .bold))
                .shadow(color: .black, radius: 2)
                .position(position)
                .gesture(
                    DragGesture()
                        .onChanged { value in
                            position = value.location
                        }
                )
        }
    }
}
