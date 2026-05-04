package xyz.ksharma.aagya.permission.data

import androidx.compose.runtime.Composable
import xyz.ksharma.aagya.permission.Logger
import xyz.ksharma.aagya.permission.NoOpLogger
import xyz.ksharma.aagya.permission.PermissionPolicy

/**
 * Compose-friendly factory for a [PermissionController].
 *
 * The returned controller is bound to the current platform host (`Activity` on Android,
 * root `UIViewController` on iOS). It survives configuration changes via Compose's
 * `remember` semantics.
 *
 * @param policy Optional stricter policy on top of system behavior. Defaults to
 * [PermissionPolicy.Default] which uses the OS as the source of truth.
 * @param logger Optional logger for diagnostics. Defaults to [NoOpLogger] (silent).
 */
@Composable
public expect fun rememberPermissionController(
    policy: PermissionPolicy = PermissionPolicy.Default,
    logger: Logger = NoOpLogger,
): PermissionController
