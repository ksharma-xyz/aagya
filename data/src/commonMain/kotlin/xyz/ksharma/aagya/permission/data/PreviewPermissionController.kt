package xyz.ksharma.aagya.permission.data

import xyz.ksharma.aagya.permission.AppPermission
import xyz.ksharma.aagya.permission.PermissionResult
import xyz.ksharma.aagya.permission.PermissionStatus

/**
 * No-op [PermissionController] for Compose previews and unit tests.
 *
 * Returns the configured [defaultStatus] for every permission and never surfaces a
 * dialog. [requestPermission] resolves with [defaultRequestResult] without delay.
 */
public class PreviewPermissionController(
    private val defaultStatus: PermissionStatus = PermissionStatus.NotDetermined,
    private val defaultRequestResult: PermissionResult = PermissionResult.Granted,
) : PermissionController {

    override suspend fun requestPermission(permission: AppPermission): PermissionResult =
        defaultRequestResult

    override suspend fun checkPermissionStatus(permission: AppPermission): PermissionStatus =
        defaultStatus

    override suspend fun wasPermissionRequested(permission: AppPermission): Boolean = false

    override fun openAppSettings() = Unit
}
