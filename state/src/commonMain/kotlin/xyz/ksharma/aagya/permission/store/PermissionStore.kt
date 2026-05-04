package xyz.ksharma.aagya.permission.store

import xyz.ksharma.aagya.permission.AppPermission

/**
 * Persistence interface for permission request counts.
 *
 * Aagya only consults a `PermissionStore` when a [xyz.ksharma.aagya.permission.PermissionPolicy]
 * has been configured with stricter-than-system request limits. The default behavior
 * needs no storage at all because system APIs are authoritative.
 *
 * Implement this if you want to plug in your app's own preference layer. Built-in
 * implementations:
 *
 *   - [InMemoryPermissionStore] (default, this module)
 *   - `xyz.ksharma.aagya.permission.store.datastore.DataStorePermissionStore` (`aagya-store-datastore`)
 *   - `xyz.ksharma.aagya.permission.store.userdefaults.UserDefaultsPermissionStore` (`aagya-store-userdefaults`)
 *
 * Contract:
 *   - All methods must be safe to call from any coroutine context.
 *   - Implementations must be safe under concurrent reads and writes for the same key.
 *   - Methods must not throw. Surface failures via logs and treat the read as "0".
 */
public interface PermissionStore {

    /** Returns the number of times Aagya has presented a prompt for [permission]. */
    public suspend fun getRequestCount(permission: AppPermission): Int

    /** Increments the persisted request count for [permission] by 1. */
    public suspend fun incrementRequestCount(permission: AppPermission)

    /** Resets the request count for [permission] to 0. Useful for tests and "Reset" UX. */
    public suspend fun reset(permission: AppPermission)
}
