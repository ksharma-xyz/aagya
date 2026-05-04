package xyz.ksharma.aagya.permission.data

import kotlinx.coroutines.CompletableDeferred
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.darwin.NSObject

/**
 * `CLLocationManagerDelegate` that bridges authorization callbacks to Kotlin coroutines.
 *
 * iOS does not provide a callback-based "ask the user and tell me what they picked"
 * API for location. Instead the manager fires `locationManagerDidChangeAuthorization:`
 * any time authorization changes. This delegate forwards that into the
 * [pending] deferred so callers can `await()` it.
 */
internal class IosLocationAuthorizationDelegate :
    NSObject(),
    CLLocationManagerDelegateProtocol {

    /** Set by the controller before calling `requestWhenInUseAuthorization`. */
    var pending: CompletableDeferred<CLAuthorizationStatus>? = null

    override fun locationManager(
        manager: CLLocationManager,
        didChangeAuthorizationStatus: CLAuthorizationStatus,
    ) {
        // Filter out the "I haven't asked yet" event we get on initial subscription.
        if (didChangeAuthorizationStatus == kCLAuthorizationStatusNotDetermined &&
            pending?.isCompleted == false
        ) {
            return
        }
        pending?.complete(didChangeAuthorizationStatus)
        pending = null
    }

    /** iOS 14+ delegate method. Same wiring as the iOS 13 method above. */
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        val status = manager.authorizationStatus
        if (status == kCLAuthorizationStatusNotDetermined && pending?.isCompleted == false) {
            return
        }
        pending?.complete(status)
        pending = null
    }
}
