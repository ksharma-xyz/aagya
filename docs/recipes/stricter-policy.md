# Stricter "ask once" policy

The default policy lets Android prompt twice and iOS once, matching system behavior.
If you want **one prompt, ever, on both platforms**, configure a `PermissionPolicy`
with a persistent store.

## The configuration

```kotlin
val controller = rememberPermissionController(
    policy = PermissionPolicy(
        maxRequestsAndroid = 1,
        maxRequestsIos = 1,
        store = rememberDataStorePermissionStore(), // Android
    ),
)
```

On iOS, swap the store factory:

```kotlin
PermissionPolicy(
    maxRequestsAndroid = 1,
    maxRequestsIos = 1,
    store = UserDefaultsPermissionStore(),
)
```

In a shared KMP module you can wire both up via expect/actual or with platform-specific
factories.

## What happens

After the first call to `requestPermission(...)`:

1. Aagya records the prompt in the store (`incrementRequestCount`).
2. Subsequent calls hit the policy gate first and return
   `PermissionResult.PolicyExhausted` immediately if the count has reached `1`.
3. The system prompt is never shown a second time.

The user's only path forward is **Settings**.

## When `PolicyExhausted` lands, route to Settings

```kotlin
when (val result = controller.requestPermission(AppPermission.Location.Fine)) {
    is PermissionResult.Granted -> ...
    is PermissionResult.Denied -> if (!result.canAskAgain) controller.openAppSettings()
    is PermissionResult.PolicyExhausted -> controller.openAppSettings()
    is PermissionResult.Cancelled -> Unit
}
```

## Trade-offs

Stricter policies are safer for the user's tolerance budget but harsher for retention.
You only get one shot to convince the user. Treat the prompt as a high-signal moment:

- Surface the prompt only when the user has just tapped a feature that needs it
  ("Show me nearby stops"), not on a cold app launch.
- Lead with copy that explains *why*. The system dialog cannot do this for you on
  Android. iOS uses your `NSLocationWhenInUseUsageDescription` string.

## Resetting the count

For testing or "reset onboarding" UX:

```kotlin
val store: PermissionStore = rememberDataStorePermissionStore()

LaunchedEffect(Unit) {
    store.reset(AppPermission.Location.Fine)
}
```

Note that resetting the count does **not** restore the system-level prompt. iOS will
still refuse to show a dialog if the user previously denied. The "Open Settings"
affordance remains the canonical recovery path.
