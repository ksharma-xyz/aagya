package xyz.ksharma.aagya.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import xyz.ksharma.aagya.permission.AppPermission
import xyz.ksharma.aagya.permission.PermissionPolicy
import xyz.ksharma.aagya.permission.PermissionResult
import xyz.ksharma.aagya.permission.PermissionStatus
import xyz.ksharma.aagya.permission.data.rememberPermissionController
import xyz.ksharma.aagya.permission.store.datastore.rememberDataStorePermissionStore

class SampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SampleScreen()
                }
            }
        }
    }
}

@Composable
private fun SampleScreen() {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "Aagya Sample",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Three usage tiers, side by side. Tap Reset under any tier to clear " +
                    "request counts and start over.",
                style = MaterialTheme.typography.bodyMedium,
            )

            HorizontalDivider()

            Tier(
                title = "Zero-config",
                subtitle = "Default policy. Lets the OS run the show.",
            )

            HorizontalDivider()

            Tier(
                title = "Stricter (ask once, persistent)",
                subtitle = "maxRequestsAndroid = 1, DataStore-backed.",
                policy = PermissionPolicy(maxRequestsAndroid = 1, maxRequestsIos = 1),
                useDataStore = true,
            )
        }
    }
}

@Composable
private fun Tier(
    title: String,
    subtitle: String,
    policy: PermissionPolicy = PermissionPolicy.Default,
    useDataStore: Boolean = false,
) {
    val effectivePolicy = if (useDataStore) {
        policy.copy(store = rememberDataStorePermissionStore())
    } else {
        policy
    }
    val controller = rememberPermissionController(policy = effectivePolicy)
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf<PermissionStatus>(PermissionStatus.NotDetermined) }
    var lastResult by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(controller) {
        status = controller.checkPermissionStatus(AppPermission.Location.Fine)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        Text(text = "Status: $status", style = MaterialTheme.typography.bodyMedium)
        lastResult?.let {
            Text(text = "Last result: $it", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(4.dp))
        Button(onClick = {
            scope.launch {
                val result = controller.requestPermission(AppPermission.Location.Fine)
                lastResult = result.toString()
                status = controller.checkPermissionStatus(AppPermission.Location.Fine)
                if (result is PermissionResult.Denied && !result.canAskAgain) {
                    controller.openAppSettings()
                }
            }
        }) {
            Text("Request Fine Location")
        }
        OutlinedButton(onClick = { controller.openAppSettings() }) {
            Text("Open Settings")
        }
    }
}
