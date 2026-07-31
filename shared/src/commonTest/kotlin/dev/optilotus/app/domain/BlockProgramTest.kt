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

    // ── Core functionality ──

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

    // ── Literal value edge cases ──

    @Test
    fun `print null literal produces string null`() {
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), null),
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
        assertEquals(listOf("null"), context.output)
    }

    @Test
    fun `print empty string produces empty output`() {
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), ""),
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
    fun `print boolean true`() {
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), true),
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
        assertEquals(listOf("true"), context.output)
    }

    @Test
    fun `print boolean false`() {
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), false),
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
        assertEquals(listOf("false"), context.output)
    }

    @Test
    fun `print double value`() {
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), 3.14159),
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
        assertEquals(listOf("3.14159"), context.output)
    }

    @Test
    fun `print integer max boundary`() {
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), Int.MAX_VALUE),
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
        assertEquals(listOf("2147483647"), context.output)
    }

    @Test
    fun `print value larger than int max uses long`() {
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), Int.MAX_VALUE.toLong() + 1),
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
        assertEquals(listOf("2147483648"), context.output)
    }

    @Test
    fun `print long max boundary`() {
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), Long.MAX_VALUE),
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
        assertEquals(listOf("9223372036854775807"), context.output)
    }

    @Test
    fun `print value beyond long max as string`() {
        val context = BlockExecutionContext()
        val hugeNumber = "99999999999999999999999999999999999"
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), hugeNumber),
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
        assertEquals(listOf(hugeNumber), context.output)
    }

    @Test
    fun `print list of integers`() {
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), listOf(1, 2, 3)),
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
        assertEquals(listOf("[1, 2, 3]"), context.output)
    }

    @Test
    fun `print unicode and emoji characters`() {
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), "🚀 Kotlin ✓ 你好"),
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
        assertEquals(listOf("🚀 Kotlin ✓ 你好"), context.output)
    }

    @Test
    fun `print multiline string preserves embedded newlines`() {
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), "line1\nline2"),
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
        assertEquals(listOf("line1\nline2\n"), context.output)
    }

    @Test
    fun `print very long string`() {
        val longString = "A".repeat(10000)
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), longString),
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
        assertEquals(1, context.output.size)
        assertEquals(10000, context.output[0].length)
        assertEquals(longString, context.output[0])
    }

    // ── Graph edge cases ──

    @Test
    fun `empty graph executes without error`() {
        val context = BlockExecutionContext()
        BlockProgramExecutor().executeProgram(
            BlockGraph(
                entryPointBlockId = BlockId.random(),
                nodes = emptyMap(),
                connections = emptyList()
            ),
            context
        )
        assertTrue(context.output.isEmpty())
        assertEquals(1, context.errors.size)
        assertTrue(context.errors[0].contains("Missing"))
    }

    @Test
    fun `entry point not in nodes reports error`() {
        val context = BlockExecutionContext()
        val missingId = BlockId.random()
        val printBlock = PrintStatementBlock(id = BlockId.random())
        BlockProgramExecutor().executeProgram(
            BlockGraph(
                entryPointBlockId = missingId,
                nodes = mapOf(printBlock.id to printBlock),
                connections = emptyList()
            ),
            context
        )
        assertTrue(context.output.isEmpty())
        assertEquals(1, context.errors.size)
        assertTrue(context.errors[0].contains(missingId.value))
    }

    @Test
    fun `broken chain connection reports error and stops`() {
        val print1Id = BlockId.random()
        val missingId = BlockId.random()
        val print2Id = BlockId.random()
        val context = BlockExecutionContext()
        val print1 = PrintStatementBlock(
            id = print1Id,
            valueExpression = LiteralExpressionBlock(BlockId.random(), "A"),
            addNewline = false
        )
        val print2 = PrintStatementBlock(
            id = print2Id,
            valueExpression = LiteralExpressionBlock(BlockId.random(), "B"),
            addNewline = false
        )
        BlockProgramExecutor().executeProgram(
            BlockGraph(
                entryPointBlockId = print1Id,
                nodes = mapOf(print1Id to print1, print2Id to print2),
                connections = listOf(
                    Connection(fromBlockId = print1Id, toBlockId = missingId)
                )
            ),
            context
        )
        // First block executed, then chain broke
        assertEquals(listOf("A"), context.output)
        assertEquals(1, context.errors.size)
        assertTrue(context.errors[0].contains(missingId.value))
    }

    @Test
    fun `executor skips expression blocks in the statement chain`() {
        val exprId = BlockId.random()
        val printId = BlockId.random()
        val context = BlockExecutionContext()
        val exprBlock = LiteralExpressionBlock(id = exprId, literalValue = "should-not-print")
        val printBlock = PrintStatementBlock(
            id = printId,
            valueExpression = LiteralExpressionBlock(BlockId.random(), "B"),
            addNewline = false
        )
        BlockProgramExecutor().executeProgram(
            BlockGraph(
                entryPointBlockId = exprId,
                nodes = mapOf(exprId to exprBlock, printId to printBlock),
                connections = listOf(
                    Connection(fromBlockId = exprId, toBlockId = printId)
                )
            ),
            context
        )
        // Expression block silently skipped, only print executed
        assertEquals(listOf("B"), context.output)
        assertTrue(context.errors.isEmpty())
    }

    @Test
    fun `single statement with no connections still executes`() {
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), "lonely"),
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
        assertEquals(listOf("lonely"), context.output)
    }

    @Test
    fun `unreachable extra nodes are ignored`() {
        val printId = BlockId.random()
        val orphanId = BlockId.random()
        val context = BlockExecutionContext()
        val printBlock = PrintStatementBlock(
            id = printId,
            valueExpression = LiteralExpressionBlock(BlockId.random(), "reachable"),
            addNewline = false
        )
        val orphanBlock = PrintStatementBlock(
            id = orphanId,
            valueExpression = LiteralExpressionBlock(BlockId.random(), "orphan"),
            addNewline = false
        )
        BlockProgramExecutor().executeProgram(
            BlockGraph(
                entryPointBlockId = printId,
                nodes = mapOf(printId to printBlock, orphanId to orphanBlock),
                connections = emptyList()
            ),
            context
        )
        assertEquals(listOf("reachable"), context.output)
    }

    @Test
    fun `duplicate connections only uses first match`() {
        val print1Id = BlockId.random()
        val print2Id = BlockId.random()
        val print3Id = BlockId.random()
        val context = BlockExecutionContext()
        val print1 = PrintStatementBlock(
            id = print1Id,
            valueExpression = LiteralExpressionBlock(BlockId.random(), "first"),
            addNewline = false
        )
        val print2 = PrintStatementBlock(
            id = print2Id,
            valueExpression = LiteralExpressionBlock(BlockId.random(), "second"),
            addNewline = false
        )
        val print3 = PrintStatementBlock(
            id = print3Id,
            valueExpression = LiteralExpressionBlock(BlockId.random(), "third"),
            addNewline = false
        )
        BlockProgramExecutor().executeProgram(
            BlockGraph(
                entryPointBlockId = print1Id,
                nodes = mapOf(print1Id to print1, print2Id to print2, print3Id to print3),
                connections = listOf(
                    Connection(fromBlockId = print1Id, toBlockId = print2Id),
                    Connection(fromBlockId = print1Id, toBlockId = print3Id)
                )
            ),
            context
        )
        // Only the first connection (print2) is followed; print3 is unreachable
        assertEquals(listOf("first", "second"), context.output)
    }

    // ── Execution context state ──

    @Test
    fun `multiple error reports accumulate`() {
        val context = BlockExecutionContext()
        context.reportError("error one")
        context.reportError("error two")
        assertEquals(2, context.errors.size)
        assertEquals(listOf("error one", "error two"), context.errors)
    }

    @Test
    fun `context output is immutable snapshot`() {
        val context = BlockExecutionContext()
        context.writeOutput("hello")
        val snapshot = context.output
        context.writeOutput("world")
        // Snapshot was taken before second write
        assertEquals(listOf("hello"), snapshot)
        assertEquals(listOf("hello", "world"), context.output)
    }

    @Test
    fun `fresh context starts empty`() {
        val context = BlockExecutionContext()
        assertTrue(context.output.isEmpty())
        assertTrue(context.errors.isEmpty())
    }

    // ── ValueExpression nesting ──

    @Test
    fun `expression block evaluates independently without executor`() {
        val expr = LiteralExpressionBlock(BlockId.random(), 99)
        assertEquals(99, expr.evaluate(BlockExecutionContext()))
    }

    @Test
    fun `nested expression in print evaluates correctly`() {
        val inner = LiteralExpressionBlock(BlockId.random(), "nested")
        val outer = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = inner,
            addNewline = false
        )
        val context = BlockExecutionContext()
        BlockProgramExecutor().executeProgram(
            BlockGraph(
                entryPointBlockId = outer.id,
                nodes = mapOf(outer.id to outer),
                connections = emptyList()
            ),
            context
        )
        assertEquals(listOf("nested"), context.output)
    }

    @Test
    fun `addNewline toggles between outputs`() {
        val context = BlockExecutionContext()
        val printNewline = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), "text"),
            addNewline = true
        )
        val printNoNewline = PrintStatementBlock(
            id = BlockId.random(),
            valueExpression = LiteralExpressionBlock(BlockId.random(), "text"),
            addNewline = false
        )
        assertEquals("text\n", printNewline.valueExpression.evaluate(context).let {
            if (printNewline.addNewline) "$it\n" else "$it"
        })
        assertEquals("text", printNoNewline.valueExpression.evaluate(context).let {
            if (printNoNewline.addNewline) "$it\n" else "$it"
        })
    }
}
