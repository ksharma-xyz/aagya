package xyz.ksharma.aagya.sample

import androidx.compose.runtime.Composable
import xyz.ksharma.aagya.permission.store.PermissionStore

/**
 * Platform-specific persistent store for the sample's "Ask once" tier.
 *
 * - Android: backed by Jetpack DataStore (`aagya-store-datastore`).
 * - iOS: backed by NSUserDefaults (`aagya-store-userdefaults`).
 *
 * The shared sample UI calls this to demonstrate the BYOS storage tier without
 * needing to know which platform it's on.
 */
@Composable
internal expect fun rememberPersistentStore(): PermissionStore
