# Crash handling

Aagya is designed so that the host app never has to wrap calls in `try/catch`. The
public API does not throw across the bridge. This page documents what *can* go wrong
and how Aagya translates it.

## What can throw

Exactly one thing: **construction**. If `rememberPermissionController()` is invoked
inside a Composable whose `Context` chain has no `ComponentActivity`, Aagya throws
`IllegalStateException` with a pointer to the docs.

This is intentional, it is a developer error, not a runtime condition. Wrapping it
in a sealed result would just hide a misuse and lead to silent no-ops in production.

## What never throws

Every `suspend` and synchronous method returns a sealed value. Failure modes:

| Symptom | Translation |
|---|---|
| `Activity` is null when launching the picker | `PermissionResult.Denied(canAskAgain=true, reason=PresentationUnavailable)` |
| `ActivityResultLauncher.launch(...)` throws | `PermissionResult.Denied(canAskAgain=true, reason=PlatformError)`, logged |
| `CLLocationManager.requestWhenInUseAuthorization` throws | same as above, logged |
| `NSURL.URLWithString(UIApplicationOpenSettingsURLString)` is null | `openAppSettings()` no-ops, logged at warn |
| `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` activity not found | `openAppSettings()` no-ops, logged at error |
| Aagya policy cap reached | `PermissionResult.PolicyExhausted` |

## Wiring up your logger

By default Aagya is silent (`NoOpLogger`). To see diagnostics, plug in your logger:

```kotlin
class KermitAagyaLogger : Logger {
    private val log = co.touchlab.kermit.Logger.withTag("aagya")
    override fun debug(message: String) = log.d { message }
    override fun info(message: String) = log.i { message }
    override fun warn(message: String, error: Throwable?) = log.w(error) { message }
    override fun error(message: String, error: Throwable?) = log.e(error) { message }
}

val controller = rememberPermissionController(logger = KermitAagyaLogger())
```

Good logger hooks for production:

- Crashlytics non-fatal report on `error(...)` calls.
- Forward `info(...)` to your analytics pipeline tagged as `permissionFlow`.
- Skip `debug(...)` in release builds.

## What "crash-safe" does not mean

Aagya does not handle every conceivable failure mode of `Activity` or
`CLLocationManager`, only the ones it can plausibly translate. If `CLLocationManager`
itself crashes the process (it shouldn't, but in theory), Aagya can't catch that.

The contract is: **the public Aagya API will not be the cause of a crash in your app**.
Anything beyond that is the platform's responsibility.

## Threading

If a host app calls a `suspend` Aagya method from `Dispatchers.Default` and Aagya
needs to touch `Activity`, it marshals to `Dispatchers.Main` internally. You don't
need to switch dispatchers.
