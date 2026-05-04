# Basic location permission

The simplest possible Aagya integration: ask for fine location, react to the result.

## The flow

```kotlin
@Composable
fun LocationButton(onGranted: () -> Unit) {
    val controller = rememberPermissionController()
    val scope = rememberCoroutineScope()
    var label by remember { mutableStateOf("Use my location") }

    Button(onClick = {
        scope.launch {
            label = "Asking..."
            when (val result = controller.requestPermission(AppPermission.Location.Fine)) {
                is PermissionResult.Granted -> {
                    onGranted()
                    label = "Granted"
                }
                is PermissionResult.Denied -> {
                    label = if (result.canAskAgain) "Tap to try again" else "Open Settings"
                    if (!result.canAskAgain) controller.openAppSettings()
                }
                is PermissionResult.Cancelled -> {
                    label = "Use my location"
                }
                is PermissionResult.PolicyExhausted -> {
                    label = "Open Settings"
                    controller.openAppSettings()
                }
            }
        }
    }) {
        Text(label)
    }
}
```

## Pre-flight check (no prompt)

If you want to render UI conditional on the current state without prompting:

```kotlin
@Composable
fun MaybeMapButton() {
    val controller = rememberPermissionController()
    var status by remember { mutableStateOf<PermissionStatus>(PermissionStatus.NotDetermined) }

    LaunchedEffect(controller) {
        status = controller.checkPermissionStatus(AppPermission.Location.Fine)
    }

    when (val s = status) {
        PermissionStatus.NotDetermined -> RequestButton()
        PermissionStatus.Granted -> MapButton()
        is PermissionStatus.Denied -> if (s.canAskAgain) RetryButton() else SettingsButton()
        PermissionStatus.Restricted -> RestrictedNotice()
    }
}
```

## Manifests / Info.plist

=== "Android"

    ```xml
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    ```

=== "iOS"

    ```xml
    <key>NSLocationWhenInUseUsageDescription</key>
    <string>We use your location to show nearby stops.</string>
    ```

That is the complete integration. No service to register, no Application subclass, no
DI to wire up.
