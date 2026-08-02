package dev.optilotus.app.ui.desktop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.optilotus.app.ui.state.CanvasGeometry
import dev.optilotus.app.ui.state.PlacedBlock
import dev.optilotus.app.ui.theme.Accent
import dev.optilotus.app.ui.theme.AccentBright

@Composable
fun ConnectionLine(from: PlacedBlock, to: PlacedBlock, geometry: CanvasGeometry) {
    val start = geometry.outputSocketCenter(from)
    val end = geometry.inputSocketCenter(to)
    Canvas(Modifier.fillMaxSize()) {
        val dist = (end.y - start.y).coerceAtLeast(0f)
        val controlOffset = (dist / 2f).coerceAtLeast(48.dp.toPx())
        val path = Path().apply {
            moveTo(start.x, start.y)
            cubicTo(start.x, start.y + controlOffset, end.x, end.y - controlOffset, end.x, end.y)
        }
        drawPath(path, Accent.copy(alpha = 0.18f), style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round))
        drawPath(path, Accent, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(AccentBright, radius = 5.dp.toPx(), center = end)
    }
}