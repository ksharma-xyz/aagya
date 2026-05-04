<div align="center">

# 🪔 Aagya

**आज्ञा**, Sanskrit for *permission, consent*.

A small, opinionated **Kotlin Multiplatform** permission library for Android and iOS.
Built so you stop reimplementing the same `shouldShowRequestPermissionRationale` /
`CLAuthorizationStatus` dance in every app.

[![Maven Central](https://img.shields.io/maven-central/v/xyz.ksharma/aagya-state?style=flat-square&label=maven%20central)](https://central.sonatype.com/artifact/xyz.ksharma/aagya-state)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-2.3.10-7F52FF.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Platforms](https://img.shields.io/badge/platforms-Android%20%7C%20iOS-lightgrey.svg?style=flat-square)](#)

[Documentation](https://ksharma-xyz.github.io/aagya) ·
[Quickstart](#quickstart) ·
[Recipes](https://ksharma-xyz.github.io/aagya/recipes/) ·
[Sample apps](sample-android)

</div>

---

## Why Aagya

Permissions on Android and iOS are not the same shape:

- **Android** lets you ask twice; after that the OS suppresses the dialog and you must send the user to Settings.
- **iOS** lets you ask once; after the first answer the system never shows the dialog again.

Most libraries paper over this with the lowest common denominator (one ask, ever) and force you to pick a storage strategy. Aagya does neither.

It exposes the *real* state machine:

```
NotDetermined
    becomes Granted                          (user allows)
    becomes Denied(canAskAgain = true)       (request again)
    becomes Denied(canAskAgain = false)      (open Settings)
```

…and lets you decide if you want to layer a stricter app policy on top (with your storage of choice).

## Highlights

- **Stateless by default.** Uses `shouldShowRequestPermissionRationale` on Android and `CLAuthorizationStatus` on iOS as the source of truth. Zero storage required for the common case.
- **Bring Your Own Storage** when you want a stricter policy. Optional `:store-datastore` (Android) and `:store-userdefaults` (iOS) adapters ship in the box.
- **Sealed permission model** ready for Camera, Microphone, Notifications, Photos. Adding new types in v0.x is a non-breaking change.
- **Crash-safe**. Every public API returns a sealed result. Nothing throws across the bridge.
- **Compose-native**, no Compose dep leakage. `rememberPermissionController()` for Compose users; plain interface for non-Compose KMP code.
- **DI-agnostic.** Optional `:di-koin` module if you use Koin. Otherwise, build it yourself with `rememberPermissionController()`.

## Quickstart

### Add the dependency

```kotlin
// build.gradle.kts (KMP shared module)
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("xyz.ksharma:aagya-data:0.1.0")
        }
    }
}
```

### Request a permission

```kotlin
@Composable
fun LocationButton() {
    val controller = rememberPermissionController()
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            when (val result = controller.requestPermission(AppPermission.Location.Fine)) {
                is PermissionResult.Granted -> startLocationFlow()
                is PermissionResult.Denied -> if (!result.canAskAgain) controller.openAppSettings()
                is PermissionResult.Cancelled -> Unit
            }
        }
    }) {
        Text("Use my location")
    }
}
```

That's it. No singletons, no Application subclass to extend, no storage to set up.

### Want a stricter "ask once" policy?

```kotlin
val controller = rememberPermissionController(
    policy = PermissionPolicy(
        maxRequestsAndroid = 1,
        maxRequestsIos = 1,
        store = rememberDataStorePermissionStore(),
    ),
)
```

See [recipes](https://ksharma-xyz.github.io/aagya/recipes/) for stricter policies, custom storage, and DI integration.

## Modules

| Artifact | Purpose | Required? |
|---|---|---|
| `xyz.ksharma:aagya-state` | Pure-Kotlin types: `AppPermission`, `PermissionStatus`, `PermissionResult`, `PermissionPolicy`. | yes |
| `xyz.ksharma:aagya-data` | `PermissionController` interface and Android/iOS implementations. | yes |
| `xyz.ksharma:aagya-store-datastore` | Android `PermissionStore` backed by Jetpack DataStore. | optional |
| `xyz.ksharma:aagya-store-userdefaults` | iOS `PermissionStore` backed by `NSUserDefaults`. | optional |
| `xyz.ksharma:aagya-di-koin` | Koin module factory if you use Koin. | optional |

## Supported platforms

- **Android** API 28+ (Android 7.0 Nougat)
- **iOS** 15.3+ (`iosArm64`, `iosSimulatorArm64`)

JVM and JS targets may follow if there is real demand. Open an issue.

## Roadmap

`v0.1` ships only `Location.Fine` and `Location.Coarse`. The sealed hierarchy already
includes stubs for these so adding them is non-breaking:

- `Camera`
- `Microphone`
- `Notifications`
- `Photos` (read / read-write split for iOS 14+)
- `Calendar`, `Contacts`
- Background location

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). PRs welcome. Please open an issue before any
non-trivial work so we can agree on the shape of the API.

## License

[Apache 2.0](LICENSE). Use it however you want.

---

<sub>Aagya is named for आज्ञा, the Sanskrit word for *permission* / *command*. The library
gives you a clean way to ask the user for theirs.</sub>
