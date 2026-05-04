package xyz.ksharma.aagya.permission

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PermissionStatusTest {

    @Test
    fun deniedDistinguishesByCanAskAgain() {
        val transient = PermissionStatus.Denied(canAskAgain = true)
        val permanent = PermissionStatus.Denied(canAskAgain = false)
        assertNotEquals(transient, permanent)
    }

    @Test
    fun grantedAndNotDeterminedAreSingletons() {
        assertEquals(PermissionStatus.Granted, PermissionStatus.Granted)
        assertEquals(PermissionStatus.NotDetermined, PermissionStatus.NotDetermined)
    }
}
