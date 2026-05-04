import SwiftUI
import CoreLocation

/// Minimal driver showing how a SwiftUI host can talk to Aagya's `IosPermissionController`.
///
/// The simplest path is to invoke the Compose factory `rememberPermissionController()`
/// from a Compose Multiplatform iOS UI hosted via `ComposeUIViewController`. For a
/// SwiftUI-only host you can either call into the shared module's Kotlin API directly
/// or use `CLLocationManager` here and reuse the rest of Aagya's state types from your
/// shared module.
///
/// This sample demonstrates the SwiftUI-only path and bridges the resulting state into
/// Aagya's `PermissionStatus` for parity with the Android sample.
struct ContentView: View {
    @StateObject private var model = SampleViewModel()

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 16) {
                Text("Aagya iOS Sample")
                    .font(.title2)
                    .bold()

                Text("Status: \(model.statusDescription)")

                if let last = model.lastResult {
                    Text("Last result: \(last)")
                        .font(.callout)
                }

                Button("Request Location") {
                    model.requestLocation()
                }
                .buttonStyle(.borderedProminent)

                Button("Open Settings") {
                    model.openSettings()
                }
                .buttonStyle(.bordered)

                Spacer()
            }
            .padding()
            .navigationTitle("Aagya")
        }
    }
}

@MainActor
final class SampleViewModel: NSObject, ObservableObject, CLLocationManagerDelegate {
    @Published var statusDescription: String = "NotDetermined"
    @Published var lastResult: String?

    private let manager = CLLocationManager()

    override init() {
        super.init()
        manager.delegate = self
        statusDescription = describe(manager.authorizationStatus)
    }

    func requestLocation() {
        switch manager.authorizationStatus {
        case .notDetermined:
            manager.requestWhenInUseAuthorization()
        case .denied, .restricted:
            lastResult = "Denied (canAskAgain=false)"
        case .authorizedWhenInUse, .authorizedAlways:
            lastResult = "Granted"
        @unknown default:
            lastResult = "Unknown"
        }
    }

    func openSettings() {
        guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
        UIApplication.shared.open(url)
    }

    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            self.statusDescription = self.describe(manager.authorizationStatus)
            switch manager.authorizationStatus {
            case .authorizedWhenInUse, .authorizedAlways:
                self.lastResult = "Granted"
            case .denied:
                self.lastResult = "Denied (canAskAgain=false)"
            default:
                break
            }
        }
    }

    private func describe(_ status: CLAuthorizationStatus) -> String {
        switch status {
        case .notDetermined: return "NotDetermined"
        case .denied: return "Denied(canAskAgain=false)"
        case .restricted: return "Restricted"
        case .authorizedWhenInUse: return "Granted (when in use)"
        case .authorizedAlways: return "Granted (always)"
        @unknown default: return "Unknown"
        }
    }
}
