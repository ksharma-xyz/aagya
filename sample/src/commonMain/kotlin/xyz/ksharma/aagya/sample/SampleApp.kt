package xyz.ksharma.aagya.sample

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import xyz.ksharma.aagya.permission.AppPermission
import xyz.ksharma.aagya.permission.PermissionPolicy
import xyz.ksharma.aagya.permission.PermissionResult
import xyz.ksharma.aagya.permission.PermissionStatus
import xyz.ksharma.aagya.permission.data.PermissionController
import xyz.ksharma.aagya.permission.data.rememberPermissionController
import xyz.ksharma.aagya.permission.store.PermissionStore

/**
 * The full Aagya sample app — single screen, written once in Compose Multiplatform,
 * runs on Android and iOS. Demonstrates:
 *
 *   - The permission state machine (NotDetermined / Granted / Denied(canAskAgain) / Restricted),
 *     with smooth color/icon morph between states.
 *   - Stateless vs persistent storage tiers (toggle the chip — re-creates the controller).
 *   - The action button changing label and color based on what to do next:
 *       "Ask for permission"  -> "Try again"  -> "Open Settings".
 *   - Live request count from the underlying [PermissionStore], and a transition log.
 *
 * The UI is intentionally one screen so a first-time evaluator sees the whole library
 * surface at once.
 */

private enum class StorageTier(val label: String, val description: String) {
    Stateless("OS default", "No store. 2 prompts on Android, 1 on iOS."),
    StrictAskOnce("Ask once", "Persistent store. 1 prompt across both platforms.");
}

@Composable
fun SampleApp(modifier: Modifier = Modifier) {
    var tier by remember { mutableStateOf(StorageTier.Stateless) }
    val persistentStore = rememberPersistentStore()

    val controller: PermissionController = when (tier) {
        StorageTier.Stateless -> rememberPermissionController(policy = PermissionPolicy.Default)
        StorageTier.StrictAskOnce -> rememberPermissionController(
            policy = PermissionPolicy(
                maxRequestsAndroid = 1,
                maxRequestsIos = 1,
                store = persistentStore,
            ),
        )
    }

    var status by remember(controller) { mutableStateOf<PermissionStatus>(PermissionStatus.NotDetermined) }
    var requestCount by remember(controller) { mutableStateOf(0) }
    val log = remember(controller) { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(controller) {
        status = controller.checkPermissionStatus(AppPermission.Location.Fine)
        if (controller.wasPermissionRequested(AppPermission.Location.Fine)) {
            requestCount = 1   // we only know "at least one" without expanding the public API
        }
    }

    val palette = remember(status) { paletteFor(status) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF1A1410),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            HeaderBar(status = status)
            StatusOrb(status = status, palette = palette)
            DetailCard(status = status, requestCount = requestCount, tier = tier)
            TierSelector(current = tier, onSelect = { tier = it })
            PrimaryAction(
                status = status,
                palette = palette,
                onClick = {
                    scope.launch {
                        if (status is PermissionStatus.Denied && !(status as PermissionStatus.Denied).canAskAgain) {
                            controller.openAppSettings()
                            log.prepend("Opened Settings — return when you've toggled permission")
                            return@launch
                        }
                        log.prepend("Calling requestPermission(Location.Fine)")
                        val result = controller.requestPermission(AppPermission.Location.Fine)
                        log.prepend("-> $result")
                        status = controller.checkPermissionStatus(AppPermission.Location.Fine)
                        requestCount += 1
                        if (result is PermissionResult.Denied && !result.canAskAgain) {
                            log.prepend("System suppressed; tap again to open Settings")
                        }
                    }
                },
            )
            Spacer(Modifier.height(0.dp))
            ActivityLog(log = log)
        }
    }
}

@Composable
private fun HeaderBar(status: PermissionStatus) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "Aagya",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFFFD7BA),
            )
            Text(
                text = "आज्ञा · permission",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB59481),
            )
        }
        StateBadge(status = status)
    }
}

@Composable
private fun StateBadge(status: PermissionStatus) {
    val (label, color) = when (status) {
        PermissionStatus.NotDetermined -> "Not asked" to Color(0xFF6B6F75)
        PermissionStatus.Granted -> "Granted" to Color(0xFF45C490)
        is PermissionStatus.Denied -> if (status.canAskAgain) "Denied · retry" to Color(0xFFF4B860)
            else "Denied · settings" to Color(0xFFE25C29)
        PermissionStatus.Restricted -> "Restricted" to Color(0xFF8B2E1B)
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = color,
        )
    }
}

@Composable
private fun StatusOrb(status: PermissionStatus, palette: StatePalette) {
    val infinite = rememberInfiniteTransition(label = "orb-pulse")
    val pulseScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = if (status is PermissionStatus.Granted) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val targetGlow = if (status is PermissionStatus.NotDetermined) 0.18f else 0.32f
    val glow by animateFloatAsState(targetValue = targetGlow, animationSpec = tween(700), label = "glow")
    val bgColor by animateColorAsState(
        targetValue = palette.surface,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "orb-bg",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(palette.accent.copy(alpha = glow), Color(0xFF1A1410)),
                ),
                RoundedCornerShape(28.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(116.dp)
                .scale(pulseScale)
                .background(bgColor, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = status::class,
                transitionSpec = {
                    (scaleIn(initialScale = 0.6f) + fadeIn()) togetherWith
                        (scaleOut(targetScale = 0.6f) + fadeOut())
                },
                label = "icon",
            ) { _ ->
                Icon(
                    imageVector = palette.icon,
                    contentDescription = null,
                    tint = palette.onAccent,
                    modifier = Modifier.size(56.dp),
                )
            }
        }
    }
}

@Composable
private fun DetailCard(
    status: PermissionStatus,
    requestCount: Int,
    tier: StorageTier,
) {
    val description = when (status) {
        PermissionStatus.NotDetermined -> "Aagya hasn't asked yet. Tap below to surface the system dialog."
        PermissionStatus.Granted -> "App can read precise location. The system can revoke this from Settings."
        is PermissionStatus.Denied -> if (status.canAskAgain) {
            "User declined once. Aagya can ask again."
        } else {
            "User declined and the OS won't surface the dialog again. Only Settings can flip this back."
        }
        PermissionStatus.Restricted -> "Device policy or parental controls are blocking this permission. Cannot ask."
    }
    Surface(
        color = Color(0xFF251D17),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE6D7CC),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatBlock(label = "Prompts shown", value = requestCount.toString())
                StatBlock(label = "Tier", value = tier.label)
            }
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
            color = Color(0xFFB59481),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFFFFD7BA),
        )
    }
}

@Composable
private fun TierSelector(current: StorageTier, onSelect: (StorageTier) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "STORAGE POLICY",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
            color = Color(0xFFB59481),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StorageTier.entries.forEach { tier ->
                FilterChip(
                    selected = current == tier,
                    onClick = { onSelect(tier) },
                    label = { Text(tier.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF251D17),
                        labelColor = Color(0xFFB59481),
                        selectedContainerColor = Color(0xFFE25C29),
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }
        Text(
            text = current.description,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8B7468),
        )
    }
}

@Composable
private fun PrimaryAction(status: PermissionStatus, palette: StatePalette, onClick: () -> Unit) {
    val label = when (status) {
        PermissionStatus.NotDetermined -> "Ask for location permission"
        PermissionStatus.Granted -> "Already granted, ask again"
        is PermissionStatus.Denied -> if (status.canAskAgain) "Try again" else "Open Settings"
        PermissionStatus.Restricted -> "Restricted, learn more"
    }
    val color by animateColorAsState(
        targetValue = palette.accent,
        animationSpec = tween(durationMillis = 400),
        label = "btn-color",
    )
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = palette.onAccent),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun ActivityLog(log: List<String>) {
    if (log.isEmpty()) return
    Surface(
        color = Color(0xFF15100C),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "ACTIVITY",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                color = Color(0xFFB59481),
            )
            log.take(LOG_VISIBLE).forEach { entry ->
                Text(
                    text = "• $entry",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color(0xFFE6D7CC),
                )
            }
        }
    }
}

private const val LOG_VISIBLE = 5

private data class StatePalette(
    val accent: Color,
    val onAccent: Color,
    val surface: Color,
    val icon: ImageVector,
)

private fun paletteFor(status: PermissionStatus): StatePalette = when (status) {
    PermissionStatus.NotDetermined -> StatePalette(
        accent = Color(0xFFE25C29),
        onAccent = Color.White,
        surface = Color(0xFF2F241C),
        icon = Icons.Filled.Info,
    )
    PermissionStatus.Granted -> StatePalette(
        accent = Color(0xFF45C490),
        onAccent = Color(0xFF0E2E20),
        surface = Color(0xFF1B3A2A),
        icon = Icons.Filled.Check,
    )
    is PermissionStatus.Denied -> if (status.canAskAgain) StatePalette(
        accent = Color(0xFFF4B860),
        onAccent = Color(0xFF3A2A0E),
        surface = Color(0xFF3A2C1A),
        icon = Icons.Filled.Warning,
    ) else StatePalette(
        accent = Color(0xFFE25C29),
        onAccent = Color.White,
        surface = Color(0xFF3A1E12),
        icon = Icons.Filled.Settings,
    )
    PermissionStatus.Restricted -> StatePalette(
        accent = Color(0xFF8B2E1B),
        onAccent = Color.White,
        surface = Color(0xFF3A1410),
        icon = Icons.Filled.Lock,
    )
}

private fun <T> MutableList<T>.prepend(value: T) {
    add(0, value)
}
