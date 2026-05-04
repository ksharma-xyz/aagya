---
hide:
  - navigation
  - toc
---

<div class="aagya-hero" markdown>

<div class="sanskrit">आज्ञा</div>

# Aagya

Permissions for Kotlin Multiplatform apps. Honest about platform differences, quiet by default, ready when you need more.

[Get started](quickstart.md){ .md-button .md-button--primary } [GitHub](https://github.com/ksharma-xyz/aagya){ .md-button }

</div>

## What you get

<ul class="tag-pills">
  <li>Sealed permission model</li>
  <li>Stateless by default</li>
  <li>BYOS storage</li>
  <li>Crash-safe API</li>
  <li>Compose-native</li>
  <li>Apache 2.0</li>
</ul>

Aagya is a small KMP library that gives you a uniform permission API across Android and iOS without flattening their differences.

- The default flow uses `shouldShowRequestPermissionRationale` on Android and `CLAuthorizationStatus` on iOS as the source of truth. **No storage required.**
- A `PermissionStatus.Denied(canAskAgain: Boolean)` field tells your UI exactly when to show "Allow" vs "Open Settings".
- Optional `PermissionPolicy` lets you layer stricter rules (for example "ask once, ever") with the storage adapter of your choice.

## Quickstart

Add the dependency:

```kotlin
implementation("io.github.ksharma-xyz:aagya-data:0.1.0")
```

Use it:

```kotlin
@Composable
fun LocationPrompt() {
    val controller = rememberPermissionController()
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            when (val r = controller.requestPermission(AppPermission.Location.Fine)) {
                is PermissionResult.Granted -> startLocationFlow()
                is PermissionResult.Denied -> if (!r.canAskAgain) controller.openAppSettings()
                is PermissionResult.Cancelled,
                is PermissionResult.PolicyExhausted -> Unit
            }
        }
    }) { Text("Use my location") }
}
```

Continue with the [Quickstart guide](quickstart.md) for setup details, or skip ahead to [recipes](recipes/basic-location-permission.md) for common patterns.

## Why "Aagya"?

**Aagya** (आज्ञा) is the Sanskrit word for *permission* or *consent*. The library does what its name says.

It is the first of two small KMP libraries published under [`xyz.ksharma`](https://github.com/ksharma-xyz). The companion library, [Dhruva](https://github.com/ksharma-xyz/dhruva), handles location.
