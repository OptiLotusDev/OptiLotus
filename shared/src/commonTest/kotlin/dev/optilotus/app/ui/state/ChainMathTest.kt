package dev.optilotus.app.ui.state

import kotlin.test.Test
import kotlin.test.assertEquals

class ChainMathTest {

    // ── gappedSlotIndex: phantom palette insertion ──

    @Test
    fun `phantom insertion shifts blocks after the gap down`() {
        // size 3, gap 0 (insert at very top)
        assertEquals(1, gappedSlotIndex(0, from = null, gap = 0, size = 3))
        assertEquals(2, gappedSlotIndex(1, from = null, gap = 0, size = 3))
        assertEquals(3, gappedSlotIndex(2, from = null, gap = 0, size = 3))
    }

    @Test
    fun `phantom insertion appends at the end when gap equals size`() {
        assertEquals(0, gappedSlotIndex(0, from = null, gap = 3, size = 3))
        assertEquals(1, gappedSlotIndex(1, from = null, gap = 3, size = 3))
        assertEquals(2, gappedSlotIndex(2, from = null, gap = 3, size = 3))
    }

    @Test
    fun `phantom middle insertion opens a hole where the block lands`() {
        // size 3, gap 1 (between first and second)
        assertEquals(0, gappedSlotIndex(0, from = null, gap = 1, size = 3))
        assertEquals(2, gappedSlotIndex(1, from = null, gap = 1, size = 3))
        assertEquals(3, gappedSlotIndex(2, from = null, gap = 1, size = 3))
    }

    // ── gappedSlotIndex: dragging an existing block ──

    @Test
    fun `drag to end keeps earlier blocks and opens bottom`() {
        // [A, B, C], dragging B (from=1) to the end (gap=3)
        assertEquals(0, gappedSlotIndex(0, from = 1, gap = 3, size = 3)) // A stays top
        assertEquals(1, gappedSlotIndex(2, from = 1, gap = 3, size = 3)) // C slides down one
    }

    @Test
    fun `drag to the top pushes the other blocks down`() {
        // [A, B, C], dragging B (from=1) to the top (gap=0)
        assertEquals(1, gappedSlotIndex(0, from = 1, gap = 0, size = 3)) // A drops below the gap
        assertEquals(2, gappedSlotIndex(2, from = 1, gap = 0, size = 3)) // C drops below
    }

    @Test
    fun `drag mid-chain parts the neighbours to make space`() {
        // [A, B, C], dragging B (from=1) into its own slot keeps the gap open there
        assertEquals(0, gappedSlotIndex(0, from = 1, gap = 1, size = 3)) // A keeps the top slot
        assertEquals(2, gappedSlotIndex(2, from = 1, gap = 1, size = 3)) // C slides down to open the gap
    }

    @Test
    fun `drag just below its slot compresses the tail`() {
        // [A, B, C], dragging B (from=1) toward the gap under itself (gap=2)
        assertEquals(0, gappedSlotIndex(0, from = 1, gap = 2, size = 3)) // A keeps the top slot
        assertEquals(1, gappedSlotIndex(2, from = 1, gap = 2, size = 3)) // C compresses up, gap opens below
    }

    // ── pointer-to-gap mapping ──

    @Test
    fun `pointer near the top resolves to the first gap`() {
        assertEquals(0, gapIndexForPointer(pointerY = 10f, chainTop = 0f, stepPx = 100f, size = 10))
        assertEquals(0, gapIndexForPointer(pointerY = 40f, chainTop = 0f, stepPx = 100f, size = 10))
    }

    @Test
    fun `pointer clamped to the ends of the chain`() {
        assertEquals(0, gapIndexForPointer(pointerY = -500f, chainTop = 0f, stepPx = 100f, size = 10))
        assertEquals(10, gapIndexForPointer(pointerY = 5000f, chainTop = 0f, stepPx = 100f, size = 10))
    }

    @Test
    fun `pointer midway rounds to the nearest slot boundary`() {
        assertEquals(1, gapIndexForPointer(pointerY = 60f, chainTop = 0f, stepPx = 100f, size = 10))
        assertEquals(2, gapIndexForPointer(pointerY = 150f, chainTop = 0f, stepPx = 100f, size = 10))
    }

    // ── accumulated-delta mapping for reorder ──

    @Test
    fun `drag down from a block advances the gap`() {
        assertEquals(4, reorderGapIndex(fromIndex = 3, accumulatedDeltaY = 90f, stepPx = 100f, size = 10))
        assertEquals(2, reorderGapIndex(fromIndex = 3, accumulatedDeltaY = -110f, stepPx = 100f, size = 10))
    }

    @Test
    fun `reorder gap is clamped to the list bounds`() {
        assertEquals(0, reorderGapIndex(fromIndex = 2, accumulatedDeltaY = -10000f, stepPx = 100f, size = 4))
        assertEquals(4, reorderGapIndex(fromIndex = 2, accumulatedDeltaY = 10000f, stepPx = 100f, size = 4))
    }
}