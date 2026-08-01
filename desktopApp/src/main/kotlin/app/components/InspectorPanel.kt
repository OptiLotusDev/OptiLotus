package app.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ui.BlockProgramStateHolder
import app.ui.BlockProgramUiState
import app.ui.theme.Accent
import app.ui.theme.Error
import app.ui.theme.TextPrimary
import app.ui.theme.TextSecondary
import app.ui.theme.glassSurface
import kotlin.math.roundToInt

@Composable
fun InspectorPanel(state: BlockProgramUiState, holder: BlockProgramStateHolder, modifier: Modifier = Modifier) {
    val block = state.blocks.firstOrNull { it.id == state.selectedBlockId }
    var windowOffset by remember { mutableStateOf(Offset.Zero) }
    var width by remember { mutableStateOf(320.dp) }
    var height by remember { mutableStateOf(340.dp) }
    val density = LocalDensity.current

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
                Text("Inspector", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.weight(1f))
                GlyphIconButton("✕", onClick = { holder.toggleInspector() })
            }
            if (block != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    if (block.addNewline) "println" else "print",
                    color = Accent,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )
                Text("id ${block.id.value.take(8)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(14.dp))
                Text("Value", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                OutlinedTextField(
                    value = block.literalValue,
                    onValueChange = { holder.updateValue(block.id, it) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    minLines = 2
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Append newline", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Spacer(Modifier.weight(1f))
                    Switch(checked = block.addNewline, onCheckedChange = { holder.toggleNewline(block.id) })
                }
                Spacer(Modifier.height(14.dp))
                Row {
                    Button(onClick = { holder.runProgram() }) { Text("Run") }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { holder.deleteBlock(block.id) }) { Text("Delete", color = Error) }
                }
            } else {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Select a block on the canvas to inspect and edit its value and newline behaviour.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .size(24.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        width = (width + with(density) { dragAmount.x.toDp() }).coerceIn(280.dp, 620.dp)
                        height = (height + with(density) { dragAmount.y.toDp() }).coerceIn(280.dp, 720.dp)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("⤡", color = TextSecondary, fontSize = 12.sp)
        }
    }
}
