package xyz.ksharma.aagya.permission.data

import android.Manifest
import xyz.ksharma.aagya.permission.AppPermission

/**
 * Maps an [AppPermission] to the underlying Android manifest permission strings.
 *
 * Some permissions span more than one manifest entry (e.g. fine location requires
 * `ACCESS_FINE_LOCATION`, but you almost always also want `ACCESS_COARSE_LOCATION` for
 * pre-Q devices). Each entry in the returned list must be granted for Aagya to consider
 * the [AppPermission] granted.
 */
internal fun AppPermission.toAndroidPermissions(): List<String> = when (this) {
    AppPermission.Location.Fine -> listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    AppPermission.Location.Coarse -> listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
}
