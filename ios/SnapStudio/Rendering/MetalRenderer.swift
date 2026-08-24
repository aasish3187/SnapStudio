import MetalKit
import AVFoundation

class MetalRenderer: NSObject, MTKViewDelegate {
    var device: MTLDevice!
    var commandQueue: MTLCommandQueue!
    var currentFilter: Int = 0 // 0: None, 1: Grayscale, 2: Sepia
    
    var currentTexture: MTLTexture?
    
    init(metalView: MTKView) {
        super.init()
        self.device = metalView.device
        self.commandQueue = device.makeCommandQueue()
    }
    
    func draw(in view: MTKView) {
        guard let drawable = view.currentDrawable,
              let texture = currentTexture,
              let commandBuffer = commandQueue.makeCommandBuffer() else {
            return
        }
        
        // TODO: Create a render pipeline state dynamically based on `currentFilter`
        // TODO: Bind `texture` to fragment shader
        // TODO: Draw primitives (fullscreen quad)
        
        commandBuffer.present(drawable)
        commandBuffer.commit()
    }
    
    func mtkView(_ view: MTKView, drawableSizeWillChange size: CGSize) { }
}
