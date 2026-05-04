# Custom storage adapter

Use this when you already have a preference layer in your app (Sandook, MMKV, encrypted
prefs, a key-value store via Ktor, etc.) and don't want to add another one.

## Implement `PermissionStore`

```kotlin
class MyAppPermissionStore(
    private val prefs: MyAppPrefs,
) : PermissionStore {

    override suspend fun getRequestCount(p: AppPermission): Int =
        prefs.getInt("aagya.requestCount.${p.key}", default = 0)

    override suspend fun incrementRequestCount(p: AppPermission) {
        val key = "aagya.requestCount.${p.key}"
        prefs.setInt(key, prefs.getInt(key, default = 0) + 1)
    }

    override suspend fun reset(p: AppPermission) {
        prefs.remove("aagya.requestCount.${p.key}")
    }
}
```

## Pass it into the policy

```kotlin
@Composable
fun App() {
    val store = remember { MyAppPermissionStore(prefs = MyAppPrefs.get()) }
    val controller = rememberPermissionController(
        policy = PermissionPolicy(
            maxRequestsAndroid = 1,
            maxRequestsIos = 1,
            store = store,
        ),
    )
    // ...
}
```

## Contract reminders

- All methods must be safe to call from any coroutine.
- Implementations must be safe under concurrent reads and writes for the same key.
- Methods must not throw across the public boundary. If your underlying store can
  fail, catch the exception, log it, and treat the read as `0`.

## Choosing a key prefix

The `permission.key` field is intentionally short and stable (`location.fine`,
`location.coarse`). Prefix it with something namespace-y like `aagya.requestCount.`
to avoid collisions with your app's other preference keys.

## Testing your store

Aagya's own `InMemoryPermissionStore` is a good reference implementation. Run the
same correctness checks against your store:

```kotlin
@Test
fun newStoreReportsZero() = runTest {
    val store = MyAppPermissionStore(prefs = TestPrefs())
    assertEquals(0, store.getRequestCount(AppPermission.Location.Fine))
}

@Test
fun incrementCountsForOnePermissionOnly() = runTest {
    val store = MyAppPermissionStore(prefs = TestPrefs())
    store.incrementRequestCount(AppPermission.Location.Fine)
    store.incrementRequestCount(AppPermission.Location.Fine)
    assertEquals(2, store.getRequestCount(AppPermission.Location.Fine))
    assertEquals(0, store.getRequestCount(AppPermission.Location.Coarse))
}
```
