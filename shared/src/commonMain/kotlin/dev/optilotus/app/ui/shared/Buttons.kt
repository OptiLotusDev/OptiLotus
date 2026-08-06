package dev.optilotus.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.optilotus.app.ui.theme.Accent
import dev.optilotus.app.ui.theme.AccentBright
import dev.optilotus.app.ui.theme.TextPrimary
import dev.optilotus.app.ui.theme.TextSecondary

private fun Modifier.pressScale(pressed: Boolean): Modifier = graphicsLayer {
    scaleX = if (pressed) 0.95f else 1f
    scaleY = if (pressed) 0.95f else 1f
}

@Composable
fun RunButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    Row(
        modifier
            .pressScale(isPressed)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(Accent, AccentBright.copy(alpha = 0.85f))))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("▶", color = Color.White, fontSize = 12.sp)
        Spacer(Modifier.width(6.dp))
        Text("Run", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
fun SmallGlassButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier
            .pressScale(isPressed)
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) Accent.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (active) AccentBright else TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun GlyphIconButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = TextPrimary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier
            .size(34.dp)
            .pressScale(isPressed)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = contentColor, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
    }
}