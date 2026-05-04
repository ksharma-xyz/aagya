package xyz.ksharma.aagya.permission

/**
 * Result of a `requestPermission` call.
 *
 * Aagya never throws across the bridge. Every outcome of asking for a permission,
 * including failure, lands here as a sealed value.
 */
public sealed interface PermissionResult {

    /** The user granted the permission. */
    public data object Granted : PermissionResult

    /**
     * The permission was denied.
     *
     * @param canAskAgain Whether asking again will surface the system dialog. Mirrors
     * [PermissionStatus.Denied.canAskAgain] but reflects the post-request state. When
     * `false`, surface a "Open Settings" affordance to the user.
     * @param reason Why this denial happened. Useful for telemetry; do not branch UX
     * on this beyond what `canAskAgain` already tells you.
     */
    public data class Denied(
        public val canAskAgain: Boolean,
        public val reason: DenialReason = DenialReason.UserDenied,
    ) : PermissionResult

    /** The user dismissed the prompt without choosing. iOS does not produce this. */
    public data object Cancelled : PermissionResult

    /**
     * Aagya refused to ask because the configured [PermissionPolicy] has been
     * exhausted (request count reached the configured maximum). Treat this as a
     * permanent denial from the app's perspective, even if the OS would still allow
     * the dialog.
     */
    public data object PolicyExhausted : PermissionResult
}

/** Why a [PermissionResult.Denied] happened. Diagnostics, not UX-driving. */
public enum class DenialReason {
    /** The user tapped "Don't Allow" / equivalent. */
    UserDenied,

    /** The OS suppressed the dialog because the permission was already permanently denied. */
    SystemSuppressed,

    /** Aagya could not present the dialog (no `Activity`, no `UIViewController`, etc.). */
    PresentationUnavailable,

    /** Underlying platform error. Inspect logs for details. */
    PlatformError,
}
