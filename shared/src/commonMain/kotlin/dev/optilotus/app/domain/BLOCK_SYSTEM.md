# Block System — Architecture & API Guide

## Overview

The block system is a backend AST engine for a visual programming language. It lives in `dev.optilotus.app.domain` — pure Kotlin, zero UI dependencies, targeting all KMP platforms.

```
Every block is either:
  ┌─ ExpressionBlockNode  — computes and RETURNS a value
  └─ StatementBlockNode   — performs an ACTION, returns nothing
```

A program is a graph of blocks (`BlockGraph`) connected by edges (`Connection`). An executor (`BlockProgramExecutor`) walks the graph from the entry point, executing statements in order.

---

## Core Types

### `BlockId` (`BlockId.kt`)

A type-safe UUID wrapper. Every block has one.

```kotlin
val id = BlockId.random()  // generates UUID v4
```

---

### `BlockExecutionContext` (`BlockExecutionContext.kt`)

The runtime state object that flows through every `execute()` and `evaluate()` call.

```kotlin
val context = BlockExecutionContext()

context.writeOutput("Hello")
context.reportError("Oops")

context.output   // List<String> — immutable snapshot
context.errors   // List<String> — immutable snapshot
```

---

### `BlockAstNode` / `ExpressionBlockNode` / `StatementBlockNode` (`BlockAstNode.kt`)

The three sealed interfaces form the type hierarchy.

```kotlin
sealed interface BlockAstNode { val id: BlockId }

sealed interface ExpressionBlockNode : BlockAstNode {
    fun evaluate(context: BlockExecutionContext): Any?
}

sealed interface StatementBlockNode : BlockAstNode {
    fun execute(context: BlockExecutionContext)
}
```

**Type system rules:**

| If you have a... | You can... | You cannot... |
|---|---|---|
| `ExpressionBlockNode` | Plug it into a block's value slot | Execute it as a statement |
| `StatementBlockNode` | Execute it in the program chain | Plug it into a value slot |

---

### `LiteralExpressionBlock` (`LiteralExpressionBlock.kt`)

A generic expression block that wraps any constant value.

```kotlin
LiteralExpressionBlock(id = BlockId.random(), literalValue = "Hello")
LiteralExpressionBlock(id = BlockId.random(), literalValue = 42)
LiteralExpressionBlock(id = BlockId.random(), literalValue = true)
LiteralExpressionBlock(id = BlockId.random(), literalValue = null)
```

`evaluate()` returns `literalValue` as-is. This is used both when a user types a value directly into a slot, and when they drag a "value" block from the palette.

---

### `PrintStatementBlock` (`PrintStatementBlock.kt`)

Accepts any `ExpressionBlockNode` as its value source and writes to the execution context's output.

```kotlin
PrintStatementBlock(
    id = BlockId.random(),
    valueExpression = LiteralExpressionBlock(BlockId.random(), "Hello"),
    addNewline = true
)
```

If `valueExpression` is not specified, it defaults to an empty `LiteralExpressionBlock`:

```kotlin
PrintStatementBlock(id = BlockId.random())  // prints ""
```

---

### `Connection` and `BlockGraph` (`BlockGraph.kt`)

Graph topology is separated from node behavior.

```kotlin
data class Connection(
    val fromBlockId: BlockId,
    val toBlockId: BlockId,
    val slotName: String? = null  // null = default linear connection
)

data class BlockGraph(
    val entryPointBlockId: BlockId,
    val nodes: Map<BlockId, BlockAstNode>,
    val connections: List<Connection>
)
```

Only `StatementBlockNode`s go into the nodes map. `ExpressionBlockNode`s nested inside statements (as `valueExpression` references) are not in the graph.

```kotlin
val graph = BlockGraph(
    entryPointBlockId = print1.id,
    nodes = mapOf(print1.id to print1, print2.id to print2),
    connections = listOf(
        Connection(fromBlockId = print1.id, toBlockId = print2.id)
    )
)
```

---

### `BlockProgramExecutor` (`BlockProgramExecutor.kt`)

Walks the graph and executes statements in sequence.

```kotlin
val executor = BlockProgramExecutor()
val context = BlockExecutionContext()

executor.executeProgram(graph, context)

assertEquals(listOf("Hello ", "OptiLotus!\n"), context.output)
assertEquals(emptyList(), context.errors)
```

**Execution algorithm:**

```
1. Start at graph.entryPointBlockId
2. Look up the node in graph.nodes
3. If it's a StatementBlockNode, call execute(context)
4. Find the next connection via graph.nextBlockAfter(currentId, viaSlotNamed = null)
5. If found, go to step 2. If not found, stop.
```

---

## Testing Patterns

### Single statement

```kotlin
@Test
fun `print hello`() {
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
```

### Expression evaluation directly

```kotlin
@Test
fun `literal evaluates to its value`() {
    val block = LiteralExpressionBlock(BlockId.random(), 42)
    assertEquals(42, block.evaluate(BlockExecutionContext()))
}
```

### Chained statements

```kotlin
@Test
fun `chained prints execute in order`() {
    val print1 = PrintStatementBlock(
        id = BlockId.random(),
        valueExpression = LiteralExpressionBlock(BlockId.random(), "A"),
        addNewline = false
    )
    val print2 = PrintStatementBlock(
        id = BlockId.random(),
        valueExpression = LiteralExpressionBlock(BlockId.random(), "B"),
        addNewline = true
    )
    val graph = BlockGraph(
        entryPointBlockId = print1.id,
        nodes = mapOf(print1.id to print1, print2.id to print2),
        connections = listOf(Connection(fromBlockId = print1.id, toBlockId = print2.id))
    )
    val context = BlockExecutionContext()
    BlockProgramExecutor().executeProgram(graph, context)

    assertEquals(listOf("A", "B\n"), context.output)
}
```

### Default value expression

```kotlin
@Test
fun `print with no value expression defaults to empty string`() {
    val context = BlockExecutionContext()
    val printBlock = PrintStatementBlock(id = BlockId.random())
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
```

---

## Execution Flow

```
UI constructs BlockGraph {
    entryPointBlockId,
    nodes: Map<BlockId, BlockAstNode>,
    connections: List<Connection>
}
        │
        ▼
UI calls BlockProgramExecutor.executeProgram(graph, context)
        │
        ▼
Executor loop:
  currentId = graph.entryPointBlockId
  while (currentId != null) {
      node = graph.nodes[currentId]
      if (node is StatementBlockNode) node.execute(context)
      currentId = graph.nextBlockAfter(currentId)
  }
        │
        ▼
UI reads context.output, context.errors
```

---

## File Reference

```
domain/
├── BlockId.kt                    value class, UUID factory
├── BlockExecutionContext.kt      mutable state holder (output, errors)
├── model/
│   ├── BlockAstNode.kt           3 sealed interfaces
│   ├── BlockGraph.kt             Connection + BlockGraph topology
│   ├── LiteralExpressionBlock.kt generic expression leaf (Any?)
│   └── PrintStatementBlock.kt    statement leaf (print)
└── executor/
    └── BlockProgramExecutor.kt   graph walker (execute loop)
```
