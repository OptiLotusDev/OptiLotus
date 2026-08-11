package dev.optilotus.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.optilotus.app.ui.state.CanvasGeometry
import dev.optilotus.app.ui.state.PaletteItem
import dev.optilotus.app.ui.state.BlockPaletteItems
import dev.optilotus.app.ui.theme.TextPrimary
import dev.optilotus.app.ui.theme.TextSecondary
import dev.optilotus.app.ui.theme.categoryColor
import dev.optilotus.app.ui.theme.glassSurface
import kotlin.math.roundToInt

/**
 * Palette of blocks to drag onto the canvas. Drag events report positions in
 * root (window) coordinates so the owning layout can preview and drop anywhere,
 * which works the same on desktop, web and tablets.
 */
@Composable
fun BlockPalette(
    onDragStarted: (PaletteItem, rootPosition: Offset) -> Unit,
    onDragMoved: (rootPosition: Offset) -> Unit,
    onDragEnded: (PaletteItem, rootPosition: Offset) -> Unit,
    onDragCancelled: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxHeight()
            .padding(12.dp)
            .glassSurface(shape = RoundedCornerShape(20.dp))
            .width(232.dp)
            .padding(14.dp)
    ) {
        Text("Palette", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text("Drag onto the canvas", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Spacer(Modifier.size(14.dp))
        BlockPaletteItems.forEach { item ->
            PaletteItemCard(
                item = item,
                onDragStarted = onDragStarted,
                onDragMoved = onDragMoved,
                onDragEnded = onDragEnded,
                onDragCancelled = onDragCancelled
            )
            Spacer(Modifier.size(8.dp))
        }
        Spacer(Modifier.size(16.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .background(TextSecondary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                "Drop a print block onto the chain. Drop it between blocks to insert mid-chain, or drag placed blocks up and down to reorder. Press ▶ Run to execute.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

/**
 * Floating glass preview that follows the palette drag, rendered in the owning
 * layout's coordinate space.
 */
@Composable
fun PaletteDragGhost(item: PaletteItem, rootPosition: Offset, geometry: CanvasGeometry) {
    val color = categoryColor(item.category)
    val density = LocalDensity.current
    val offsetUp = with(density) { Offset((-116).dp.toPx(), (-64).dp.toPx()) }
    Box(
        Modifier
            .offset { IntOffset((rootPosition.x + offsetUp.x).roundToInt(), (rootPosition.y + offsetUp.y).roundToInt()) }
            .width(232.dp)
            .glassSurface(shape = RoundedCornerShape(14.dp), tint = color.copy(alpha = 0.28f), elevation = 8.dp, blurred = false)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(color, CircleShape))
            Spacer(Modifier.width(10.dp))
            Text(
                item.label,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PaletteItemCard(
    item: PaletteItem,
    onDragStarted: (PaletteItem, Offset) -> Unit,
    onDragMoved: (Offset) -> Unit,
    onDragEnded: (PaletteItem, Offset) -> Unit,
    onDragCancelled: () -> Unit
) {
    val color = categoryColor(item.category)
    var rootOffset by remember { mutableStateOf(Offset.Zero) }
    val currentRootOffset = rememberUpdatedState(rootOffset)

    Box(
        Modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(14.dp), tint = color.copy(alpha = 0.28f), elevation = 4.dp)
            .onGloballyPositioned { rootOffset = it.positionInRoot() }
            .pointerInput(item.id) {
                var lastRootPosition = Offset.Zero
                detectDragGestures(
                    onDragStart = { down ->
                        lastRootPosition = currentRootOffset.value + down
                        onDragStarted(item, lastRootPosition)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        lastRootPosition = currentRootOffset.value + change.position
                        onDragMoved(lastRootPosition)
                    },
                    onDragEnd = { onDragEnded(item, lastRootPosition) },
                    onDragCancel = onDragCancelled
                )
            }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(color, CircleShape))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(item.label, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                Text(item.description, color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}
