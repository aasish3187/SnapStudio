import SwiftUI
import MetalKit
import AVFoundation

struct CameraPreviewView: UIViewRepresentable {
    let session: AVCaptureSession
    let currentFilter: Int
    
    func makeUIView(context: Context) -> MTKView {
        let mtkView = MTKView()
        mtkView.device = MTLCreateSystemDefaultDevice()
        mtkView.framebufferOnly = false
        mtkView.colorPixelFormat = .bgra8Unorm
        
        let renderer = MetalRenderer(metalView: mtkView)
        context.coordinator.renderer = renderer
        mtkView.delegate = renderer
        
        if let device = mtkView.device {
            context.coordinator.frameProvider = CameraFrameProvider(device: device)
            context.coordinator.frameProvider?.onFrameAvailable = { texture in
                renderer.currentTexture = texture
                mtkView.draw()
            }
        }
        
        // Start streaming frames
        let output = AVCaptureVideoDataOutput()
        output.setSampleBufferDelegate(context.coordinator.frameProvider, queue: DispatchQueue(label: "camera_frame_queue"))
        if session.canAddOutput(output) {
            session.addOutput(output)
        }
        
        return mtkView
    }
    
    func updateUIView(_ uiView: MTKView, context: Context) {
        context.coordinator.renderer?.currentFilter = currentFilter
    }
    
    func makeCoordinator() -> Coordinator {
        Coordinator()
    }
    
    class Coordinator {
        var renderer: MetalRenderer?
        var frameProvider: CameraFrameProvider?
    }
}
