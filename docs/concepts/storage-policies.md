# Storage policies

Aagya tries hard not to need storage. For most apps, the answer is "don't bother".
This doc explains when you do bother and how to choose.

## When you don't need storage

The system gives you the truth for free:

- **Android**: `shouldShowRequestPermissionRationale` plus `checkSelfPermission` is
  enough to compute `canAskAgain` correctly. The OS already enforces the 2-prompt cap.
- **iOS**: `CLAuthorizationStatus` is fully authoritative. Once non-`notDetermined`,
  the dialog will never appear again.

If you are happy letting the OS run the show, you need exactly zero storage. The
default `PermissionPolicy.Default` works without any persistent state.

## When you do need storage

You want **stricter than system** behavior. The most common case is: "ask only once on
Android too, like iOS, so the prompt never feels naggy."

```kotlin
val controller = rememberPermissionController(
    policy = PermissionPolicy(
        maxRequestsAndroid = 1,
        maxRequestsIos = 1,
        store = rememberDataStorePermissionStore(),
    ),
)
```

When `maxRequestsAndroid < 2`, Aagya needs to remember **across app launches** how many
times you have prompted. That is the storage layer's job.

## Picking a storage adapter

| Adapter | Module | Backed by | When to use |
|---|---|---|---|
| **In-memory** | (built into `aagya-state`) | `mutableMapOf` + `Mutex` | Tests, throwaway sessions, default. Resets on every launch. |
| **DataStore** | `aagya-store-datastore` | Jetpack DataStore Preferences | Most Android apps. |
| **NSUserDefaults** | `aagya-store-userdefaults` | `NSUserDefaults` | Most iOS apps. |
| **Custom** | your code | anything | When you already have a preference layer (Sandook, MMKV, encrypted prefs, etc.). |

## Implementing a custom store

The interface is intentionally small:

```kotlin
interface PermissionStore {
    suspend fun getRequestCount(permission: AppPermission): Int
    suspend fun incrementRequestCount(permission: AppPermission)
    suspend fun reset(permission: AppPermission)
}
```

Contract:

- All methods are safe to call from any dispatcher.
- Implementations must be safe under concurrent reads and writes for the same key.
- Methods must not throw. Treat any failure as "0" and log internally if needed.

Example wrapping an existing `SharedPreferences` layer:

```kotlin
class SharedPrefsPermissionStore(
    private val prefs: SharedPreferences,
) : PermissionStore {

    override suspend fun getRequestCount(p: AppPermission): Int =
        prefs.getInt(key(p), 0)

    override suspend fun incrementRequestCount(p: AppPermission) {
        prefs.edit { putInt(key(p), prefs.getInt(key(p), 0) + 1) }
    }

    override suspend fun reset(p: AppPermission) {
        prefs.edit { remove(key(p)) }
    }

    private fun key(p: AppPermission) = "aagya.requestCount.${p.key}"
}
```

## Two-platform stores

If you have a cross-platform preference layer (for example
[KStore](https://github.com/xxfast/KStore) or your own), implement `PermissionStore`
in your shared module and pass the same instance into every `PermissionPolicy`.

## Resetting

Both `PermissionStore.reset(...)` and the system permission state are independent.
Resetting the store will let Aagya prompt again, but **only if the OS-level state still
allows it**. On iOS, once denied at the OS level, no amount of resetting Aagya's store
will bring back the dialog. The "Open Settings" affordance is the only path forward.

This is a feature, not a bug. The system enforcement layer protects users from apps
that try to ask 50 times.
