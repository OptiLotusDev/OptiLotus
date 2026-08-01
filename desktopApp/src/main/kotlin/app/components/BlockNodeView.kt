package app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ui.LocalCanvasGeometry
import app.ui.PlacedBlock
import app.ui.theme.Accent
import app.ui.theme.AccentBright
import app.ui.theme.Error
import app.ui.theme.SocketColor
import app.ui.theme.Success
import app.ui.theme.TextPrimary
import app.ui.theme.TextSecondary
import app.ui.theme.categoryColor
import app.ui.theme.glassSurface
import kotlin.math.roundToInt

@Composable
fun BlockNodeView(
    block: PlacedBlock,
    selected: Boolean,
    hoverConnect: Boolean,
    onSelect: () -> Unit,
    onMovedBy: (Offset) -> Unit,
    onValueChange: (String) -> Unit,
    onToggleNewline: () -> Unit,
    onDelete: () -> Unit
) {
    val geometry = LocalCanvasGeometry.current
    val color = categoryColor(block.category)
    val shape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        Modifier
            .offset { IntOffset(block.position.x.roundToInt(), block.position.y.roundToInt()) }
            .size(geometry.width, geometry.height)
            .shadow(elevation = if (selected) 18.dp else 10.dp, shape = shape, clip = false)
            .graphicsLayer {
                scaleX = if (selected) 1.015f else 1f
                scaleY = if (selected) 1.015f else 1f
            }
            .clickable(interactionSource = interactionSource, indication = null) { onSelect() }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .glassSurface(shape = shape, tint = color.copy(alpha = 0.30f), elevation = 0.dp)
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .pointerInput(block.id) {
                            detectDragGestures(
                                onDragStart = { onSelect() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onMovedBy(dragAmount)
                                }
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⠿", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (block.addNewline) "println" else "print",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.weight(1f))
                    NewlineChip(addNewline = block.addNewline, onClick = onToggleNewline)
                    if (selected) {
                        Spacer(Modifier.width(6.dp))
                        DeleteGlyph(onDelete)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "value",
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.width(38.dp)
                    )
                    ValueSlot(value = block.literalValue, onValueChange = onValueChange)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    if (block.addNewline) "output: \"…\\n\"" else "output: \"…\"",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (selected) {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(1.5.dp, Accent, shape)
            )
        }

        Canvas(
            Modifier
                .align(Alignment.TopCenter)
                .size(18.dp)
                .offset(y = (-9).dp)
        ) {
            drawArc(
                color = SocketColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(0f, 0f),
                size = this.size
            )
        }

        Canvas(
            Modifier
                .align(Alignment.BottomCenter)
                .size(20.dp)
                .offset(y = 10.dp)
        ) {
            val r = geometry.socketRadiusPx
            if (hoverConnect) {
                drawCircle(Success.copy(alpha = 0.30f), radius = r + 9.dp.toPx(), center = center)
            }
            drawCircle(if (hoverConnect) Success else SocketColor, radius = r, center = center)
            drawCircle(
                Color.White.copy(alpha = 0.45f),
                radius = r / 2.8f,
                center = Offset(center.x - r / 4f, center.y - r / 4f)
            )
        }
    }
}

@Composable
private fun NewlineChip(addNewline: Boolean, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (addNewline) Accent.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
            .graphicsLayer {
                scaleX = if (pressed) 0.94f else 1f
                scaleY = if (pressed) 0.94f else 1f
            }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                })
            }
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "\\n",
            color = if (addNewline) AccentBright else TextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun DeleteGlyph(onClick: () -> Unit) {
    Box(
        Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text("✕", color = Error, fontSize = 9.sp)
    }
}

@Composable
private fun ValueSlot(value: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
        cursorBrush = SolidColor(Accent),
        decorationBox = { innerTextField ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                if (value.isEmpty()) {
                    Text("type a value…", color = TextSecondary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                }
                innerTextField()
            }
        }
    )
}
