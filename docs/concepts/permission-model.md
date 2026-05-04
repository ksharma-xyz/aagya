# Permission model

Aagya represents permissions as a sealed hierarchy. v0.1 ships only the `Location`
family but the shape is designed for a dozen more.

## The sealed hierarchy

```kotlin
sealed interface AppPermission {
    val key: String

    sealed interface Location : AppPermission {
        data object Fine : Location
        data object Coarse : Location
    }

    // Reserved for future minor releases:
    // sealed interface Camera
    // sealed interface Microphone
    // sealed interface Notifications
    // sealed interface Photos { Read; ReadWrite }
    // sealed interface Calendar
    // sealed interface Contacts
}
```

Each leaf is a `data object` so equality, hashing, and `toString` are free. The `key`
field is the stable string identifier that storage adapters and telemetry use; it is
**part of the public contract** and must not change.

## Why a sealed hierarchy

Two reasons:

1. **Exhaustive `when` for callers.** Code that explicitly handles every permission
   gets compiler-checked completeness.
2. **Closed mapping for the library.** Every leaf has exactly one definition for
   "what does this mean on Android" and "what does this mean on iOS". Aagya can
   guarantee correct platform mappings because it owns the universe of values.

## Adding a new permission family

Adding a family in v0.x is a non-breaking change because consumers pattern-match against
specific families (e.g. `is AppPermission.Location`), not the closed universe of all
permissions.

```kotlin
sealed interface Camera : AppPermission {
    data object Default : Camera {
        override val key: String = "camera.default"
    }
}
```

Then in the platform layer:

```kotlin
// Android
internal fun AppPermission.toAndroidPermissions(): List<String> = when (this) {
    AppPermission.Location.Fine -> listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    AppPermission.Location.Coarse -> listOf(Manifest.permission.ACCESS_COARSE_LOCATION)
    AppPermission.Camera.Default -> listOf(Manifest.permission.CAMERA)
}
```

iOS gets a parallel mapping in `IosPermissionMapping.kt`.

See [Extending to new permissions](../advanced/extending.md) for the full checklist.

## Status vs Result

Aagya distinguishes between **reading** the permission state and **acting** on it.

| Question | Type | Returned by |
|---|---|---|
| What state is the permission in *right now*? | `PermissionStatus` | `checkPermissionStatus(...)` |
| What did the user just do? | `PermissionResult` | `requestPermission(...)` |

```kotlin
sealed interface PermissionStatus {
    data object NotDetermined
    data object Granted
    data class Denied(val canAskAgain: Boolean)
    data object Restricted   // iOS parental controls etc.
}

sealed interface PermissionResult {
    data object Granted
    data class Denied(val canAskAgain: Boolean, val reason: DenialReason)
    data object Cancelled
    data object PolicyExhausted
}
```

The `canAskAgain` field on `Denied` is the most important value in the library. It is
the only signal your UI needs to decide between showing "Allow" (which will prompt
again) and "Open Settings" (which will route the user to manual grant).

## The DenialReason field

`PermissionResult.Denied` carries a `reason` for diagnostics. It is **not** intended
to drive UX; `canAskAgain` is. Reasons:

- `UserDenied`, the user tapped "Don't Allow".
- `SystemSuppressed`, the OS short-circuited the dialog because the permission is
  already permanently denied.
- `PresentationUnavailable`, Aagya could not present the dialog (no `Activity`, no
  `UIViewController`).
- `PlatformError`, an unexpected platform exception. Inspect logs.

If you find yourself wanting to branch UX on `reason`, that is a smell, open an issue
so we can talk about extending `canAskAgain` instead.
