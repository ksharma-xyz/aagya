package xyz.ksharma.aagya.permission.store.userdefaults

import platform.Foundation.NSUserDefaults
import xyz.ksharma.aagya.permission.AppPermission
import xyz.ksharma.aagya.permission.store.PermissionStore

/**
 * [PermissionStore] backed by `NSUserDefaults`.
 *
 * Persists across process restarts. Uses the keyspace `xyz.ksharma.aagya.permissions.*`
 * to avoid collisions with the host app's own keys.
 *
 * @param suiteName Optional `NSUserDefaults` suite name. Pass a value here to share
 * counts with an App Group, otherwise defaults to the standard user defaults.
 */
public class UserDefaultsPermissionStore(
    suiteName: String? = null,
) : PermissionStore {

    private val defaults: NSUserDefaults =
        suiteName?.let { NSUserDefaults(suiteName = it) } ?: NSUserDefaults.standardUserDefaults

    override suspend fun getRequestCount(permission: AppPermission): Int {
        val raw = defaults.integerForKey(permission.toKey())
        return raw.toInt().coerceAtLeast(0)
    }

    override suspend fun incrementRequestCount(permission: AppPermission) {
        val key = permission.toKey()
        val next = defaults.integerForKey(key) + 1
        defaults.setInteger(next, key)
    }

    override suspend fun reset(permission: AppPermission) {
        defaults.removeObjectForKey(permission.toKey())
    }

    private fun AppPermission.toKey(): String = "xyz.ksharma.aagya.permissions.requestCount.$key"
}
