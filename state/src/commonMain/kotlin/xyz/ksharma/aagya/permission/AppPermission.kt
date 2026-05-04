package xyz.ksharma.aagya.permission

/**
 * A permission that an app can request from the user.
 *
 * Aagya keeps this hierarchy sealed so that platform implementations can exhaustively
 * map every public type to the matching system permission. Adding a new family
 * (Camera, Microphone, etc.) is non-breaking for consumers because they pattern-match
 * against the family they care about, not the closed universe.
 *
 * v0.1 ships only the [Location] family. Other families are reserved here for visibility
 * and will be added in subsequent minor releases.
 *
 * Example:
 * ```
 * val perm: AppPermission = AppPermission.Location.Fine
 * controller.requestPermission(perm)
 * ```
 */
public sealed interface AppPermission {

    /** Stable string identifier suitable for storage keys, telemetry, etc. */
    public val key: String

    /**
     * Location permissions.
     *
     * On Android, [Fine] requires `ACCESS_FINE_LOCATION` and [Coarse] requires
     * `ACCESS_COARSE_LOCATION`. Granting Fine implicitly grants Coarse.
     *
     * On iOS, both map to `requestWhenInUseAuthorization` against `CLLocationManager`.
     * The system does not distinguish fine vs coarse at the API level prior to iOS 14;
     * on iOS 14+ the precise toggle is surfaced through the system dialog and reflected
     * back through `accuracyAuthorization`.
     */
    public sealed interface Location : AppPermission {

        /** Foreground precise location. */
        public data object Fine : Location {
            override val key: String = "location.fine"
        }

        /** Foreground approximate location only. */
        public data object Coarse : Location {
            override val key: String = "location.coarse"
        }
    }

    // Future families. Uncomment as implementations land. Marked here so consumers
    // can see the trajectory and IDEs surface the namespace.
    //
    // public sealed interface Camera : AppPermission { public data object Default : Camera }
    // public sealed interface Microphone : AppPermission { public data object Default : Microphone }
    // public sealed interface Notifications : AppPermission { public data object Default : Notifications }
    // public sealed interface Photos : AppPermission {
    //     public data object Read : Photos
    //     public data object ReadWrite : Photos
    // }
    // public sealed interface Calendar : AppPermission { public data object Default : Calendar }
    // public sealed interface Contacts : AppPermission { public data object Default : Contacts }
}
