# Block System — Roadmap

## Legend

| Status      | Meaning                             |
|-------------|-------------------------------------|
| Done        | Implemented and tested              |
| Next        | High priority, next to build        |
| Later       | Planned but not imminent            |
| Exploration | Design needed before implementation |

---

## Phase 1 — Foundation (Complete)

| Component                                                     | Status | Note                               |
|---------------------------------------------------------------|--------|------------------------------------|
| `BlockId`                                                     | Done   | UUID value class                   |
| `BlockExecutionContext`                                       | Done   | Output + error buffers             |
| `BlockAstNode` / `ExpressionBlockNode` / `StatementBlockNode` | Done   | Sealed interface hierarchy         |
| `LiteralExpressionBlock`                                      | Done   | Generic leaf for `Any?` values     |
| `PrintStatementBlock`                                         | Done   | Statement with `addNewline` toggle |
| `BlockGraph` / `Connection`                                   | Done   | Topology separated from nodes      |
| `BlockProgramExecutor`                                        | Done   | Walks graph, executes statements   |
| Default `valueExpression`                                     | Done   | Inline typing, no second drag      |

---

## Phase 2 — Variables & Execution Context (Next)

### Execution Context Expansion

Add variable storage to `BlockExecutionContext`:

```kotlin
class BlockExecutionContext {
    // Existing
    fun writeOutput(text: String)
    fun reportError(message: String)
    val output: List<String>
    val errors: List<String>

    // New
    fun setVariable(name: String, value: Any?)
    fun getVariable(name: String): Any?
}
```

### New Blocks

| Block               | Type                  | Purpose                                    |
|---------------------|-----------------------|--------------------------------------------|
| `SetVariableBlock`  | `StatementBlockNode`  | `setVariable(name, expression.evaluate())` |
| `VariableReadBlock` | `ExpressionBlockNode` | `getVariable(name)`                        |

---

## Phase 3 — Math & Logic (Next)

| Block                  | Type                  | Purpose                                                |
|------------------------|-----------------------|--------------------------------------------------------|
| `BinaryOperationBlock` | `ExpressionBlockNode` | `+`, `-`, `*`, `/` on two `ExpressionBlockNode` inputs |

Accepts `left: ExpressionBlockNode`, `operator: String`, `right: ExpressionBlockNode`. Evaluates
both sides, applies operator.

---

## Phase 4 — Control Flow (Later)

Uses `Connection.slotName` for labeled branching instead of default `null`.

| Block            | Type                 | Slot Names                      | Purpose                               |
|------------------|----------------------|---------------------------------|---------------------------------------|
| `IfElseBlock`    | `StatementBlockNode` | `"trueBranch"`, `"falseBranch"` | Evaluates boolean condition, branches |
| `WhileLoopBlock` | `StatementBlockNode` | `"loopBody"`, `"completed"`     | Loops while condition is true         |

The executor resolves labeled connections:

```kotlin
val branchSlot = if (conditionValue == true) "trueBranch" else "falseBranch"
currentBlockId = program.nextBlockAfter(currentId, viaSlotNamed = branchSlot)
```

---

## Phase 5 — UI Layer (Later)

All UI code lives in a separate module, not in `domain/`.

| Feature                             | Description                                                                |
|-------------------------------------|----------------------------------------------------------------------------|
| `BlockCategory` enum (in UI module) | `INPUT_OUTPUT`, `TEXT`, `MATH`, `LOGIC`, `CONTROL_FLOW`, `VARIABLES`       |
| `BlockAstNode.uiCategory` extension | Maps each block type to its palette category                               |
| Inline slot editor                  | When `valueExpression` is a `LiteralExpressionBlock`, render editable text |
| Block palette                       | Drag blocks from categorized palette to canvas                             |
| Connection rendering                | Draw lines between connected blocks                                        |
| Console panel                       | Render `context.output`, highlight `context.errors`                        |

---

## Phase 6 — Sub-Graphs & Functions (Exploration)

| Feature                                | Description                                            |
|----------------------------------------|--------------------------------------------------------|
| `SubGraphBlock`                        | Invokes a separate `BlockGraph` as a reusable function |
| Isolated child `BlockExecutionContext` | Sandboxed scope with explicit parameter passing        |
| `ReturnBlock`                          | Terminates sub-graph and returns value to parent       |
| Call stack with depth limits           | Prevent infinite recursion                             |

---

## Phase 7 — Debugging & Tooling (Exploration)

| Feature                   | Description                                    |
|---------------------------|------------------------------------------------|
| Step-through execution    | Pause after each statement                     |
| State snapshots           | Capture `context` at each step for time-travel |
| Error recovery strategies | `RETRY`, `SKIP`, `HALT` per block              |
