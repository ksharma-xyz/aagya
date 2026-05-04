# Testing

Aagya provides two testing affordances out of the box, plus guidance for anything more.

## Unit tests for your screens

Use `PreviewPermissionController` to drive Composable tests without the Android or
iOS runtime in scope. It implements `PermissionController` directly and lets you fix
the answers it returns.

```kotlin
@Test
fun showsSettingsLinkWhenPermanentlyDenied() = runComposeUiTest {
    setContent {
        val controller = remember {
            PreviewPermissionController(
                defaultStatus = PermissionStatus.Denied(canAskAgain = false),
                defaultRequestResult = PermissionResult.Denied(
                    canAskAgain = false,
                    reason = DenialReason.SystemSuppressed,
                ),
            )
        }
        LocationButton(controller = controller, onGranted = {})
    }
    onNodeWithText("Open Settings").assertIsDisplayed()
}
```

For Composables that get the controller from `rememberPermissionController`, factor
the controller out as a parameter for testability:

```kotlin
@Composable
fun LocationButton(
    onGranted: () -> Unit,
    controller: PermissionController = rememberPermissionController(),
) { /* ... */ }
```

## Tests for your `PermissionStore`

If you implement a custom store, the contract is small enough to verify exhaustively.
Aagya's own tests are a good template:

```kotlin
@Test
fun newStoreReportsZero() = runTest {
    val store = MyStore()
    assertEquals(0, store.getRequestCount(AppPermission.Location.Fine))
}

@Test
fun incrementCountsForOnePermissionOnly() = runTest {
    val store = MyStore()
    store.incrementRequestCount(AppPermission.Location.Fine)
    store.incrementRequestCount(AppPermission.Location.Fine)
    assertEquals(2, store.getRequestCount(AppPermission.Location.Fine))
    assertEquals(0, store.getRequestCount(AppPermission.Location.Coarse))
}

@Test
fun resetClearsCount() = runTest {
    val store = MyStore()
    store.incrementRequestCount(AppPermission.Location.Fine)
    store.reset(AppPermission.Location.Fine)
    assertEquals(0, store.getRequestCount(AppPermission.Location.Fine))
}
```

## Integration tests

The platform behavior of `ActivityResultContracts` and `CLLocationManager` is hard to
mock meaningfully. Use the sample apps as a manual-test rig:

| Scenario | How to reproduce |
|---|---|
| First grant | Fresh install, tap "Request", accept. Verify `Granted`. |
| Single denial (Android) | Fresh install, tap "Request", deny. Tap again, verify second prompt shows. |
| Permanent denial (Android) | After two denials, verify `canAskAgain = false`. |
| Permanent denial (iOS) | After one denial, verify `canAskAgain = false`. |
| OS-suppressed re-prompt | After permanent denial, tap Request, verify no dialog, immediate `Denied(canAskAgain=false)`. |
| Settings round-trip | Tap "Open Settings", grant, return, verify status flips to `Granted`. |
| Policy exhausted | With `maxRequestsAndroid = 1`, verify second tap returns `PolicyExhausted` immediately. |

Run each scenario before cutting a release.

## What we don't recommend

- **Mocking `ActivityResultLauncher` or `CLLocationManager`.** They have so many
  state hooks that mocks drift quickly. Use `PreviewPermissionController` for unit
  tests and the real APIs in instrumented tests.
- **Relying on `wasPermissionRequested` for UX gating.** It only reports requests
  routed through Aagya. If your app ever requested a permission outside Aagya, the
  count will not reflect that.
