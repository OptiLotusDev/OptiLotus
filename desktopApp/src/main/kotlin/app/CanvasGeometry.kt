package app.ui

import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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

    fun px(value: Dp): Float = with(density) { value.toPx() }

    fun outputSocketCenter(block: PlacedBlock): Offset =
        Offset(block.position.x + widthPx / 2f, block.position.y + heightPx)

    fun inputSocketCenter(block: PlacedBlock): Offset =
        Offset(block.position.x + widthPx / 2f, block.position.y)
}

val LocalCanvasGeometry = staticCompositionLocalOf<CanvasGeometry> { error("LocalCanvasGeometry not provided") }
