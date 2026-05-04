# Aagya iOS Sample

This sample exists primarily as a Swift-side reference of how the iOS host calls into
Aagya. The Kotlin-Multiplatform–generated framework is consumed identically to any
other shared KMP module.

## Run it

The recommended path is to drive Aagya from a shared KMP UI module via
`ComposeUIViewController { /* ... */ }` and call `rememberPermissionController()` there.
The flow is identical to the Android sample.

For a SwiftUI-only host, see `ContentView.swift`, it shows the equivalent direct call
into `CLLocationManager`. The translation layer is small because Aagya intentionally
mirrors the system semantics.

## Setup

1. Open `iosApp/iosApp.xcodeproj` (you will need to create the Xcode project locally, this
   sample ships only the Swift sources, `Info.plist`, and a `README` to keep the repo small
   and avoid checking in machine-specific Xcode metadata).
2. Add `iosApp/AagyaSampleApp.swift` and `iosApp/ContentView.swift` to the target.
3. Use `Info.plist` from this folder.
4. If integrating Aagya through a shared KMP module, add the framework via SPM or CocoaPods
   per JetBrains' [Compose Multiplatform iOS integration guide](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-ios-storyboard.html).
