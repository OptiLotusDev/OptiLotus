package dev.optilotus.app.domain.executor

import dev.optilotus.app.domain.BlockExecutionContext
import dev.optilotus.app.domain.BlockId
import dev.optilotus.app.domain.model.BlockGraph
import dev.optilotus.app.domain.model.StatementBlockNode

class BlockProgramExecutor {

    fun executeProgram(program: BlockGraph, context: BlockExecutionContext) {
        var currentBlockId: BlockId? = program.entryPointBlockId

        while (currentBlockId != null) {
            val currentNode = program.nodes[currentBlockId]
                ?: run {
                    context.reportError("Missing block node for ID $currentBlockId")
                    return
                }

            if (currentNode is StatementBlockNode) {
                currentNode.execute(context)
            }

            currentBlockId = program.nextBlockAfter(currentBlockId, viaSlotNamed = null)
        }
    }
}
