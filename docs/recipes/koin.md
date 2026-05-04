# Koin DI integration

Aagya is DI-agnostic, but if you use Koin in your app the optional `aagya-di-koin`
module wires the moving parts up cleanly.

## Add the optional dep

```kotlin
implementation("io.github.ksharma-xyz:aagya-di-koin:0.1.0")
```

## Register the module

```kotlin
startKoin {
    modules(
        aagyaModule(
            policy = PermissionPolicy(
                maxRequestsAndroid = 1,
                maxRequestsIos = 1,
            ),
            logger = MyKermitLogger(),
            storeProvider = { MyAppPermissionStore(prefs = get()) },
        ),
    )
}
```

`aagyaModule` registers:

- `PermissionPolicy` as a singleton
- `Logger` as a singleton
- `PermissionStore` as a singleton

The controller itself stays Composable-bound because it needs platform context. You
inject the policy and logger from Koin and pass them into `rememberPermissionController`:

```kotlin
@Composable
fun MyScreen() {
    val policy: PermissionPolicy = koinInject()
    val logger: Logger = koinInject()
    val controller = rememberPermissionController(policy = policy, logger = logger)
    // ...
}
```

## Why not register the controller too?

The `PermissionController` carries platform context (`Activity` on Android,
`UIViewController` indirectly on iOS). Registering it as a Koin singleton would either
hold onto a stale reference after configuration changes or require a `factory` /
`scope` setup for every call site. Compose's `remember` already solves this; Koin
registration would only add a layer.
