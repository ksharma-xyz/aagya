package xyz.ksharma.aagya.permission.data

import xyz.ksharma.aagya.permission.AppPermission
import xyz.ksharma.aagya.permission.PermissionPolicy
import xyz.ksharma.aagya.permission.store.InMemoryPermissionStore
import xyz.ksharma.aagya.permission.store.PermissionStore

/**
 * Shared policy/cap evaluator used by every platform controller.
 *
 * The platform constructs this with its OS-side cap (`2` on Android, `1` on iOS) and
 * the configured [PermissionPolicy]'s matching field. Aagya then takes the minimum of
 * the two as the effective cap. `Int.MAX_VALUE` on both sides means "uncapped, defer
 * to OS state entirely".
 *
 * Internal. Not part of the public API.
 */
internal class PolicyEvaluator(
    private val policyMax: Int,
    private val platformMax: Int,
    policy: PermissionPolicy,
) {
    private val store: PermissionStore = policy.store ?: InMemoryPermissionStore()
    private val effectiveCap: Int = minOf(policyMax, platformMax)

    /** Returns true if a prompt would respect the configured cap. */
    suspend fun canPrompt(permission: AppPermission): Boolean {
        if (effectiveCap == Int.MAX_VALUE) return true
        return store.getRequestCount(permission) < effectiveCap
    }

    suspend fun recordPrompt(permission: AppPermission) {
        store.incrementRequestCount(permission)
    }

    suspend fun wasRequested(permission: AppPermission): Boolean =
        store.getRequestCount(permission) > 0
}
