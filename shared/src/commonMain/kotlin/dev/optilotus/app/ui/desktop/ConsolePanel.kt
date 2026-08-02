package dev.optilotus.app.ui.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.optilotus.app.ui.shared.GlyphIconButton
import dev.optilotus.app.ui.state.BlockProgramStateHolder
import dev.optilotus.app.ui.state.BlockProgramUiState
import dev.optilotus.app.ui.theme.Error
import dev.optilotus.app.ui.theme.TextPrimary
import dev.optilotus.app.ui.theme.TextSecondary
import dev.optilotus.app.ui.theme.glassSurface
import kotlin.math.roundToInt

@Composable
fun ConsolePanel(state: BlockProgramUiState, holder: BlockProgramStateHolder, modifier: Modifier = Modifier) {
    var windowOffset by remember { mutableStateOf(Offset.Zero) }
    var width by remember { mutableStateOf(560.dp) }
    var height by remember { mutableStateOf(300.dp) }
    val density = LocalDensity.current
    val scrollState = rememberScrollState()

    LaunchedEffect(state.output.size, state.errors.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Box(
        modifier
            .padding(16.dp)
            .offset { IntOffset(windowOffset.x.roundToInt(), windowOffset.y.roundToInt()) }
            .width(width)
            .height(height)
            .glassSurface(shape = RoundedCornerShape(20.dp))
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            windowOffset += dragAmount
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Console", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.width(10.dp))
                Text(
                    "${state.output.size} lines · ${state.errors.size} errors",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { holder.runProgram() }) { Text("Run") }
                TextButton(onClick = { holder.clearConsole() }) { Text("Clear") }
                GlyphIconButton("✕", onClick = { holder.toggleConsole() })
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xCC080B12), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(12.dp))
                    .verticalScroll(scrollState)
                    .padding(12.dp)
            ) {
                if (state.output.isEmpty() && state.errors.isEmpty()) {
                    Text(
                        "No output yet. Press ▶ Run to execute the block program.",
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                } else {
                    val consoleText = state.output.joinToString("")
                    Column {
                        consoleText.split('\n').forEach { line ->
                            Text(line, color = TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 18.sp)
                        }
                        state.errors.forEach { error ->
                            Row {
                                Text("✕ ", color = Error, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                Text(error, color = Error, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .size(24.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        width = (width + with(density) { dragAmount.x.toDp() }).coerceIn(360.dp, 900.dp)
                        height = (height + with(density) { dragAmount.y.toDp() }).coerceIn(180.dp, 620.dp)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("⤡", color = TextSecondary, fontSize = 12.sp)
        }
    }
}