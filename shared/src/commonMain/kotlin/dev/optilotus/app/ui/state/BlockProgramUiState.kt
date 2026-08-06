package dev.optilotus.app.ui.state

import androidx.compose.ui.geometry.Offset
import dev.optilotus.app.domain.BlockId

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
    PaletteItem(
        id = "print",
        label = "print",
        description = "Print a value; toggle \\n to end with a newline",
        category = BlockCategory.INPUT_OUTPUT,
        addNewline = true
    )
)

data class BlockProgramUiState(
    val blocks: List<PlacedBlock> = emptyList(),
    val chainOffset: Offset = Offset.Zero,
    val selectedBlockId: BlockId? = null,
    val output: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
    val runCount: Int = 0,
    val draggingBlockId: BlockId? = null,
    val dragInsertIndex: Int = 0,
    val consoleVisible: Boolean = true,
    val inspectorVisible: Boolean = true
) {
    val entryPointBlockId: BlockId?
        get() = blocks.firstOrNull()?.id

    val linkCount: Int
        get() = (blocks.size - 1).coerceAtLeast(0)
}