package dev.optilotus.app.ui.desktop

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.optilotus.app.domain.BlockId
import dev.optilotus.app.ui.state.BlockProgramStateHolder
import dev.optilotus.app.ui.state.BlockProgramUiState
import dev.optilotus.app.ui.state.CanvasGeometry
import dev.optilotus.app.ui.state.LocalCanvasGeometry
import dev.optilotus.app.ui.state.gapIndexForPointer
import dev.optilotus.app.ui.state.gappedSlotIndex
import dev.optilotus.app.ui.theme.Accent
import dev.optilotus.app.ui.theme.Success
import dev.optilotus.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val chainSpring = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)

/**
 * Lego-style block editor. Blocks stack into a single vertical chain anchored
 * at [BlockProgramUiState.chainOffset]; the whole chain pans when the empty
 * canvas is dragged. Blocks reorder inside the chain (start/middle/end) with a
 * live spring gap that opens beneath the drag and closes on drop.
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
    val paletteGap = dragOnCanvas?.let {
        gapIndexForPointer(it.y, state.chainOffset.y, geometry.stepPx, state.blocks.size)
    }

    val draggingId = state.draggingBlockId
    val dragFrom = draggingId?.let { id -> state.blocks.indexOfFirst { b -> b.id == id } }
        .takeIf { it != null && it >= 0 }

    // A gap is open only while a reorder drag or a palette drop is live.
    val layoutGap: Int? = when {
        dragFrom != null && draggingId != null -> state.dragInsertIndex
        paletteGap != null -> paletteGap
        else -> null
    }
    val gapFrom: Int? = if (draggingId != null && dragFrom != null) dragFrom else null

    var draggedDeltaY by remember { mutableStateOf(0f) }

    val yAnims = remember { mutableStateMapOf<BlockId, Animatable<Float, AnimationVector1D>>() }
    val scope = rememberCoroutineScope()
    val idsKey = state.blocks.joinToString(",") { it.id.value }
    LaunchedEffect(idsKey) { yAnims.keys.retainAll(state.blocks.map { it.id }.toSet()) }

    val currentState by rememberUpdatedState(state)
    val currentGeometry by rememberUpdatedState(geometry)
    val panSlop = LocalViewConfiguration.current.touchSlop

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
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    // Panning only starts off the chain; drags on the chain are
                    // handled by the blocks' reorder gesture.
                    val s = currentState
                    val g = currentGeometry
                    val chainTop = s.chainOffset.y
                    val chainBottom = chainTop + (s.blocks.size - 1) * g.stepPx + g.heightPx
                    val onChain = s.blocks.isNotEmpty() &&
                        down.position.x in s.chainOffset.x..(s.chainOffset.x + g.widthPx) &&
                        down.position.y in chainTop..chainBottom
                    if (onChain) return@awaitEachGesture

                    var panning = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.isConsumed) break
                        if (!change.pressed) break
                        if (!panning) {
                            val distance = (change.position - down.position).getDistance()
                            if (distance <= panSlop) { continue }
                            panning = true
                            holder.selectBlock(null)
                        }
                        val delta = change.position - change.previousPosition
                        if (delta != Offset.Zero) {
                            change.consume()
                            holder.panChain(delta)
                        }
                    }
                }
            }
    ) {
        ChainConnectors(state, layoutGap, yAnims, geometry)

        state.blocks.forEachIndexed { j, block ->
            val targetSlot = if (block.id == draggingId) j else {
                if (layoutGap != null) gappedSlotIndex(j, gapFrom, layoutGap, state.blocks.size) else j
            }
            val targetRelY = targetSlot * geometry.stepPx
            val anim = yAnims.getOrPut(block.id) { Animatable(targetRelY) }
            LaunchedEffect(anim, targetRelY) { anim.animateTo(targetRelY, chainSpring) }

            BlockNodeView(
                block = block,
                selected = state.selectedBlockId == block.id,
                index = j,
                chainLength = state.blocks.size,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            state.chainOffset.x.roundToInt(),
                            (state.chainOffset.y + anim.value).roundToInt()
                        )
                    }
                    .graphicsLayer {
                        translationY = if (block.id == draggingId) draggedDeltaY else 0f
                    },
                onSelect = { holder.selectBlock(block.id) },
                onReorderStart = { id ->
                    holder.setDragInsert(id, j)
                    draggedDeltaY = 0f
                },
                onReorderMove = { id, gap, deltaY ->
                    holder.setDragInsert(id, gap)
                    draggedDeltaY = deltaY
                },
                onReorderEnd = { id ->
                    scope.launch {
                        val anim = yAnims[id]
                        anim?.snapTo(anim.value + draggedDeltaY)
                        val moved = holder.moveBlockTo(id)
                        if (!moved) {
                            anim?.animateTo(j * geometry.stepPx, chainSpring)
                        }
                        draggedDeltaY = 0f
                    }
                },
                onReorderCancel = { id ->
                    scope.launch {
                        val anim = yAnims[id]
                        anim?.snapTo(anim.value + draggedDeltaY)
                        holder.moveBlockTo(id, j)
                        anim?.animateTo(j * geometry.stepPx, chainSpring)
                        draggedDeltaY = 0f
                    }
                },
                onValueChange = { value -> holder.updateValue(block.id, value) },
                onToggleNewline = { holder.toggleNewline(block.id) },
                onDelete = { holder.deleteBlock(block.id) }
            )
        }

        if (state.blocks.isNotEmpty()) {
            StartMarker(state, geometry)
        }

        layoutGap?.let { gap -> GapIndicator(state, gap, geometry) }

        if (state.blocks.isEmpty()) {
            Box(Modifier.align(Alignment.Center)) {
                EmptyCanvasHint()
            }
        }
    }
}

/** Straight, no-curve connectors between consecutive blocks, hidden while a gap is open. */
@Composable
private fun ChainConnectors(
    state: BlockProgramUiState,
    layoutGap: Int?,
    yAnims: Map<BlockId, Animatable<Float, AnimationVector1D>>,
    geometry: CanvasGeometry
) {
    Canvas(Modifier.fillMaxSize()) {
        if (layoutGap != null) return@Canvas
        val x = state.chainOffset.x + geometry.widthPx / 2f
        for (k in 0 until state.blocks.size - 1) {
            val a = state.blocks[k]
            val b = state.blocks[k + 1]
            val ya = state.chainOffset.y + (yAnims[a.id]?.value
                ?: (k * geometry.stepPx)) + geometry.heightPx
            val yb = state.chainOffset.y + (yAnims[b.id]?.value ?: ((k + 1) * geometry.stepPx))
            drawLine(
                color = Accent.copy(alpha = 0.18f),
                start = Offset(x, ya),
                end = Offset(x, yb),
                strokeWidth = geometry.px(3.dp),
                cap = StrokeCap.Round
            )
        }
    }
}

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

/** Pulsing insertion line shown where a drag or palette drop would land. */
@Composable
private fun GapIndicator(state: BlockProgramUiState, gap: Int, geometry: CanvasGeometry) {
    val transition = rememberInfiniteTransition(label = "gap")
    val pulse by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(600), repeatMode = RepeatMode.Reverse),
        label = "gapPulse"
    )
    Canvas(Modifier.fillMaxSize()) {
        val y = state.chainOffset.y + gap * geometry.stepPx
        val startX = state.chainOffset.x + geometry.px(28.dp)
        val endX = state.chainOffset.x + geometry.widthPx - geometry.px(28.dp)
        val x = (startX + endX) / 2f
        drawLine(
            color = Accent.copy(alpha = pulse),
            start = Offset(startX, y),
            end = Offset(endX, y),
            strokeWidth = geometry.px(2.dp),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Accent.copy(alpha = (pulse + 0.2f).coerceAtMost(1f)),
            radius = geometry.px(5.dp),
            center = Offset(x, y)
        )
    }
}

@Composable
private fun StartMarker(state: BlockProgramUiState, geometry: CanvasGeometry) {
    val input = geometry.inputSocketCenter(state.chainOffset, 0)
    val badgeRadius = geometry.px(12.dp)
    val badgeCenter = Offset(input.x - geometry.px(90.dp), input.y)
    Canvas(Modifier.fillMaxSize()) {
        drawLine(
            color = Success,
            start = Offset(badgeCenter.x + badgeRadius + geometry.px(4.dp), badgeCenter.y),
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
        "Empty chain\n\nDrag a print block from the palette and drop it onto the canvas. Drop between blocks to insert mid-chain, or drag blocks up and down to reorder.",
        style = MaterialTheme.typography.titleMedium,
        color = TextSecondary
    )
}