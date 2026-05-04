package xyz.ksharma.aagya.permission.data

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CompletableDeferred
import xyz.ksharma.aagya.permission.Logger
import xyz.ksharma.aagya.permission.NoOpLogger
import xyz.ksharma.aagya.permission.PermissionPolicy

@Composable
public actual fun rememberPermissionController(
    policy: PermissionPolicy,
    logger: Logger,
): PermissionController {
    val activity = (LocalActivity.current as? ComponentActivity)
        ?: error(
            "Aagya could not find a ComponentActivity. " +
                "Call rememberPermissionController() inside an Activity host (typical Compose setup).",
        )

    // Single deferred per outstanding request. Holding it in a remembered box lets
    // the launcher's callback resolve whichever request is currently in flight without
    // a global static.
    val pending = remember { Box<CompletableDeferred<Map<String, Boolean>>?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        val deferred = pending.value
        pending.value = null
        deferred?.complete(results)
    }

    return remember(activity, launcher, policy, logger) {
        AndroidPermissionController(
            activity = activity,
            launcher = launcher,
            installResultChannel = { pending.value = it },
            policy = policy,
            logger = logger,
        )
    }
}

/** Tiny mutable holder for use inside `remember`. Keeps the result channel a single object. */
private class Box<T>(var value: T)
