package xyz.ksharma.aagya.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS entry point. The Swift side calls this to obtain a `UIViewController`
 * hosting the shared Compose Multiplatform sample.
 */
@Suppress("FunctionName")
fun SampleViewController(): UIViewController = ComposeUIViewController {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFE25C29),
            secondary = Color(0xFFF4B860),
            background = Color(0xFF1A1410),
            surface = Color(0xFF251D17),
        ),
    ) {
        SampleApp()
    }
}
