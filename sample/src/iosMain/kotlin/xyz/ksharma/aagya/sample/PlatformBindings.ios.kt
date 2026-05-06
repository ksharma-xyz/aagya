package xyz.ksharma.aagya.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import xyz.ksharma.aagya.permission.store.PermissionStore
import xyz.ksharma.aagya.permission.store.userdefaults.UserDefaultsPermissionStore

@Composable
internal actual fun rememberPersistentStore(): PermissionStore =
    remember { UserDefaultsPermissionStore() }
