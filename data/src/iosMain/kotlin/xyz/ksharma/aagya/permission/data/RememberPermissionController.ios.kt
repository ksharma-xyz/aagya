package xyz.ksharma.aagya.permission.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.CoreLocation.CLLocationManager
import xyz.ksharma.aagya.permission.Logger
import xyz.ksharma.aagya.permission.NoOpLogger
import xyz.ksharma.aagya.permission.PermissionPolicy

@Composable
public actual fun rememberPermissionController(
    policy: PermissionPolicy,
    logger: Logger,
): PermissionController {
    return remember(policy, logger) {
        val manager = CLLocationManager()
        val delegate = IosLocationAuthorizationDelegate()
        IosPermissionController(
            locationManager = manager,
            delegate = delegate,
            policy = policy,
            logger = logger,
        )
    }
}
