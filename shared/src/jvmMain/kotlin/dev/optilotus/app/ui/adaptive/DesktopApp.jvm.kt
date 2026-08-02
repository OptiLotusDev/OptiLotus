package dev.optilotus.app.ui.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.optilotus.app.ui.desktop.BlockCanvas
import dev.optilotus.app.ui.desktop.ConsolePanel
import dev.optilotus.app.ui.desktop.InspectorPanel
import dev.optilotus.app.ui.shared.BlockPalette
import dev.optilotus.app.ui.shared.TopBar
import dev.optilotus.app.ui.state.BlockProgramStateHolder
import dev.optilotus.app.ui.state.CanvasGeometry
import dev.optilotus.app.ui.state.LocalCanvasGeometry
import dev.optilotus.app.ui.theme.OptiLotusTheme
import dev.optilotus.app.ui.theme.themeBackgroundGradient

@Composable
actual fun DesktopApp() {
    OptiLotusTheme {
        val holder = remember { BlockProgramStateHolder() }
        val state by holder.state.collectAsState()
        val density = LocalDensity.current
        val geometry = remember(density) { CanvasGeometry(density) }

        CompositionLocalProvider(LocalCanvasGeometry provides geometry) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(themeBackgroundGradient))
            ) {
                Column(Modifier.fillMaxSize()) {
                    TopBar(state, holder)
                    Row(Modifier.weight(1f).fillMaxSize()) {
                        BlockPalette()
                        BlockCanvas(state, holder, Modifier.weight(1f))
                    }
                }
                if (state.inspectorVisible) {
                    InspectorPanel(state, holder, Modifier.align(Alignment.TopEnd).padding(top = 96.dp))
                }
                if (state.consoleVisible) {
                    ConsolePanel(state, holder, Modifier.align(Alignment.BottomEnd))
                }
            }
        }
    }
}