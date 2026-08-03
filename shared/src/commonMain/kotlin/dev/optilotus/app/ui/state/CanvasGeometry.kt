package dev.optilotus.app.ui.state

import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Immutable
class CanvasGeometry(private val density: Density) {
    val width = 260.dp
    val height = 112.dp
    val widthPx: Float get() = with(density) { width.toPx() }
    val heightPx: Float get() = with(density) { height.toPx() }
    val socketRadius = 7.dp
    val socketRadiusPx: Float get() = with(density) { socketRadius.toPx() }
    val connectorHitRadius = 34.dp
    val connectorHitRadiusPx: Float get() = with(density) { connectorHitRadius.toPx() }
    val gridSpacing = 28.dp
    val gridSpacingPx: Float get() = with(density) { gridSpacing.toPx() }
    val chainGap = 16.dp
    val chainGapPx: Float get() = with(density) { chainGap.toPx() }
    val stepPx: Float get() = heightPx + chainGapPx

    fun px(value: Dp): Float = with(density) { value.toPx() }

    fun inputSocketCenter(chainOffset: Offset, index: Int): Offset =
        Offset(chainOffset.x + widthPx / 2f, chainOffset.y + index * stepPx)

    fun outputSocketCenter(chainOffset: Offset, index: Int): Offset =
        Offset(chainOffset.x + widthPx / 2f, chainOffset.y + index * stepPx + heightPx)
}

val LocalCanvasGeometry = staticCompositionLocalOf<CanvasGeometry> { error("LocalCanvasGeometry not provided") }

/**
 * Chain placement maths shared by the palette drop, the reorder drag and the
 * live "make space" gap preview. Kept pure so it can be unit tested.
 */
fun gapIndexForPointer(pointerY: Float, chainTop: Float, stepPx: Float, size: Int): Int =
    ((pointerY - chainTop) / stepPx).roundToInt().coerceIn(0, size)

fun reorderGapIndex(fromIndex: Int, accumulatedDeltaY: Float, stepPx: Float, size: Int): Int =
    (fromIndex + accumulatedDeltaY / stepPx).roundToInt().coerceIn(0, size)

/**
 * Visual slot a block at list index [j] should animate to while a gap is open.
 * [from] is the list index of the block being dragged (reorder) or null for a
 * phantom insertion from the palette. Empty slot lands at [gap].
 */
fun gappedSlotIndex(j: Int, from: Int?, gap: Int, size: Int): Int {
    val g = gap.coerceIn(0, size)
    return when {
        from == null -> if (j >= g) j + 1 else j
        g == size -> { val r = if (j > from) j - 1 else j; r }
        else -> { val r = if (j > from) j - 1 else j; r + (if (r >= g) 1 else 0) }
    }
}