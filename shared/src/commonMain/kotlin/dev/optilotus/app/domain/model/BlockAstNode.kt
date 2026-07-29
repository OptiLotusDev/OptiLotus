package dev.optilotus.app.domain.model

import dev.optilotus.app.domain.BlockExecutionContext
import dev.optilotus.app.domain.BlockId

sealed interface BlockAstNode {
    val id: BlockId
}

sealed interface ExpressionBlockNode : BlockAstNode {
    fun evaluate(context: BlockExecutionContext): Any?
}

sealed interface StatementBlockNode : BlockAstNode {
    fun execute(context: BlockExecutionContext)
}
