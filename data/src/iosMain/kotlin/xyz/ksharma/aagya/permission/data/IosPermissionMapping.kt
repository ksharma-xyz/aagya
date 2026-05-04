package xyz.ksharma.aagya.permission.data

import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import xyz.ksharma.aagya.permission.PermissionStatus

/**
 * Translates a `CLAuthorizationStatus` into Aagya's [PermissionStatus] sealed type.
 *
 * iOS only exposes a request twice on the same status if it was `notDetermined`.
 * Once the user has answered, calling `requestWhenInUseAuthorization` again is a
 * no-op. So `Denied` always means `canAskAgain = false` on iOS.
 */
internal fun CLAuthorizationStatus.toPermissionStatus(): PermissionStatus = when (this) {
    kCLAuthorizationStatusNotDetermined -> PermissionStatus.NotDetermined
    kCLAuthorizationStatusAuthorizedWhenInUse,
    kCLAuthorizationStatusAuthorizedAlways,
    -> PermissionStatus.Granted
    kCLAuthorizationStatusDenied -> PermissionStatus.Denied(canAskAgain = false)
    kCLAuthorizationStatusRestricted -> PermissionStatus.Restricted
    else -> PermissionStatus.NotDetermined
}
