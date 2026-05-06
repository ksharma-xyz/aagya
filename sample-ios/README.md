# Aagya iOS Sample

A native iOS app that hosts the **shared Compose Multiplatform** sample from
the `:sample` module. Same UI as the Android sample — written once in Kotlin,
rendered on iOS via Compose Multiplatform.

## Run it

1. Open the Xcode project:
   ```bash
   open iosApp/iosApp.xcodeproj
   ```
2. Pick a Simulator (iPhone 15 Pro or similar) or a connected device.
3. Hit ⌘R.

The build phase invokes `./gradlew :sample:embedAndSignAppleFrameworkForXcode`
to compile the shared Compose code into a `.framework`, embeds it in the app,
and Swift's `ContentView.swift` wraps the resulting `UIViewController` from
the Kotlin side via `IosEntryKt.SampleViewController()`.

## What you should see

- The same single screen as the Android sample: a status orb that morphs
  through `NotDetermined → Granted → Denied(canAskAgain) → Denied(permanent)`,
  a tier selector (OS-default vs persistent NSUserDefaults-backed), a primary
  action button that morphs label/colour, and an activity log.
- Tapping the primary button either surfaces the system permission dialog or
  routes to Settings (when the OS-level state is permanently denied).

## Troubleshooting

- **"Cannot find 'IosEntryKt' in scope"**: the framework wasn't built. Force
  the gradle build phase to run by cleaning (⇧⌘K) and rebuilding (⌘B).
- **"Failed to find or build framework AagyaSample"**: run
  `./gradlew :sample:linkDebugFrameworkIosSimulatorArm64` from the repo root
  by hand and look at the gradle output for the real error.
- **Code signing errors**: set `TEAM_ID` in
  `iosApp/Configuration/Config.xcconfig` to your Apple Developer team, or
  switch to a Simulator (which doesn't need signing).

## Configuration

- Bundle ID, marketing version, deployment target: `iosApp/Configuration/Config.xcconfig`.
- App Info plist (with `NSLocationWhenInUseUsageDescription`): `iosApp/Info.plist`.
