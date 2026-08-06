package dev.optilotus.app.ui.state

import androidx.compose.ui.geometry.Offset
import dev.optilotus.app.domain.BlockExecutionContext
import dev.optilotus.app.domain.BlockId
import dev.optilotus.app.domain.executor.BlockProgramMain
import dev.optilotus.app.domain.model.BlockAstNode
import dev.optilotus.app.domain.model.BlockGraph
import dev.optilotus.app.domain.model.Connection
import dev.optilotus.app.domain.model.LiteralExpressionBlock
import dev.optilotus.app.domain.model.PrintStatementBlock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BlockProgramStateHolder {

    private val _state = MutableStateFlow(BlockProgramUiState())
    val state: StateFlow<BlockProgramUiState> = _state.asStateFlow()

    fun addPrintBlock(addNewline: Boolean, insertIndex: Int? = null) {
        _state.update { current ->
            val index = insertIndex?.coerceIn(0, current.blocks.size) ?: current.blocks.size
            val block = PlacedBlock(
                id = BlockId.random(),
                addNewline = addNewline
            )
            current.copy(
                blocks = current.blocks.toMutableList().apply { add(index, block) },
                selectedBlockId = block.id,
                draggingBlockId = null,
                dragInsertIndex = 0
            )
        }
    }

    /** Returns true when the block was actually moved, false when it stayed in place. */
    fun moveBlockTo(id: BlockId, insertIndex: Int? = null): Boolean {
        var moved = false
        _state.update { state ->
            val from = state.blocks.indexOfFirst { it.id == id }
            if (from < 0) return@update state.copy(draggingBlockId = null, dragInsertIndex = 0)
            val gap = (insertIndex ?: state.dragInsertIndex).coerceIn(0, state.blocks.size)
            val effective = if (gap > from) gap - 1 else gap
            if (effective == from) return@update state.copy(draggingBlockId = null, dragInsertIndex = 0)
            moved = true
            val reordered = state.blocks.toMutableList().apply {
                val block = removeAt(from)
                add(effective, block)
            }
            state.copy(
                blocks = reordered,
                draggingBlockId = null,
                dragInsertIndex = 0
            )
        }
        return moved
    }

    fun setDragInsert(draggingId: BlockId?, insertIndex: Int) {
        _state.update { it.copy(draggingBlockId = draggingId, dragInsertIndex = insertIndex) }
    }

    fun panChain(delta: Offset) {
        _state.update { it.copy(chainOffset = it.chainOffset + delta) }
    }

    fun selectBlock(id: BlockId?) {
        _state.update {
            it.copy(selectedBlockId = id, inspectorVisible = if (id != null) true else it.inspectorVisible)
        }
    }

    fun updateValue(id: BlockId, value: String) {
        _state.update { state ->
            state.copy(blocks = state.blocks.map { block ->
                if (block.id == id) block.copy(literalValue = value) else block
            })
        }
    }

    fun toggleNewline(id: BlockId) {
        _state.update { state ->
            state.copy(blocks = state.blocks.map { block ->
                if (block.id == id) block.copy(addNewline = !block.addNewline) else block
            })
        }
    }

    fun deleteBlock(id: BlockId) {
        _state.update { state ->
            state.copy(
                blocks = state.blocks.filterNot { it.id == id },
                selectedBlockId = if (state.selectedBlockId == id) null else state.selectedBlockId,
                draggingBlockId = if (state.draggingBlockId == id) null else state.draggingBlockId,
                dragInsertIndex = 0
            )
        }
    }

    fun runProgram() {
        val current = _state.value
        if (current.blocks.isEmpty()) return
        val nodes: Map<BlockId, BlockAstNode> = current.blocks.associate { block ->
            block.id to PrintStatementBlock(
                id = block.id,
                valueExpression = LiteralExpressionBlock(BlockId.random(), block.literalValue),
                addNewline = block.addNewline
            )
        }
        val connections = current.blocks.zipWithNext { a, b -> Connection(fromBlockId = a.id, toBlockId = b.id) }
        val graph = BlockGraph(
            entryPointBlockId = current.blocks.first().id,
            nodes = nodes,
            connections = connections
        )
        val context = BlockExecutionContext()
        BlockProgramMain(graph, context).main()
        _state.update { it.copy(output = context.output, errors = context.errors, runCount = it.runCount + 1) }
    }

    fun clearConsole() {
        _state.update { it.copy(output = emptyList(), errors = emptyList()) }
    }

    fun resetCanvas() {
        _state.update {
            it.copy(
                blocks = emptyList(),
                chainOffset = Offset.Zero,
                selectedBlockId = null,
                output = emptyList(),
                errors = emptyList(),
                draggingBlockId = null,
                dragInsertIndex = 0
            )
        }
    }

    fun toggleConsole() {
        _state.update { it.copy(consoleVisible = !it.consoleVisible) }
    }

    fun toggleInspector() {
        _state.update { it.copy(inspectorVisible = !it.inspectorVisible) }
    }
}