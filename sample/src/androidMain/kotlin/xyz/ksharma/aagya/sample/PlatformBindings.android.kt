package xyz.ksharma.aagya.sample

import androidx.compose.runtime.Composable
import xyz.ksharma.aagya.permission.store.PermissionStore
import xyz.ksharma.aagya.permission.store.datastore.rememberDataStorePermissionStore

@Composable
internal actual fun rememberPersistentStore(): PermissionStore =
    rememberDataStorePermissionStore()
