package xyz.ksharma.aagya.permission

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AppPermissionTest {

    @Test
    fun fineAndCoarseHaveDistinctKeys() {
        assertNotEquals(
            AppPermission.Location.Fine.key,
            AppPermission.Location.Coarse.key,
        )
    }

    @Test
    fun keysAreStable() {
        // Keys are part of the public contract: storage adapters use them as map keys.
        // Changing them is a breaking change.
        assertEquals("location.fine", AppPermission.Location.Fine.key)
        assertEquals("location.coarse", AppPermission.Location.Coarse.key)
    }
}
