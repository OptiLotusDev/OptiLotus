package dev.optilotus.app.domain.model

import dev.optilotus.app.domain.BlockExecutionContext
import dev.optilotus.app.domain.BlockId

data class LiteralExpressionBlock(
    override val id: BlockId,
    val literalValue: Any?
) : ExpressionBlockNode {
    override fun evaluate(context: BlockExecutionContext): Any? = literalValue
}
