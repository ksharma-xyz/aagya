package xyz.ksharma.aagya.permission.data

import kotlinx.coroutines.test.runTest
import xyz.ksharma.aagya.permission.AppPermission
import xyz.ksharma.aagya.permission.PermissionPolicy
import xyz.ksharma.aagya.permission.store.InMemoryPermissionStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PolicyEvaluatorTest {

    @Test
    fun uncappedPolicyAlwaysAllowsPrompt() = runTest {
        val evaluator = PolicyEvaluator(
            policyMax = Int.MAX_VALUE,
            platformMax = Int.MAX_VALUE,
            policy = PermissionPolicy.Default,
        )
        repeat(5) { evaluator.recordPrompt(AppPermission.Location.Fine) }
        assertTrue(evaluator.canPrompt(AppPermission.Location.Fine))
    }

    @Test
    fun blocksAfterPolicyMaxReached() = runTest {
        val store = InMemoryPermissionStore()
        val evaluator = PolicyEvaluator(
            policyMax = 1,
            platformMax = 2,
            policy = PermissionPolicy(maxRequestsAndroid = 1, store = store),
        )
        assertTrue(evaluator.canPrompt(AppPermission.Location.Fine))
        evaluator.recordPrompt(AppPermission.Location.Fine)
        assertFalse(evaluator.canPrompt(AppPermission.Location.Fine))
    }

    @Test
    fun usesMinimumOfPolicyAndPlatformCaps() = runTest {
        val store = InMemoryPermissionStore()
        val evaluator = PolicyEvaluator(
            policyMax = 5,
            platformMax = 1,
            policy = PermissionPolicy(maxRequestsAndroid = 5, store = store),
        )
        evaluator.recordPrompt(AppPermission.Location.Fine)
        assertFalse(
            actual = evaluator.canPrompt(AppPermission.Location.Fine),
            message = "Effective cap should be min(policy=5, platform=1) = 1",
        )
    }

    @Test
    fun wasRequestedTracksFirstPrompt() = runTest {
        val store = InMemoryPermissionStore()
        val evaluator = PolicyEvaluator(
            policyMax = 2,
            platformMax = 2,
            policy = PermissionPolicy(store = store),
        )
        assertEquals(false, evaluator.wasRequested(AppPermission.Location.Fine))
        evaluator.recordPrompt(AppPermission.Location.Fine)
        assertEquals(true, evaluator.wasRequested(AppPermission.Location.Fine))
    }
}
