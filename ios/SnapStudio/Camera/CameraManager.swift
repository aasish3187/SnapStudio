import AVFoundation

class CameraManager: NSObject, ObservableObject {
    @Published var session = AVCaptureSession()
    @Published var isPermissionGranted = false
    @Published var capturedImageURL: URL?
    private let photoOutput = AVCapturePhotoOutput()

    override init() {
        super.init()
        checkPermission()
    }

    func checkPermission() {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            isPermissionGranted = true
            setupCamera()
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                DispatchQueue.main.async {
                    self?.isPermissionGranted = granted
                    if granted {
                        self?.setupCamera()
                    }
                }
            }
        default:
            isPermissionGranted = false
        }
    }

    private func setupCamera() {
        session.beginConfiguration()
        
        guard let videoDevice = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back),
              let videoDeviceInput = try? AVCaptureDeviceInput(device: videoDevice) else {
            session.commitConfiguration()
            return
        }

        if session.canAddInput(videoDeviceInput) {
            session.addInput(videoDeviceInput)
        }

        if session.canAddOutput(photoOutput) {
            session.addOutput(photoOutput)
        }

        session.commitConfiguration()

        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            self?.session.startRunning()
        }
    }

    func takePhoto() {
        let settings = AVCapturePhotoSettings()
        photoOutput.capturePhoto(with: settings, delegate: self)
    }
}

extension CameraManager: AVCapturePhotoCaptureDelegate {
    func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        guard let data = photo.fileDataRepresentation() else { return }
        
        let url = FileManager.default.temporaryDirectory.appendingPathComponent("SnapStudio-\(UUID().uuidString).jpg")
        do {
            try data.write(to: url)
            print("Photo saved to: \(url.path)")
            DispatchQueue.main.async {
                self.capturedImageURL = url
            }
        } catch {
            print("Failed to save photo: \(error)")
        }
    }
}
