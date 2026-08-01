package dev.optilotus.app.domain.executor

import dev.optilotus.app.domain.BlockExecutionContext
import dev.optilotus.app.domain.BlockId
import dev.optilotus.app.domain.model.BlockGraph
import dev.optilotus.app.domain.model.PrintStatementBlock

class BlockProgramMain(
    private val program: BlockGraph,
    private val context: BlockExecutionContext
) {

    fun print(value: Any?) {
        context.writeOutput("$value")
    }

    fun println(value: Any?) {
        context.writeOutput("$value\n")
    }

    fun main() {
        var currentBlockId: BlockId? = program.entryPointBlockId

        while (currentBlockId != null) {
            val node = program.nodes[currentBlockId]
                ?: run {
                    context.reportError("Missing block node for ID $currentBlockId")
                    return
                }

            if (node is PrintStatementBlock) {
                val value = node.valueExpression.evaluate(context)
                if (node.addNewline) println(value) else print(value)
            }

            currentBlockId = program.nextBlockAfter(currentBlockId, viaSlotNamed = null)
        }
    }
}
