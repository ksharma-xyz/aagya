package xyz.ksharma.aagya.permission.store

import kotlinx.coroutines.test.runTest
import xyz.ksharma.aagya.permission.AppPermission
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemoryPermissionStoreTest {

    @Test
    fun newStoreReportsZero() = runTest {
        val store = InMemoryPermissionStore()
        assertEquals(0, store.getRequestCount(AppPermission.Location.Fine))
    }

    @Test
    fun incrementCountsForOnePermissionOnly() = runTest {
        val store = InMemoryPermissionStore()
        store.incrementRequestCount(AppPermission.Location.Fine)
        store.incrementRequestCount(AppPermission.Location.Fine)
        assertEquals(2, store.getRequestCount(AppPermission.Location.Fine))
        assertEquals(0, store.getRequestCount(AppPermission.Location.Coarse))
    }

    @Test
    fun resetClearsCount() = runTest {
        val store = InMemoryPermissionStore()
        store.incrementRequestCount(AppPermission.Location.Fine)
        store.reset(AppPermission.Location.Fine)
        assertEquals(0, store.getRequestCount(AppPermission.Location.Fine))
    }
}
