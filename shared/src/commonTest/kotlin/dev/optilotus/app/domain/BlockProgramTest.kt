package dev.optilotus.app.domain

import dev.optilotus.app.domain.executor.BlockProgramExecutor
import dev.optilotus.app.domain.model.BlockGraph
import dev.optilotus.app.domain.model.Connection
import dev.optilotus.app.domain.model.LiteralExpressionBlock
import dev.optilotus.app.domain.model.PrintStatementBlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BlockProgramTest {

    @Test
    fun `print literal string without newline`() {
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), "Hello"),
            addNewline = false
        )
        BlockProgramExecutor().executeProgram(
            BlockGraph(
                entryPointBlockId = printBlock.id,
                nodes = mapOf(printBlock.id to printBlock),
                connections = emptyList()
            ),
            context
        )
        assertEquals(listOf("Hello"), context.output)
    }

    @Test
    fun `print literal integer with newline`() {
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), 42),
            addNewline = true
        )
        BlockProgramExecutor().executeProgram(
            BlockGraph(
                entryPointBlockId = printBlock.id,
                nodes = mapOf(printBlock.id to printBlock),
                connections = emptyList()
            ),
            context
        )
        assertEquals(listOf("42\n"), context.output)
    }

    @Test
    fun `print with default expression defaults to empty string`() {
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            addNewline = false
        )
        BlockProgramExecutor().executeProgram(
            BlockGraph(
                entryPointBlockId = printBlock.id,
                nodes = mapOf(printBlock.id to printBlock),
                connections = emptyList()
            ),
            context
        )
        assertEquals(listOf(""), context.output)
    }

    @Test
    fun `executor runs chained print blocks in sequence`() {
        val print1Id = BlockId.random()
        val print2Id = BlockId.random()
        val print1 = PrintStatementBlock(
            id = print1Id,
            valueExpression = LiteralExpressionBlock(BlockId.random(), "Hello "),
            addNewline = false
        )
        val print2 = PrintStatementBlock(
            id = print2Id,
            valueExpression = LiteralExpressionBlock(BlockId.random(), "OptiLotus!"),
            addNewline = true
        )
        val context = BlockExecutionContext()
        BlockProgramExecutor().executeProgram(
            BlockGraph(
                entryPointBlockId = print1Id,
                nodes = mapOf(print1Id to print1, print2Id to print2),
                connections = listOf(Connection(fromBlockId = print1Id, toBlockId = print2Id))
            ),
            context
        )
        assertEquals(listOf("Hello ", "OptiLotus!\n"), context.output)
        assertTrue(context.errors.isEmpty())
    }
}
