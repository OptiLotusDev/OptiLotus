package app.ui

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

    private var canvasWidthPx = 1600f
    private var canvasHeightPx = 1000f
    private var nodeWidthPx = 260f
    private var nodeHeightPx = 112f
    private val nodeGapPx = 24f

    fun updateCanvasMetrics(widthPx: Float, heightPx: Float, nodeWidthPx: Float, nodeHeightPx: Float) {
        this.canvasWidthPx = widthPx
        this.canvasHeightPx = heightPx
        this.nodeWidthPx = nodeWidthPx
        this.nodeHeightPx = nodeHeightPx
    }

    private fun clamp(position: Offset): Offset = Offset(
        position.x.coerceIn(0f, (canvasWidthPx - nodeWidthPx).coerceAtLeast(0f)),
        position.y.coerceIn(0f, (canvasHeightPx - nodeHeightPx).coerceAtLeast(0f))
    )

    fun addPrintBlock(position: Offset, addNewline: Boolean, afterBlockId: BlockId? = null) {
        _state.update { current ->
            val parent = afterBlockId?.let { id -> current.blocks.firstOrNull { it.id == id } }
            val targetPosition = if (parent != null) {
                Offset(parent.position.x, parent.position.y + nodeHeightPx + nodeGapPx)
            } else {
                clamp(position)
            }
            val block = PlacedBlock(
                id = BlockId.random(),
                position = targetPosition,
                addNewline = addNewline
            )
            val connections = if (parent != null && current.connections.none { it.fromBlockId == parent.id }) {
                current.connections + Connection(fromBlockId = parent.id, toBlockId = block.id)
            } else {
                current.connections
            }
            current.copy(
                blocks = current.blocks + block,
                connections = connections,
                selectedBlockId = block.id,
                dragHoverPosition = null,
                dragHoverTargetBlockId = null
            )
        }
    }

    fun setDragHover(position: Offset?, targetBlockId: BlockId?) {
        _state.update { it.copy(dragHoverPosition = position, dragHoverTargetBlockId = targetBlockId) }
    }

    fun moveBlockBy(id: BlockId, delta: Offset) {
        _state.update { state ->
            state.copy(blocks = state.blocks.map { block ->
                if (block.id == id) block.copy(position = clamp(block.position + delta)) else block
            })
        }
    }

    fun selectBlock(id: BlockId?) {
        _state.update { it.copy(selectedBlockId = id) }
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
                connections = state.connections.filterNot { it.fromBlockId == id || it.toBlockId == id },
                selectedBlockId = if (state.selectedBlockId == id) null else state.selectedBlockId
            )
        }
    }

    fun runProgram() {
        val current = _state.value
        if (current.blocks.isEmpty()) return
        val entryPoint = current.entryPointBlockId ?: return
        val nodes: Map<BlockId, BlockAstNode> = current.blocks.associate { block ->
            block.id to PrintStatementBlock(
                id = block.id,
                valueExpression = LiteralExpressionBlock(BlockId.random(), block.literalValue),
                addNewline = block.addNewline
            )
        }
        val graph = BlockGraph(
            entryPointBlockId = entryPoint,
            nodes = nodes,
            connections = current.connections
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
                connections = emptyList(),
                selectedBlockId = null,
                output = emptyList(),
                errors = emptyList()
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
