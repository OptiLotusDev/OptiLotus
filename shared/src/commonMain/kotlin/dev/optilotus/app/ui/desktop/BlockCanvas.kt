package dev.optilotus.app.ui.desktop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.optilotus.app.ui.state.BlockProgramStateHolder
import dev.optilotus.app.ui.state.BlockProgramUiState
import dev.optilotus.app.ui.state.CanvasGeometry
import dev.optilotus.app.ui.state.LocalCanvasGeometry
import dev.optilotus.app.ui.state.PlacedBlock
import dev.optilotus.app.ui.theme.Accent
import dev.optilotus.app.ui.theme.Success
import dev.optilotus.app.ui.theme.TextSecondary
import dev.optilotus.app.ui.theme.glassSurface
import kotlin.math.roundToInt

/**
 * Infinite-canvas block editor. Palette drags are reported in root coordinates
 * via [paletteDragPosition]; this composable resolves them against its own
 * position so the same interaction works on desktop, web and tablets.
 */
@Composable
fun BlockCanvas(
    state: BlockProgramUiState,
    holder: BlockProgramStateHolder,
    paletteDragPosition: Offset?,
    onCanvasPositioned: (Offset) -> Unit,
    onCanvasResized: (IntSize) -> Unit,
    modifier: Modifier = Modifier
) {
    val geometry = LocalCanvasGeometry.current
    var canvasRootOffset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val dragPositionLocal = paletteDragPosition?.let { it - canvasRootOffset }
    val dragOnCanvas = dragPositionLocal?.takeIf { position ->
        position.x in 0f..canvasSize.width.toFloat() && position.y in 0f..canvasSize.height.toFloat()
    }
    val hoverTarget = dragOnCanvas?.let { hitOutputSocket(it, state.blocks, geometry) }

    LaunchedEffect(dragOnCanvas, hoverTarget) {
        holder.setDragHover(dragOnCanvas, hoverTarget?.id)
    }

    Box(
        modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFF101420), Color(0xFF0B0F19))))
            .canvasGrid(geometry)
            .onGloballyPositioned {
                canvasRootOffset = it.positionInRoot()
                onCanvasPositioned(it.positionInRoot())
            }
            .onSizeChanged { size ->
                canvasSize = size
                onCanvasResized(size)
                holder.updateCanvasMetrics(size.width.toFloat(), size.height.toFloat(), geometry.widthPx, geometry.heightPx)
            }
    ) {
        state.connections.forEach { connection ->
            val from = state.blocks.firstOrNull { it.id == connection.fromBlockId } ?: return@forEach
            val to = state.blocks.firstOrNull { it.id == connection.toBlockId } ?: return@forEach
            ConnectionLine(from, to, geometry)
        }

        state.entryPointBlockId?.let { rootId ->
            state.blocks.firstOrNull { it.id == rootId }?.let { root ->
                StartMarker(root, geometry)
            }
        }

        state.blocks.forEach { block ->
            BlockNodeView(
                block = block,
                selected = state.selectedBlockId == block.id,
                hoverConnect = state.dragHoverTargetBlockId == block.id,
                onSelect = { holder.selectBlock(block.id) },
                onMovedBy = { delta -> holder.moveBlockBy(block.id, delta) },
                onValueChange = { value -> holder.updateValue(block.id, value) },
                onToggleNewline = { holder.toggleNewline(block.id) },
                onDelete = { holder.deleteBlock(block.id) }
            )
        }

        dragOnCanvas?.let { position ->
            DropPreview(position, willChain = hoverTarget != null, geometry = geometry)
        }

        if (state.blocks.isEmpty()) {
            Box(Modifier.align(Alignment.Center)) {
                EmptyCanvasHint()
            }
        }
    }
}

internal fun hitOutputSocket(position: Offset, blocks: List<PlacedBlock>, geometry: CanvasGeometry): PlacedBlock? =
    blocks.firstOrNull { block ->
        (position - geometry.outputSocketCenter(block)).getDistance() <= geometry.connectorHitRadiusPx
    }

internal fun clampToCanvas(position: Offset, geometry: CanvasGeometry, canvasSize: IntSize): Offset = Offset(
    position.x.coerceIn(0f, (canvasSize.width - geometry.widthPx).coerceAtLeast(0f)),
    position.y.coerceIn(0f, (canvasSize.height - geometry.heightPx).coerceAtLeast(0f))
)

private fun Modifier.canvasGrid(geometry: CanvasGeometry): Modifier = drawBehind {
    val spacing = geometry.gridSpacingPx
    val dotRadius = 1.2.dp.toPx()
    var x = spacing / 2f
    while (x < size.width) {
        var y = spacing / 2f
        while (y < size.height) {
            drawCircle(Color.White.copy(alpha = 0.05f), radius = dotRadius, center = Offset(x, y))
            y += spacing
        }
        x += spacing
    }
}

@Composable
private fun DropPreview(position: Offset, willChain: Boolean, geometry: CanvasGeometry) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        Modifier
            .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
            .size(geometry.width, geometry.height)
            .glassSurface(shape = shape, tint = Accent.copy(alpha = 0.12f), borderVisible = false, elevation = 2.dp, blurred = false)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(if (willChain) Success.copy(alpha = 0.08f) else Accent.copy(alpha = 0.05f), shape)
        )
    }
}

@Composable
private fun StartMarker(root: PlacedBlock, geometry: CanvasGeometry) {
    val input = geometry.inputSocketCenter(root)
    val badgeRadius = geometry.px(13.dp)
    val badgeCenter = Offset(input.x, input.y - geometry.px(38.dp))
    Canvas(Modifier.fillMaxSize()) {
        drawLine(
            color = Success,
            start = Offset(badgeCenter.x, badgeCenter.y + badgeRadius),
            end = input,
            strokeWidth = geometry.px(2.dp),
            cap = StrokeCap.Round
        )
        drawCircle(Success.copy(alpha = 0.22f), radius = badgeRadius + geometry.px(5.dp), center = badgeCenter)
        drawCircle(Success, radius = badgeRadius, center = badgeCenter)
        val triangle = Path().apply {
            moveTo(badgeCenter.x - badgeRadius / 2f, badgeCenter.y - badgeRadius / 2.4f)
            lineTo(badgeCenter.x - badgeRadius / 2f, badgeCenter.y + badgeRadius / 2.4f)
            lineTo(badgeCenter.x + badgeRadius / 2f, badgeCenter.y)
            close()
        }
        drawPath(triangle, Color.White)
    }
}

@Composable
private fun EmptyCanvasHint() {
    Text(
        "Empty canvas\n\nDrag a print or println block from the palette on the left.\nDrop it on a block's bottom socket (●) to chain blocks.",
        style = MaterialTheme.typography.titleMedium,
        color = TextSecondary
    )
}
