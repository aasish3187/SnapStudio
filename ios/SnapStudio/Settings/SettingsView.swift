import SwiftUI

struct SettingsView: View {
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Capture Quality")) {
                    Text("High (Default)")
                }
                
                Section(header: Text("Default Filter")) {
                    Text("None")
                }
                
                Section {
                    Button("Clear Image Cache") {
                        // Clear cache
                    }
                    .foregroundColor(.red)
                }
            }
            .navigationTitle("Settings")
        }
    }
}
