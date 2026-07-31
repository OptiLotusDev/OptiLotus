package dev.optilotus.app.domain.model

import dev.optilotus.app.domain.BlockExecutionContext
import dev.optilotus.app.domain.BlockId

data class PrintStatementBlock(
    override val id: BlockId,
    val valueExpression: ExpressionBlockNode = LiteralExpressionBlock(BlockId.random(), ""),
    val addNewline: Boolean = true
) : StatementBlockNode {
    override fun execute(context: BlockExecutionContext) {
        val value = valueExpression.evaluate(context)
        context.writeOutput(if (addNewline) "$value\n" else "$value")
    }
}
