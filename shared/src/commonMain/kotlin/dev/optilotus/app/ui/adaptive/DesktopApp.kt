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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.optilotus.app.ui.desktop.BlockCanvas
import dev.optilotus.app.ui.desktop.ConsolePanel
import dev.optilotus.app.ui.desktop.InspectorPanel
import dev.optilotus.app.ui.shared.BlockPalette
import dev.optilotus.app.ui.shared.PaletteDragGhost
import dev.optilotus.app.ui.shared.TopBar
import dev.optilotus.app.ui.state.BlockProgramStateHolder
import dev.optilotus.app.ui.state.CanvasGeometry
import dev.optilotus.app.ui.state.LocalCanvasGeometry
import dev.optilotus.app.ui.state.PaletteItem
import dev.optilotus.app.ui.state.gapIndexForPointer
import dev.optilotus.app.ui.theme.LocalHazeState
import dev.optilotus.app.ui.theme.OptiLotusTheme
import dev.optilotus.app.ui.theme.themeBackgroundGradient

/**
 * Spatial canvas UI for big screens (desktop, web, large tablets). The block
 * palette and canvas live side by side and share one [BlockProgramStateHolder];
 * palette drags are coordinated here in root coordinates so dropping works the
 * same on every platform.
 */
@Composable
fun DesktopApp() {
    OptiLotusTheme {
        val holder = remember { BlockProgramStateHolder() }
        val state by holder.state.collectAsState()
        val density = LocalDensity.current
        val geometry = remember(density) { CanvasGeometry(density) }

        var canvasRootOffset by remember { mutableStateOf(Offset.Zero) }
        var canvasSizePx by remember { mutableStateOf(IntSize.Zero) }

        var dragItem by remember { mutableStateOf<PaletteItem?>(null) }
        var dragPositionRoot by remember { mutableStateOf(Offset.Zero) }
        var dragActive by remember { mutableStateOf(false) }

        val hazeState = rememberHazeState()

        fun finishDrop(item: PaletteItem, rootPosition: Offset) {
            dragActive = false
            dragItem = null
            val position = rootPosition - canvasRootOffset
            val onCanvas =
                position.x in 0f..canvasSizePx.width.toFloat() &&
                    position.y in 0f..canvasSizePx.height.toFloat()
            if (onCanvas) {
                val insertIndex = gapIndexForPointer(position.y, state.chainOffset.y, geometry.stepPx, state.blocks.size)
                holder.addPrintBlock(item.addNewline, insertIndex)
            }
        }

        CompositionLocalProvider(
            LocalCanvasGeometry provides geometry,
            LocalHazeState provides hazeState
        ) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(themeBackgroundGradient))
                        .hazeSource(hazeState)
                ) {}
                Column(Modifier.fillMaxSize()) {
                    TopBar(state, holder)
                    Row(Modifier.weight(1f).fillMaxSize()) {
                        BlockPalette(
                            onDragStarted = { item, position ->
                                dragItem = item
                                dragPositionRoot = position
                                dragActive = true
                            },
                            onDragMoved = { position -> dragPositionRoot = position },
                            onDragEnded = { item, position -> finishDrop(item, position) },
                            onDragCancelled = {
                                dragActive = false
                                dragItem = null
                            }
                        )
                        BlockCanvas(
                            state = state,
                            holder = holder,
                            paletteDragPosition = if (dragActive) dragPositionRoot else null,
                            onCanvasPositioned = { canvasRootOffset = it },
                            onCanvasResized = { canvasSizePx = it },
                            modifier = Modifier.weight(1f).hazeSource(hazeState)
                        )
                    }
                }
                val item = dragItem
                if (dragActive && item != null) {
                    PaletteDragGhost(item, dragPositionRoot, geometry)
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
