package xyz.ksharma.aagya.permission.data

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionDenied
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import xyz.ksharma.aagya.permission.PermissionStatus
import kotlin.coroutines.resume

/**
 * `AVAudioSession` has no delegate-based authorization callback like `CLLocationManager` -
 * `recordPermission` answers "what's the state right now" synchronously, and
 * `requestRecordPermission` is a plain completion-handler call that resolves immediately
 * (no dialog) once the user has already answered once. That's simpler than the location
 * flow, so no delegate/`CompletableDeferred` plumbing is needed here.
 */
internal fun microphoneAuthorizationStatus(): PermissionStatus =
    when (AVAudioSession.sharedInstance().recordPermission) {
        AVAudioSessionRecordPermissionGranted -> PermissionStatus.Granted
        AVAudioSessionRecordPermissionDenied -> PermissionStatus.Denied(canAskAgain = false)
        else -> PermissionStatus.NotDetermined
    }

internal suspend fun requestMicrophoneAuthorization(): PermissionStatus =
    suspendCancellableCoroutine { continuation ->
        AVAudioSession.sharedInstance().requestRecordPermission { granted ->
            continuation.resume(
                if (granted) PermissionStatus.Granted else PermissionStatus.Denied(canAskAgain = false),
            )
        }
    }
