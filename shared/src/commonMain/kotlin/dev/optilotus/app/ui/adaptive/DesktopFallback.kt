package dev.optilotus.app.ui.adaptive

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
import dev.optilotus.app.ui.theme.OptiLotusTheme
import dev.optilotus.app.ui.theme.TextPrimary
import dev.optilotus.app.ui.theme.TextSecondary
import dev.optilotus.app.ui.theme.themeBackgroundGradient

/**
 * Common fallback shown on platforms where the spatial desktop canvas is not yet
 * implemented. Big screens on those platforms still get the OptiLotus theme and a
 * clear placeholder until a platform-specific canvas is added.
 */
@Composable
internal fun DesktopFallbackContent() {
    OptiLotusTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(themeBackgroundGradient)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("◈", style = MaterialTheme.typography.headlineLarge, color = TextPrimary)
                Spacer(Modifier.size(16.dp))
                Text(
                    "OptiLotus Desktop Canvas",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    "The spatial block canvas is coming to this platform.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    "Try it on the desktop app, or rotate to the mobile experience.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}