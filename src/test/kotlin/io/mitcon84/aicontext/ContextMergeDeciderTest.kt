package io.mitcon84.aicontext

import io.mitcon84.aicontext.context.ContextItem
import io.mitcon84.aicontext.context.ContextMergeDecider
import io.mitcon84.aicontext.context.MergeDecision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextMergeDeciderTest {
    private val decider = ContextMergeDecider()

    @Test
    fun `exact duplicate is skipped`() {
        val existing = item("a", "src/App.kt", 1, 10)
        val decision = decider.decide(listOf(existing), item("new", "src/App.kt", 1, 10))

        assertTrue(decision is MergeDecision.SkipAlreadyCovered)
    }

    @Test
    fun `nested range is skipped`() {
        val existing = item("a", "src/App.kt", 1, 44)
        val decision = decider.decide(listOf(existing), item("new", "src/App.kt", 1, 5))

        assertTrue(decision is MergeDecision.SkipAlreadyCovered)
    }

    @Test
    fun `partial overlap merges into union`() {
        val existing = item("a", "src/App.kt", 36, 41)
        val decision = decider.decide(listOf(existing), item("new", "src/App.kt", 39, 47))

        assertTrue(decision is MergeDecision.Merge)
        decision as MergeDecision.Merge
        assertEquals(36, decision.startLine)
        assertEquals(47, decision.endLine)
    }

    @Test
    fun `adjacent ranges merge`() {
        val existing = item("a", "src/App.kt", 10, 20)
        val decision = decider.decide(listOf(existing), item("new", "src/App.kt", 21, 30))

        assertTrue(decision is MergeDecision.Merge)
        decision as MergeDecision.Merge
        assertEquals(10, decision.startLine)
        assertEquals(30, decision.endLine)
    }

    @Test
    fun `non overlapping ranges create separate items`() {
        val existing = item("a", "src/App.kt", 10, 20)
        val decision = decider.decide(listOf(existing), item("new", "src/App.kt", 25, 30))

        assertTrue(decision is MergeDecision.AddNew)
    }

    @Test
    fun `overlapping multiple items produces one merged item`() {
        val first = item("a", "src/App.kt", 10, 20)
        val second = item("b", "src/App.kt", 25, 35)
        val decision = decider.decide(listOf(first, second), item("new", "src/App.kt", 18, 28))

        assertTrue(decision is MergeDecision.Merge)
        decision as MergeDecision.Merge
        assertEquals(10, decision.startLine)
        assertEquals(35, decision.endLine)
        assertEquals(2, decision.itemsToMerge.size)
    }

    @Test
    fun `merge preserves file separation`() {
        val existing = item("a", "src/App.kt", 10, 20)
        val decision = decider.decide(listOf(existing), item("new", "src/Other.kt", 15, 18))

        assertTrue(decision is MergeDecision.AddNew)
    }

    private fun item(id: String, filePath: String, startLine: Int, endLine: Int): ContextItem =
        ContextItem(
            id = id,
            projectName = "Demo",
            filePath = filePath,
            language = "Kotlin",
            selectedText = "text",
            startLine = startLine,
            endLine = endLine
        )
}
