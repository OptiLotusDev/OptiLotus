package dev.optilotus.app.domain.executor

import dev.optilotus.app.domain.BlockExecutionContext
import dev.optilotus.app.domain.model.BlockGraph

class BlockProgramMain(
    private val program: BlockGraph,
    private val context: BlockExecutionContext
) {
    fun main() {
        BlockProgramExecutor().executeProgram(program, context)
    }
}