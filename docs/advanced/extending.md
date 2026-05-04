# Extending to new permissions

Adding a new permission family (Camera, Microphone, Notifications, ...) is a
non-breaking change in v0.x. Here is the full checklist.

## 1. Add the type to `AppPermission`

```kotlin title="state/src/commonMain/.../AppPermission.kt"
public sealed interface AppPermission {
    public val key: String

    public sealed interface Location : AppPermission { /* ... */ }

    public sealed interface Camera : AppPermission {
        public data object Default : Camera {
            override val key: String = "camera.default"
        }
    }
}
```

Choose a `key` string that follows the `<family>.<variant>` convention. Once
published, the `key` becomes part of the public contract, never change it.

## 2. Map it on Android

```kotlin title="data/src/androidMain/.../AndroidPermissionMapping.kt"
internal fun AppPermission.toAndroidPermissions(): List<String> = when (this) {
    AppPermission.Location.Fine -> listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    AppPermission.Location.Coarse -> listOf(Manifest.permission.ACCESS_COARSE_LOCATION)
    AppPermission.Camera.Default -> listOf(Manifest.permission.CAMERA)
}
```

The `when` is exhaustive over the sealed hierarchy, so the compiler will tell you
exactly which case you forgot.

## 3. Map it on iOS

iOS permissions are *not* uniform, each family has its own request API:

- `CLLocationManager.requestWhenInUseAuthorization()` for location.
- `AVCaptureDevice.requestAccess(for: .video)` for camera.
- `AVAudioSession.requestRecordPermission` for microphone.
- `UNUserNotificationCenter.requestAuthorization` for notifications.
- `PHPhotoLibrary.requestAuthorization` for photos.

`IosPermissionController` therefore needs a `when` over the family to dispatch to the
right system API:

```kotlin title="data/src/iosMain/.../IosPermissionController.kt"
override suspend fun requestPermission(permission: AppPermission): PermissionResult {
    return when (permission) {
        is AppPermission.Location -> requestLocation(permission)
        is AppPermission.Camera -> requestCamera()
        // etc.
    }
}
```

Each branch has its own delegate / completion handler bridge. Keep them as separate
files (`IosLocationFlow.kt`, `IosCameraFlow.kt`, ...) to keep the controller readable.

## 4. Add tests

Cover at least:

- Stable `key` values.
- The Android mapping (assert the manifest strings).
- The iOS dispatch path (assert the right system call is invoked given a stub).

## 5. Document

Add the new family to:

- `docs/concepts/permission-model.md` (mention it under the sealed hierarchy).
- A new recipe under `docs/recipes/` showing the simplest integration.
- The README's "Roadmap" section (move from "planned" to "supported").

## 6. Bump the version

A new family is a `MINOR` bump (`0.1.0` becomes `0.2.0` during 0.x, `1.0.0` becomes `1.1.0`
post-1.0). Tag the release, write the CHANGELOG entry, publish.

## What counts as "non-breaking"?

- Adding a new family or a new variant within a family.
- Adding new fields to `PermissionResult.Denied` with default values.

What counts as breaking:

- Changing an existing `key` string.
- Removing a family or variant.
- Changing the order of declared types in the sealed hierarchy (in theory; in
  practice Kotlin handles this fine).

Tag breaking changes for the next major bump.
