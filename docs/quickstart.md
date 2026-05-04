# Quickstart

This guide gets a fresh KMP app from zero to "user can grant location" in five minutes.

## 1. Add the dependency

=== "Kotlin Multiplatform"

    ```kotlin title="shared/build.gradle.kts"
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("xyz.ksharma:aagya-data:0.1.0")
            }
        }
    }
    ```

=== "Android-only"

    ```kotlin title="app/build.gradle.kts"
    dependencies {
        implementation("xyz.ksharma:aagya-data:0.1.0")
    }
    ```

## 2. Declare the permission you'll request

Aagya does **not** declare permissions in its own manifest or `Info.plist`. The host app
must declare them:

=== "Android"

    ```xml title="AndroidManifest.xml"
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    ```

=== "iOS"

    ```xml title="Info.plist"
    <key>NSLocationWhenInUseUsageDescription</key>
    <string>We use your location to show nearby stops.</string>
    ```

## 3. Request the permission

```kotlin
@Composable
fun LocationButton(onGranted: () -> Unit) {
    val controller = rememberPermissionController()
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            when (val result = controller.requestPermission(AppPermission.Location.Fine)) {
                is PermissionResult.Granted -> onGranted()
                is PermissionResult.Denied -> {
                    if (!result.canAskAgain) controller.openAppSettings()
                }
                is PermissionResult.Cancelled,
                is PermissionResult.PolicyExhausted -> Unit
            }
        }
    }) {
        Text("Allow Location")
    }
}
```

That's the full integration. Run the app, tap the button, accept or deny. Tap again
after denying to see the second-attempt behavior, then a third time to see Aagya
route the user to system settings.

!!! tip "Why no setup code in `Application` / `App`?"
    Aagya doesn't need any. The Composable factory binds to the current `Activity` or
    `UIViewController` automatically. There is no global state to initialize.

## 4. Read the status anywhere

```kotlin
@Composable
fun LocationStatusLabel() {
    val controller = rememberPermissionController()
    var status by remember { mutableStateOf<PermissionStatus>(PermissionStatus.NotDetermined) }

    LaunchedEffect(controller) {
        status = controller.checkPermissionStatus(AppPermission.Location.Fine)
    }

    Text(
        when (val s = status) {
            PermissionStatus.NotDetermined -> "Tap to grant"
            PermissionStatus.Granted -> "Granted"
            is PermissionStatus.Denied -> if (s.canAskAgain) "Try again" else "Open Settings"
            PermissionStatus.Restricted -> "Restricted by device policy"
        }
    )
}
```

## Next steps

- [Permission model](concepts/permission-model.md), how Aagya represents the cross-platform state machine.
- [Storage policies](concepts/storage-policies.md), when to add a `PermissionStore` and which adapter to pick.
- [Recipes](recipes/basic-location-permission.md), copy-paste solutions for common scenarios.
