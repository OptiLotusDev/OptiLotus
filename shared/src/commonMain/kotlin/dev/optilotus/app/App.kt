package dev.optilotus.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import dev.optilotus.app.ui.adaptive.DesktopApp
import dev.optilotus.app.ui.adaptive.UIMode
import dev.optilotus.app.ui.adaptive.rememberUIMode
import dev.optilotus.app.ui.theme.OptiLotusTheme
import dev.optilotus.app.ui.theme.TextPrimary
import dev.optilotus.app.ui.theme.TextSecondary
import dev.optilotus.app.ui.theme.themeBackgroundGradient

/**
 * Adaptive root: big screens get the spatial canvas UI, small vertical screens
 * get the mobile UI. Both share the same domain state model.
 */
@Composable
fun App() {
    when (rememberUIMode()) {
        UIMode.DESKTOP -> DesktopApp()
        UIMode.MOBILE -> MobileApp()
    }
}

/**
 * Compact-screen experience. Placeholder until the drill-down / bottom-sheet
 * flow lands; already themed so it never shows the template UI.
 */
@Composable
fun MobileApp() {
    OptiLotusTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(themeBackgroundGradient)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("◈", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                Spacer(Modifier.size(12.dp))
                Text("OptiLotus", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Spacer(Modifier.size(8.dp))
                Text("Mobile UI coming soon", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}
