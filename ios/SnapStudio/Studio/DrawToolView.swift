import SwiftUI

struct DrawToolView: View {
    @State private var currentLine = Path()
    @State private var lines: [Path] = []
    
    var body: some View {
        Canvas { context, size in
            for line in lines {
                context.stroke(line, with: .color(.red), lineWidth: 5)
            }
            context.stroke(currentLine, with: .color(.red), lineWidth: 5)
        }
        .gesture(
            DragGesture(minimumDistance: 0)
                .onChanged { value in
                    if currentLine.isEmpty {
                        currentLine.move(to: value.location)
                    } else {
                        currentLine.addLine(to: value.location)
                    }
                }
                .onEnded { value in
                    lines.append(currentLine)
                    currentLine = Path()
                }
        )
    }
}
