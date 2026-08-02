package dev.optilotus.app.ui.state

import androidx.compose.ui.geometry.Offset
import dev.optilotus.app.domain.BlockId
import dev.optilotus.app.domain.model.Connection

enum class BlockCategory(val label: String) {
    INPUT_OUTPUT("Input / Output"),
    TEXT("Text"),
    MATH("Math"),
    LOGIC("Logic"),
    CONTROL_FLOW("Control Flow"),
    VARIABLES("Variables")
}

enum class PlacedBlockKind {
    PRINT
}

data class PlacedBlock(
    val id: BlockId,
    val position: Offset,
    val kind: PlacedBlockKind = PlacedBlockKind.PRINT,
    val category: BlockCategory = BlockCategory.INPUT_OUTPUT,
    val addNewline: Boolean = true,
    val literalValue: String = ""
)

data class PaletteItem(
    val id: String,
    val label: String,
    val description: String,
    val category: BlockCategory,
    val addNewline: Boolean
)

val BlockPaletteItems = listOf(
    PaletteItem("print", "print", "Print value without newline", BlockCategory.INPUT_OUTPUT, addNewline = false),
    PaletteItem("println", "println", "Print value with newline", BlockCategory.INPUT_OUTPUT, addNewline = true)
)

data class BlockProgramUiState(
    val blocks: List<PlacedBlock> = emptyList(),
    val connections: List<Connection> = emptyList(),
    val selectedBlockId: BlockId? = null,
    val output: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
    val runCount: Int = 0,
    val dragHoverPosition: Offset? = null,
    val dragHoverTargetBlockId: BlockId? = null,
    val consoleVisible: Boolean = true,
    val inspectorVisible: Boolean = true
) {
    val entryPointBlockId: BlockId?
        get() {
            if (blocks.isEmpty()) return null
            val roots = blocks.filter { block -> connections.none { it.toBlockId == block.id } }
            return (roots.firstOrNull() ?: blocks.first()).id
        }
}