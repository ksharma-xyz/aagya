package xyz.ksharma.aagya.permission.data

import xyz.ksharma.aagya.permission.AppPermission
import xyz.ksharma.aagya.permission.PermissionResult
import xyz.ksharma.aagya.permission.PermissionStatus

/**
 * Cross-platform handle for asking the system about permissions and prompting the user.
 *
 * Get one via [rememberPermissionController] inside a Composable. The returned controller
 * is bound to the current `Activity` (Android) or root `UIViewController` (iOS) and is
 * safe to capture in `LaunchedEffect` or pass into a ViewModel.
 *
 * Aagya never throws across this interface. Failures are returned as
 * [PermissionResult] sealed values.
 *
 * Threading: every suspend function may be called from any dispatcher. Implementations
 * marshal to the platform's main thread internally where required.
 */
public interface PermissionController {

    /**
     * Show the system prompt for [permission] and suspend until the user answers,
     * cancels, or the platform short-circuits the call.
     *
     * Pre-flight rules applied in order:
     * 1. If the configured [xyz.ksharma.aagya.permission.PermissionPolicy] is exhausted
     *    for [permission], returns [PermissionResult.PolicyExhausted] immediately.
     * 2. If the system already reports [PermissionStatus.Granted], returns
     *    [PermissionResult.Granted] without showing a dialog.
     * 3. If the system reports [PermissionStatus.Denied] with `canAskAgain = false`,
     *    returns [PermissionResult.Denied] with `canAskAgain = false` and
     *    `reason = SystemSuppressed`.
     * 4. Otherwise, surfaces the system dialog and reports the user's choice.
     */
    public suspend fun requestPermission(permission: AppPermission): PermissionResult

    /**
     * Read the current state of [permission] from the system without prompting the user.
     */
    public suspend fun checkPermissionStatus(permission: AppPermission): PermissionStatus

    /**
     * Whether [permission] has been requested at least once in this process.
     *
     * Backed by the configured [xyz.ksharma.aagya.permission.store.PermissionStore]. With
     * the default in-memory store this resets on every launch.
     */
    public suspend fun wasPermissionRequested(permission: AppPermission): Boolean

    /** Open the host app's system settings page so the user can grant permission manually. */
    public fun openAppSettings()
}
