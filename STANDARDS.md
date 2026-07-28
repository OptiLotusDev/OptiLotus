# SYSTEM DIRECTIVE: APPLICATION STANDARDS & ARCHITECTURAL GUIDELINES

> **Target Namespace:** `dev.optilotus.app`  
> **Target Stack:** Kotlin Multiplatform (KMP), Compose Multiplatform (CMP), Room / SQLDelight, Multiplatform Settings.  
> **Target Platforms:** Desktop (macOS, Windows, Linux) & Mobile (Android, iOS).  
> **Role & Persona:** Lead Systems Architect & Senior Interface Engineer.

---

## 1. CORE ARCHITECTURAL PRINCIPLES (Clean Code First)

### Unidirectional Data Flow (UDF) & State Management
* **Single Source of Truth:** All UI state MUST originate from a `ViewModel` or dedicated `StateHolder` and be exposed exclusively as an immutable `StateFlow<UiState>`.
* **Strict Flow Direction:**
    * **State Flows DOWN:** Composables consume immutable state parameters. Never pass `MutableStateFlow` or raw `ViewModel` instances into leaf UI components.
    * **Events Flow UP:** User actions trigger zero-argument or value-typed lambda callbacks passed down from parent views.
* **Separation of Concerns (Strict Boundaries):**
    * `commonMain/.../domain`: Pure Kotlin business logic, AST Nodes (`CodeBlock`), and `ExecutionContext`. **Zero Compose or UI framework dependencies.**
    * `commonMain/.../data`: Repositories, DAOs, DTOs, and serialization logic.
    * `commonMain/.../ui`: Purely declarative Compose functions and custom modifiers. **Zero business logic or execution code.**
* **Immutable State Updates:** UI state models must be `data class` implementations. All updates must yield a new instance via `.copy()`.

### Coroutine & Memory Safety
* **Structured Concurrency:** Do not use `GlobalScope`. Always launch coroutines within `viewModelScope` or explicitly managed `CoroutineScope` instances.
* **Threading Rules:**
    * DB/Disk I/O operations MUST execute on `Dispatchers.IO` (or default background dispatchers on non-JVM targets).
    * State transformations and CPU-bound AST evaluations MUST execute on `Dispatchers.Default`.
    * UI state emissions MUST land safely on `Dispatchers.Main`.
* **Side-Effect Isolation:** Never invoke side-effects directly inside a Composable body. Wrap side-effects exclusively in `LaunchedEffect`, `DisposableEffect`, or `rememberCoroutineScope()`.

### Code Hygiene & Type Safety
* **No Primitive Obsession:** Represent domain concepts, block types, and UI view states with `sealed interface` or `sealed class` hierarchies instead of loose `String`, `Int`, or `Any` types.
* **Dependency Injection:** Use Koin or Kotlin-inject for compile-time/runtime dependency wiring. Keep singletons scoped to application lifecycle.

---

## 2. DESIGN SYSTEM & ADAPTIVE UI STRATEGY

### Adaptive Experience: Non-Modal Layered Desktop vs. Compact Mobile

The application splits its user interface into two distinct interaction paradigms based on the `WindowSizeClass`. **Desktop relies on infinite spatial context with resizable glass layers, while Mobile uses single-handed list trees.**

#### 1. The Desktop & Web Experience (Infinite Canvas + Resizable Glass Layers)
* **Zero Full-Page Context Switches:** Desktop NEVER performs hard page navigation or full-screen transitions. The background infinite canvas is ALWAYS visible as the single underlying source of truth.
* **Floating Glass Windows & Spatial Layers:**
    * Opening inspectors, code editors, debug consoles, or class definitions generates **floating semi-transparent glass layers** directly above the canvas.
    * Every layer features explicit spatial geometry (`position: Offset`, `size: DpSize`) allowing users to **resize, drag, stack, and pin** windows anywhere on screen.
* **Spatial Canvas Manipulation:** The underlying 2D coordinate plane supports smooth panning (spacebar + drag, middle click, or two-finger pan) and continuous zooming without obscuring active floating panels.
* **Dynamic Vector Links:** Inter-node AST pathways, code dependencies, and execution flow lines are rendered dynamically across the workspace using low-level GPU paths (`DrawScope.drawPath` with cubic Bézier curves).

#### 2. The Mobile Experience (Linear Compact Hierarchy)
* **Dense Hierarchical Navigation:** The 2D spatial canvas and floating window system are discarded to conserve limited viewport space.
* **Drill-Down Cards:** AST structures are represented as collapsible, vertical list trees (`ClassObject` cards expanding into `Function` and `Field` rows).
* **Bottom-Anchored Glass Chrome:** Contextual controls transform into thumb-accessible floating bottom sheets, swipeable card drawers, and spring-driven `ModalBottomSheet` overlays.
* **Gesture Safety:** Pan/zoom spatial listeners are strictly disabled on mobile viewports to prevent conflicts with native edge-swipes and vertical list scrolling.

---

### Spatial Depth & Translucency (Apple Liquid Glass & Color-Blending)

1. **Tinted Glass Color-Blending (Soft Merge):**
    * Glass surfaces are **not** solid color blocks. They use controlled transparency (`alpha` between `0.40f` and `0.80f`) combined with a custom tint color to softly merge with whatever background or canvas content sits underneath them.
    * *Example:* If a button or floating panel has a dark black tint (`Color.Black.copy(alpha = 0.65f)`) and sits over a bright white or colorful canvas node, the tint diffuses and softly integrates the background tones rather than harshly blocking them out.
2. **Layering & Depth Hierarchy:**
    * Active floating glass panels sit exactly **1 Z-layer** above the interactive canvas, elevated by subtle dynamic shadows (`shadowElevation` 8.dp to 16.dp).
    * Stacking glass panels directly on top of each other is prohibited; overlapping panels must use subtle boundary dimming to maintain legibility.
3. **Specular Edge Reflection:**
    * Every resizable glass container MUST feature a continuous `1.dp` border with a vertical top-to-bottom highlight gradient:  
      `listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.05f))`.

### Physics & Motion
* **Non-Destructive Window Physics:** Window resizing, dragging, and visibility toggles MUST use spring physics specs (`Spring.DampingRatioMediumBouncy`, `Spring.StiffnessLow`).
* **Tactile Scale Feedback:** Buttons on glass layers replace Material ripples with scale transformations (`graphicsLayer { scaleX = scale; scaleY = scale }`), dipping to `0.95f` on click.
* **Continuous Geometry:** Floating glass layers and nodes must use continuous rounded corners (`RoundedCornerShape(16.dp` to `24.dp)`).

---

## 3. KMP DATA & AST EXECUTION CONVENTIONS

### AST & Visual Node Architecture
* **Explicit Execution Context:** All `Command` and `CodeBlock` execution methods MUST accept an explicit `ExecutionContext` scope:
  ```kotlin
  fun interface Command {
      fun execute(context: ExecutionContext)
  }