package xyz.ksharma.aagya.permission.store

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import xyz.ksharma.aagya.permission.AppPermission

/**
 * Default [PermissionStore] backed by a plain in-memory map.
 *
 * Counts are reset on every process restart. Suitable for ephemeral session-scoped
 * policies or for tests.
 *
 * For policies that need to survive a process restart (for example "ask only once,
 * ever") use a persistent adapter: `aagya-store-datastore` on Android, or
 * `aagya-store-userdefaults` on iOS.
 */
public class InMemoryPermissionStore : PermissionStore {

    private val mutex = Mutex()
    private val counts = mutableMapOf<String, Int>()

    override suspend fun getRequestCount(permission: AppPermission): Int = mutex.withLock {
        counts[permission.key] ?: 0
    }

    override suspend fun incrementRequestCount(permission: AppPermission): Unit = mutex.withLock {
        counts[permission.key] = (counts[permission.key] ?: 0) + 1
    }

    override suspend fun reset(permission: AppPermission): Unit = mutex.withLock {
        counts.remove(permission.key)
    }
}
