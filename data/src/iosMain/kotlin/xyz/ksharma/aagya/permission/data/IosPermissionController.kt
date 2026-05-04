package xyz.ksharma.aagya.permission.data

import kotlinx.coroutines.CompletableDeferred
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import xyz.ksharma.aagya.permission.AppPermission
import xyz.ksharma.aagya.permission.DenialReason
import xyz.ksharma.aagya.permission.Logger
import xyz.ksharma.aagya.permission.PermissionPolicy
import xyz.ksharma.aagya.permission.PermissionResult
import xyz.ksharma.aagya.permission.PermissionStatus

/**
 * iOS implementation of [PermissionController].
 *
 * Construction is private; obtain instances via [rememberPermissionController].
 *
 * iOS authorization model:
 *   - The system fully owns the "have I asked yet" question. Aagya only ever asks via
 *     `requestWhenInUseAuthorization` when the current status is `notDetermined`.
 *   - Once the user has answered (any non-`notDetermined` status), the dialog will
 *     never appear again. The only path forward is `openAppSettings`.
 *   - There is no "fine vs coarse" toggle in the API. iOS 14+ surfaces a precise/
 *     approximate toggle through the system dialog and reflects it via
 *     `accuracyAuthorization`. Aagya treats both Fine and Coarse as the same request.
 */
internal class IosPermissionController(
    private val locationManager: CLLocationManager,
    private val delegate: IosLocationAuthorizationDelegate,
    policy: PermissionPolicy,
    private val logger: Logger,
) : PermissionController {

    private val evaluator = PolicyEvaluator(
        policyMax = policy.maxRequestsIos,
        platformMax = IOS_OS_CAP,
        policy = policy,
    )

    init {
        locationManager.delegate = delegate
    }

    override suspend fun requestPermission(permission: AppPermission): PermissionResult {
        val status = checkPermissionStatus(permission)
        if (status is PermissionStatus.Granted) {
            logger.debug("requestPermission: ${permission.key} already granted")
            return PermissionResult.Granted
        }
        if (status is PermissionStatus.Denied) {
            logger.info("requestPermission: ${permission.key} permanently denied at OS level")
            return PermissionResult.Denied(
                canAskAgain = false,
                reason = DenialReason.SystemSuppressed,
            )
        }
        if (status is PermissionStatus.Restricted) {
            logger.info("requestPermission: ${permission.key} restricted by device policy")
            return PermissionResult.Denied(
                canAskAgain = false,
                reason = DenialReason.SystemSuppressed,
            )
        }
        if (!evaluator.canPrompt(permission)) {
            logger.info("requestPermission: ${permission.key} blocked by policy cap")
            return PermissionResult.PolicyExhausted
        }

        return runCatching {
            evaluator.recordPrompt(permission)
            val deferred = CompletableDeferred<CLAuthorizationStatus>()
            delegate.pending = deferred
            locationManager.requestWhenInUseAuthorization()
            val resolved = deferred.await()
            mapToResult(resolved)
        }.getOrElse { error ->
            logger.error("requestPermission: failed for ${permission.key}", error)
            PermissionResult.Denied(
                canAskAgain = true,
                reason = DenialReason.PlatformError,
            )
        }
    }

    override suspend fun checkPermissionStatus(permission: AppPermission): PermissionStatus {
        // Both Fine and Coarse share the same iOS authorization status.
        return when (permission) {
            AppPermission.Location.Fine,
            AppPermission.Location.Coarse,
            -> locationManager.authorizationStatus.toPermissionStatus()
        }
    }

    override suspend fun wasPermissionRequested(permission: AppPermission): Boolean =
        evaluator.wasRequested(permission)

    override fun openAppSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (url != null) {
            UIApplication.sharedApplication.openURL(url)
        } else {
            logger.warn("openAppSettings: could not build settings URL")
        }
    }

    private fun mapToResult(status: CLAuthorizationStatus): PermissionResult =
        when (status.toPermissionStatus()) {
            PermissionStatus.Granted -> PermissionResult.Granted
            is PermissionStatus.Denied -> PermissionResult.Denied(
                canAskAgain = false,
                reason = DenialReason.UserDenied,
            )
            PermissionStatus.Restricted -> PermissionResult.Denied(
                canAskAgain = false,
                reason = DenialReason.SystemSuppressed,
            )
            PermissionStatus.NotDetermined -> {
                // Should be unreachable: the delegate only fires after a determination.
                logger.warn("requestPermission resolved to NotDetermined; treating as denied")
                PermissionResult.Denied(
                    canAskAgain = true,
                    reason = DenialReason.PlatformError,
                )
            }
        }

    companion object {
        /** iOS only ever surfaces a single explicit prompt per permission. */
        const val IOS_OS_CAP: Int = 1
    }
}
