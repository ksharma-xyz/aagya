package xyz.ksharma.aagya.permission.data

import xyz.ksharma.aagya.permission.PermissionStatus
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * `AVAudioApplication` is iOS 17+. Kotlin/Native has no `if #available`, so a call to a
 * class the running system does not have is not a compile error - it is a crash the first
 * time the code path executes. Compiling and linking therefore proves nothing about it.
 *
 * This exercises the real call against the simulator runtime, which is the cheapest place
 * that failure mode can actually surface.
 */
class IosMicrophoneAuthorizationTest {

    @Test
    fun `microphone status resolves against the live AVAudioApplication API`() {
        val status = microphoneAuthorizationStatus()

        // The specific value depends on the simulator's privacy state, which the test
        // cannot set, so asserting one exact status would be flaky. What matters is that
        // the call reaches AVAudioApplication and maps the result to a known status
        // instead of trapping on a missing symbol or falling through the mapping.
        assertTrue(
            status == PermissionStatus.Granted ||
                status is PermissionStatus.Denied ||
                status == PermissionStatus.NotDetermined ||
                status == PermissionStatus.Restricted,
            "Unexpected microphone status: $status",
        )
    }
}
