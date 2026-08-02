package dev.optilotus.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.optilotus.app.ui.state.BlockCategory

/**
 * Optional Haze state for real gaussian backdrop blur on glass surfaces.
 * Provided by the adaptive UI roots (e.g. [dev.optilotus.app.ui.adaptive.DesktopApp]);
 * surfaces render a flat translucent tint when it is absent.
 */
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

val CanvasBackgroundStart = Color(0xFF0B0F1A)
val CanvasBackgroundEnd = Color(0xFF151B2B)
val GlassSurface = Color(0xFF1E2638)
val GlassSurfaceLight = Color(0xFF2A3350)
val Accent = Color(0xFF7C8CFF)
val AccentBright = Color(0xFFA8B4FF)
val Success = Color(0xFF4ADE80)
val Error = Color(0xFFFF6B6B)
val TextPrimary = Color(0xFFE7EBF4)
val TextSecondary = Color(0xFF99A3B8)
val SocketColor = Color(0xFF9DB0FF)

val themeBackgroundGradient = listOf(CanvasBackgroundStart, CanvasBackgroundEnd)

fun categoryColor(category: BlockCategory): Color = when (category) {
    BlockCategory.INPUT_OUTPUT -> Color(0xFF7C8CFF)
    BlockCategory.TEXT -> Color(0xFFF59E0B)
    BlockCategory.MATH -> Color(0xFF34D399)
    BlockCategory.LOGIC -> Color(0xFFF472B6)
    BlockCategory.CONTROL_FLOW -> Color(0xFF60A5FA)
    BlockCategory.VARIABLES -> Color(0xFFA78BFA)
}

/**
 * Liquid-glass surface: a real gaussian blur of whatever sits behind it (via
 * Haze) combined with a translucent tint and a specular top-to-bottom border.
 * When no [LocalHazeState] is provided (or [blurred] is false) it falls back to
 * a flat translucent tint, which is also what old Android devices get.
 */
@Composable
fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(18.dp),
    tint: Color = GlassSurface,
    borderVisible: Boolean = true,
    elevation: Dp = 10.dp,
    blurred: Boolean = true
): Modifier {
    val border = if (borderVisible) {
        Modifier.border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.05f))
            ),
            shape = shape
        )
    } else {
        Modifier
    }
    val clipped = this
        .shadow(elevation = elevation, shape = shape, clip = false)
        .clip(shape)
    val hazeState = LocalHazeState.current
    val frosted = if (hazeState != null && blurred) {
        clipped.hazeEffect(state = hazeState) {
            blurRadius = 16.dp
            backgroundColor = Color.Transparent
            tints = emptyList()
            fallbackTint = HazeTint(Color.Transparent)
            noiseFactor = 0f
        }
    } else {
        clipped
    }
    return frosted
        .background(Brush.linearGradient(listOf(tint.copy(alpha = 0.80f), tint.copy(alpha = 0.62f))), shape)
        .then(border)
}

private val OptiLotusColorScheme = darkColorScheme(
    primary = Accent,
    secondary = AccentBright,
    background = CanvasBackgroundStart,
    surface = GlassSurface,
    onSurface = TextPrimary,
    onBackground = TextPrimary,
    error = Error
)

@Composable
fun OptiLotusTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = OptiLotusColorScheme, content = content)
}