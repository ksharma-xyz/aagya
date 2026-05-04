package xyz.ksharma.aagya.permission

/**
 * Current state of a permission, as reported by the platform.
 *
 * This is a *read* of the system, never a *result* of a user action. To request
 * a permission and observe what the user did, use [PermissionResult].
 */
public sealed interface PermissionStatus {

    /** The user has not been asked yet. The permission can still be requested. */
    public data object NotDetermined : PermissionStatus

    /** The permission is granted. */
    public data object Granted : PermissionStatus

    /**
     * The permission has been denied.
     *
     * @param canAskAgain Whether calling `requestPermission` will surface the system
     * dialog again. On iOS this is always `false` once the user has denied. On Android
     * this is `true` after a single denial and `false` after the second denial (or after
     * the user has selected "don't ask again"). When `false`, route the user to
     * [xyz.ksharma.aagya.permission.data.PermissionController.openAppSettings] instead.
     */
    public data class Denied(public val canAskAgain: Boolean) : PermissionStatus

    /**
     * Permission has been restricted by parental controls or device policy and the
     * user can not change it. iOS-only in practice (`CLAuthorizationStatus.restricted`).
     */
    public data object Restricted : PermissionStatus
}
