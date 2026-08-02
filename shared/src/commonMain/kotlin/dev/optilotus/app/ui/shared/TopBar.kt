package dev.optilotus.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.optilotus.app.ui.state.BlockProgramStateHolder
import dev.optilotus.app.ui.state.BlockProgramUiState
import dev.optilotus.app.ui.theme.Accent
import dev.optilotus.app.ui.theme.AccentBright
import dev.optilotus.app.ui.theme.GlassSurface
import dev.optilotus.app.ui.theme.TextPrimary
import dev.optilotus.app.ui.theme.TextSecondary
import dev.optilotus.app.ui.theme.glassSurface

@Composable
fun TopBar(state: BlockProgramUiState, holder: BlockProgramStateHolder) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .glassSurface(shape = RoundedCornerShape(16.dp), tint = GlassSurface.copy(alpha = 0.7f))
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(34.dp)
                .background(Brush.linearGradient(listOf(Accent, AccentBright)), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("◈", color = Color.White, fontSize = 16.sp)
        }
        Spacer(Modifier.width(10.dp))
        Text("OptiLotus", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(8.dp))
        Text("Block Program Studio", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Spacer(Modifier.weight(1f))
        Text(
            "${state.blocks.size} blocks · ${state.connections.size} links · ${state.output.size} output lines",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(Modifier.width(16.dp))
        RunButton(onClick = { holder.runProgram() })
        Spacer(Modifier.width(8.dp))
        SmallGlassButton("Console", active = state.consoleVisible, onClick = { holder.toggleConsole() })
        Spacer(Modifier.width(6.dp))
        SmallGlassButton("Inspector", active = state.inspectorVisible, onClick = { holder.toggleInspector() })
        Spacer(Modifier.width(6.dp))
        SmallGlassButton("Reset", onClick = { holder.resetCanvas() })
    }
}