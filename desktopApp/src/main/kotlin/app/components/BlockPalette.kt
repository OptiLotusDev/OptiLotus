package app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferAction
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.DragAndDropTransferable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ui.BlockPaletteItems
import app.ui.PaletteItem
import app.ui.theme.TextPrimary
import app.ui.theme.TextSecondary
import app.ui.theme.categoryColor
import app.ui.theme.glassSurface
import java.awt.datatransfer.StringSelection

@Composable
fun BlockPalette(modifier: Modifier = Modifier) {
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
            PaletteItemCard(item)
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
                "Drop a block on a block's bottom socket (●) to chain them, then press ▶ Run.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
private fun PaletteItemCard(item: PaletteItem) {
    val color = categoryColor(item.category)
    Box(
        Modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(14.dp), tint = color.copy(alpha = 0.28f), elevation = 4.dp)
            .dragAndDropSource(
                drawDragDecoration = {
                    drawRoundRect(
                        color = color.copy(alpha = 0.85f),
                        topLeft = Offset(0f, size.height * 0.2f),
                        size = Size(size.width, size.height * 0.6f),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )
                },
                transferData = { offset ->
                    DragAndDropTransferData(
                        transferable = DragAndDropTransferable(StringSelection(item.id)),
                        supportedActions = listOf(DragAndDropTransferAction.Copy, DragAndDropTransferAction.Move),
                        dragDecorationOffset = offset,
                        onTransferCompleted = {}
                    )
                }
            )
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
