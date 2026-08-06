package dev.optilotus.app.ui.desktop

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.optilotus.app.domain.BlockId
import dev.optilotus.app.ui.state.LocalCanvasGeometry
import dev.optilotus.app.ui.state.PlacedBlock
import dev.optilotus.app.ui.state.reorderGapIndex
import dev.optilotus.app.ui.theme.Accent
import dev.optilotus.app.ui.theme.AccentBright
import dev.optilotus.app.ui.theme.Error
import dev.optilotus.app.ui.theme.SocketColor
import dev.optilotus.app.ui.theme.TextPrimary
import dev.optilotus.app.ui.theme.TextSecondary
import dev.optilotus.app.ui.theme.categoryColor
import dev.optilotus.app.ui.theme.glassSurface

@Composable
fun BlockNodeView(
    block: PlacedBlock,
    selected: Boolean,
    index: Int,
    chainLength: Int,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit,
    onReorderStart: (BlockId) -> Unit,
    onReorderMove: (BlockId, Int, Float) -> Unit,
    onReorderEnd: (BlockId) -> Unit,
    onReorderCancel: (BlockId) -> Unit,
    onValueChange: (String) -> Unit,
    onToggleNewline: () -> Unit,
    onDelete: () -> Unit
) {
    val geometry = LocalCanvasGeometry.current
    val color = categoryColor(block.category)
    val shape = RoundedCornerShape(14.dp)
    val viewConfig = LocalViewConfiguration.current

    // Single source of truth for tap-vs-drag on the whole block body.
    val slop = remember { viewConfig.touchSlop }

    Box(
        modifier
            .size(geometry.width, geometry.height)
            .shadow(elevation = if (selected) 16.dp else 8.dp, shape = shape, clip = false)
            .graphicsLayer {
                scaleX = if (selected) 1.02f else 1f
                scaleY = if (selected) 1.02f else 1f
            }
            .pointerInput(block.id, index, chainLength) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var dragging = false
                    var dy = 0f
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.isConsumed) {
                            if (dragging) onReorderCancel(block.id)
                            break
                        }
                        if (!change.pressed) {
                            if (dragging) onReorderEnd(block.id) else onSelect()
                            break
                        }
                        val distance = (change.position - down.position).getDistance()
                        if (!dragging) {
                            if (distance <= slop) { continue }
                            dragging = true
                            onReorderStart(block.id)
                        }
                        val delta = change.position - change.previousPosition
                        if (delta != Offset.Zero) {
                            change.consume()
                            dy += delta.y
                            onReorderMove(
                                block.id,
                                reorderGapIndex(index, dy, geometry.stepPx, chainLength),
                                dy
                            )
                        }
                    }
                }
            }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .glassSurface(shape = shape, tint = color.copy(alpha = 0.32f), elevation = 0.dp, blurred = false)
        ) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                                Spacer(Modifier.width(8.dp))
                Text(
                    "print",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(Modifier.width(8.dp))
                ValueEditor(value = block.literalValue, onValueChange = onValueChange, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(6.dp))
                NewlineChip(addNewline = block.addNewline, onClick = onToggleNewline)
                if (selected) {
                    Spacer(Modifier.width(6.dp))
                    DeleteGlyph(onDelete)
                }
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
                .size(16.dp)
                .offset(y = (-7).dp)
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
                .size(18.dp)
                .offset(y = 9.dp)
        ) {
            val r = geometry.socketRadiusPx
            drawCircle(SocketColor, radius = r, center = center)
            drawCircle(
                Color.White.copy(alpha = 0.45f),
                radius = r / 2.8f,
                center = Offset(center.x - r / 4f, center.y - r / 4f)
            )
        }
    }
}

@Composable
private fun ValueEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier
            .height(28.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.92f))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused },
            textStyle = TextStyle(
                color = Color(0xFF1A1F2E),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            ),
            singleLine = true,
            cursorBrush = SolidColor(Accent),
            decorationBox = { innerTextField ->
                if (value.isEmpty() && !focused) {
                    Text(
                        "type a value…",
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun NewlineChip(addNewline: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    Row(
        Modifier
            .graphicsLayer {
                scaleX = if (isPressed) 0.94f else 1f
                scaleY = if (isPressed) 0.94f else 1f
            }
            .clip(RoundedCornerShape(50))
            .background(if (addNewline) Accent.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.06f))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "\\n",
            color = if (addNewline) AccentBright else TextSecondary,
            fontSize = 9.sp,
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
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text("✕", color = Error, fontSize = 9.sp)
    }
}