package xyz.ksharma.aagya.permission.data

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioApplication
import platform.AVFAudio.AVAudioApplicationRecordPermissionDenied
import platform.AVFAudio.AVAudioApplicationRecordPermissionGranted
import xyz.ksharma.aagya.permission.PermissionStatus
import kotlin.coroutines.resume

/**
 * `AVAudioApplication` has no delegate-based authorization callback like `CLLocationManager` -
 * `recordPermission` answers "what's the state right now" synchronously, and
 * `requestRecordPermissionWithCompletionHandler` is a plain completion-handler call that
 * resolves immediately (no dialog) once the user has already answered once. That's simpler
 * than the location flow, so no delegate/`CompletableDeferred` plumbing is needed here.
 *
 * These APIs are iOS 17+. The `AVAudioSession` equivalents they replace were deprecated in
 * iOS 17, and Kotlin/Native has no `if #available` equivalent - it links the symbol either
 * way and fails at runtime on older systems - so Aagya sets its floor at iOS 17 rather than
 * hand-rolling a version gate around a branch it cannot test.
 */
internal fun microphoneAuthorizationStatus(): PermissionStatus =
    when (AVAudioApplication.sharedInstance().recordPermission) {
        AVAudioApplicationRecordPermissionGranted -> PermissionStatus.Granted
        AVAudioApplicationRecordPermissionDenied -> PermissionStatus.Denied(canAskAgain = false)
        else -> PermissionStatus.NotDetermined
    }

internal suspend fun requestMicrophoneAuthorization(): PermissionStatus =
    suspendCancellableCoroutine { continuation ->
        AVAudioApplication.requestRecordPermissionWithCompletionHandler { granted ->
            continuation.resume(
                if (granted) PermissionStatus.Granted else PermissionStatus.Denied(canAskAgain = false),
            )
        }
    }
