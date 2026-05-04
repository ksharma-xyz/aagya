package xyz.ksharma.aagya.permission

import xyz.ksharma.aagya.permission.store.PermissionStore

/**
 * Optional layer of app-level rules on top of system permission behavior.
 *
 * Aagya's default behavior delegates to the OS:
 *   - Android allows up to 2 explicit prompts; the OS suppresses subsequent ones.
 *   - iOS allows exactly 1 prompt; subsequent calls are no-ops.
 *
 * If you want a stricter policy (for example "ask once, ever, on Android too"), pass a
 * [PermissionPolicy] with a [store] so request counts can be persisted across runs.
 *
 * Without a [store], Aagya uses an in-memory tracker. That tracker resets on every app
 * launch, so a configured `maxRequestsAndroid = 1` would still allow one prompt per
 * launch. Almost always you want a persistent store when you configure tighter limits.
 */
public data class PermissionPolicy(

    /**
     * Maximum prompts Aagya will surface to the user on Android.
     *
     * - `Int.MAX_VALUE` (default): use system behavior, no Aagya-side cap.
     * - `2`: matches the OS limit explicitly.
     * - `1`: stricter "ask once" UX. Requires a persistent [store] to be useful.
     */
    public val maxRequestsAndroid: Int = Int.MAX_VALUE,

    /**
     * Maximum prompts Aagya will surface to the user on iOS.
     *
     * - `Int.MAX_VALUE` (default): use system behavior, no Aagya-side cap.
     * - `1`: matches the OS limit explicitly.
     */
    public val maxRequestsIos: Int = Int.MAX_VALUE,

    /**
     * Storage backing the request counter. `null` means in-memory only.
     *
     * For most apps the default `null` is correct: the OS already tracks and enforces
     * its own limits. Provide a store only when you have enabled stricter limits above.
     */
    public val store: PermissionStore? = null,
) {
    public companion object {
        /** Default policy: defer entirely to platform behavior. */
        public val Default: PermissionPolicy = PermissionPolicy()
    }
}
