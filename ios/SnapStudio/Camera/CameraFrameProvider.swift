import AVFoundation
import CoreVideo
import Metal

class CameraFrameProvider: NSObject, AVCaptureVideoDataOutputSampleBufferDelegate {
    private var textureCache: CVMetalTextureCache!
    var onFrameAvailable: ((MTLTexture) -> Void)?
    
    init(device: MTLDevice) {
        CVMetalTextureCacheCreate(kCFAllocatorDefault, nil, device, nil, &textureCache)
    }
    
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }
        
        let width = CVPixelBufferGetWidth(pixelBuffer)
        let height = CVPixelBufferGetHeight(pixelBuffer)
        
        var cvTextureOut: CVMetalTexture?
        CVMetalTextureCacheCreateTextureFromImage(kCFAllocatorDefault,
                                                  textureCache,
                                                  pixelBuffer, nil,
                                                  .bgra8Unorm,
                                                  width, height, 0,
                                                  &cvTextureOut)
        
        guard let cvTexture = cvTextureOut,
              let mtlTexture = CVMetalTextureGetTexture(cvTexture) else { return }
        
        DispatchQueue.main.async {
            self.onFrameAvailable?(mtlTexture)
        }
    }
}
