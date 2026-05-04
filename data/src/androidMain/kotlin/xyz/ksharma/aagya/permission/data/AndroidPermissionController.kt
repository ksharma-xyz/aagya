package xyz.ksharma.aagya.permission.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import xyz.ksharma.aagya.permission.AppPermission
import xyz.ksharma.aagya.permission.DenialReason
import xyz.ksharma.aagya.permission.Logger
import xyz.ksharma.aagya.permission.PermissionPolicy
import xyz.ksharma.aagya.permission.PermissionResult
import xyz.ksharma.aagya.permission.PermissionStatus

/**
 * Android implementation of [PermissionController].
 *
 * Construction is private; obtain instances via the Composable factory
 * [rememberPermissionController]. The factory wires the [ActivityResultLauncher]
 * into the host `ComponentActivity`'s lifecycle for you.
 *
 * Source-of-truth strategy:
 *   - `ContextCompat.checkSelfPermission` answers "is it currently granted?".
 *   - `ActivityCompat.shouldShowRequestPermissionRationale` answers "can I still ask?".
 *     The Android docs are subtle here: this returns `true` only when the user has
 *     denied once but not selected "Don't ask again". So:
 *       - when status=Denied AND rationale=true  means canAskAgain=true  (one denial so far)
 *       - when status=Denied AND rationale=false means canAskAgain=false (permanently denied)
 *
 * Aagya layers an optional [PermissionPolicy] cap on top of this. Without a policy,
 * the OS rules above apply unchanged.
 */
internal class AndroidPermissionController(
    private val activity: ComponentActivity,
    private val launcher: ActivityResultLauncher<Array<String>>,
    private val installResultChannel: (CompletableDeferred<Map<String, Boolean>>) -> Unit,
    policy: PermissionPolicy,
    private val logger: Logger,
) : PermissionController {

    private val evaluator = PolicyEvaluator(
        policyMax = policy.maxRequestsAndroid,
        platformMax = ANDROID_OS_CAP,
        policy = policy,
    )

    override suspend fun requestPermission(permission: AppPermission): PermissionResult {
        val status = checkPermissionStatus(permission)
        if (status is PermissionStatus.Granted) {
            logger.debug("requestPermission: ${permission.key} already granted")
            return PermissionResult.Granted
        }
        if (status is PermissionStatus.Denied && !status.canAskAgain) {
            logger.info("requestPermission: ${permission.key} permanently denied at OS level")
            return PermissionResult.Denied(
                canAskAgain = false,
                reason = DenialReason.SystemSuppressed,
            )
        }
        if (!evaluator.canPrompt(permission)) {
            logger.info("requestPermission: ${permission.key} blocked by policy cap")
            return PermissionResult.PolicyExhausted
        }

        val androidPerms = permission.toAndroidPermissions().toTypedArray()
        val deferred = CompletableDeferred<Map<String, Boolean>>()
        installResultChannel(deferred)

        return runCatching {
            evaluator.recordPrompt(permission)
            launcher.launch(androidPerms)
            val results = deferred.await()
            mapToResult(permission, results)
        }.getOrElse { error ->
            logger.error("requestPermission: launch failed for ${permission.key}", error)
            PermissionResult.Denied(
                canAskAgain = true,
                reason = DenialReason.PlatformError,
            )
        }
    }

    override suspend fun checkPermissionStatus(permission: AppPermission): PermissionStatus {
        val androidPerms = permission.toAndroidPermissions()
        val allGranted = androidPerms.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) return PermissionStatus.Granted

        val rationale = androidPerms.any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
        // shouldShowRequestPermissionRationale=true means "user denied once, can ask again".
        // shouldShowRequestPermissionRationale=false + status=denied means either:
        //   (a) the user has never been asked yet, but then the controller would have
        //       returned NotDetermined; OR
        //   (b) the user has permanently denied, this is the case we care about here.
        // Aagya distinguishes (a) and (b) using the policy store: if the request count
        // is 0, the user has never been asked, so it's NotDetermined.
        val asked = evaluator.wasRequested(permission)
        if (!asked && !rationale) return PermissionStatus.NotDetermined
        return PermissionStatus.Denied(canAskAgain = rationale)
    }

    override suspend fun wasPermissionRequested(permission: AppPermission): Boolean =
        evaluator.wasRequested(permission)

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            activity.startActivity(intent)
        }.onFailure { error ->
            logger.error("openAppSettings: failed to start settings intent", error)
        }
    }

    private fun mapToResult(
        permission: AppPermission,
        results: Map<String, Boolean>,
    ): PermissionResult {
        val androidPerms = permission.toAndroidPermissions()
        val granted = androidPerms.all { results[it] == true }
        if (granted) return PermissionResult.Granted

        // After the launcher returns, re-check rationale. If false, the user picked
        // "Don't ask again" or the OS suppressed the dialog.
        val rationale = androidPerms.any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
        return PermissionResult.Denied(
            canAskAgain = rationale,
            reason = if (rationale) DenialReason.UserDenied else DenialReason.SystemSuppressed,
        )
    }

    companion object {
        /**
         * The number of explicit prompts Android will surface to the user. After two
         * denials, `shouldShowRequestPermissionRationale` returns `false` permanently
         * and subsequent `launcher.launch` calls return immediately with all-denied.
         */
        const val ANDROID_OS_CAP: Int = 2

        internal fun resolveContextActivity(context: Context): ComponentActivity {
            var ctx: Context? = context
            while (ctx is android.content.ContextWrapper) {
                if (ctx is ComponentActivity) return ctx
                ctx = ctx.baseContext
            }
            error(
                "Aagya could not find a ComponentActivity to bind to. " +
                    "Make sure rememberPermissionController() is called inside an Activity " +
                    "context (typical Compose setup).",
            )
        }
    }
}
