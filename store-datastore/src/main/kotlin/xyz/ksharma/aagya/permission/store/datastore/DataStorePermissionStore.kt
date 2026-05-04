package xyz.ksharma.aagya.permission.store.datastore

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import xyz.ksharma.aagya.permission.AppPermission
import xyz.ksharma.aagya.permission.store.PermissionStore

private val Context.aagyaDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "aagya_permissions",
)

/**
 * [PermissionStore] backed by Jetpack DataStore (preferences).
 *
 * Persists request counts across process restarts. Reads are non-blocking; writes
 * are atomic and crash-safe.
 *
 * Stores entries under the namespace `aagya_permissions` so it does not collide with
 * any of your existing DataStore files.
 */
public class DataStorePermissionStore(
    private val context: Context,
) : PermissionStore {

    override suspend fun getRequestCount(permission: AppPermission): Int {
        return context.aagyaDataStore.data.first()[permission.toKey()] ?: 0
    }

    override suspend fun incrementRequestCount(permission: AppPermission) {
        context.aagyaDataStore.edit { prefs ->
            val key = permission.toKey()
            prefs[key] = (prefs[key] ?: 0) + 1
        }
    }

    override suspend fun reset(permission: AppPermission) {
        context.aagyaDataStore.edit { prefs ->
            prefs.remove(permission.toKey())
        }
    }

    private fun AppPermission.toKey() = intPreferencesKey("requestCount.$key")
}

/** Compose convenience that builds a `DataStorePermissionStore` for the current context. */
@Composable
public fun rememberDataStorePermissionStore(): PermissionStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { DataStorePermissionStore(context) }
}
