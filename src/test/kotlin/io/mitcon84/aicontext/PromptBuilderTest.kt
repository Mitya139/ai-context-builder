package io.mitcon84.aicontext

import io.mitcon84.aicontext.context.ContextItem
import io.mitcon84.aicontext.prompt.PromptBuilder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class PromptBuilderTest {
    private val builder = PromptBuilder()

    @Test
    fun `builds prompt with selected code and file path`() {
        val prompt = builder.build(listOf(sampleItem()))

        assertTrue(prompt.contains("AI Coding Agent Context"))
        assertTrue(prompt.contains("DemoProject"))
        assertTrue(prompt.contains("src/main/kotlin/App.kt"))
        assertTrue(prompt.contains("fun main()"))
    }

    @Test
    fun `omits user task section when user task is blank`() {
        val prompt = builder.build(listOf(sampleItem()), userTask = "   ")

        assertFalse(prompt.contains("## User Task"))
    }

    @Test
    fun `includes user task section when user task is provided`() {
        val prompt = builder.build(listOf(sampleItem()), userTask = "Explain this code.")

        assertTrue(prompt.contains("## User Task"))
        assertTrue(prompt.contains("Explain this code."))
    }

    @Test
    fun `handles empty context list`() {
        val prompt = builder.build(emptyList())

        assertTrue(prompt.contains("AI Coding Agent Context"))
        assertTrue(prompt.contains("No context items were added."))
    }

    @Test
    fun `includes line range when available`() {
        val prompt = builder.build(listOf(sampleItem(startLine = 10, endLine = 16)))

        assertTrue(prompt.contains("- Lines: `10-16`"))
    }

    @Test
    fun `omits line range when unavailable`() {
        val prompt = builder.build(listOf(sampleItem(startLine = null, endLine = null)))

        assertFalse(prompt.contains("- Lines:"))
    }

    @Test
    fun `builds raw context with selected code`() {
        val rawContext = builder.buildRawContext(listOf(sampleItem()))

        assertTrue(rawContext.contains("# Raw IDE Context"))
        assertTrue(rawContext.contains("## Item 1"))
        assertTrue(rawContext.contains("src/main/kotlin/App.kt"))
        assertTrue(rawContext.contains("- Lines: `10-16`"))
        assertTrue(rawContext.contains("fun main()"))
        assertFalse(rawContext.contains("## Instructions"))
    }

    private fun sampleItem(): ContextItem =
        sampleItem(startLine = 10, endLine = 16)

    private fun sampleItem(startLine: Int?, endLine: Int?): ContextItem =
        ContextItem(
            projectName = "DemoProject",
            filePath = "src/main/kotlin/App.kt",
            language = "Kotlin",
            selectedText = "fun main() {\n    println(\"Hello\")\n}",
            startLine = startLine,
            endLine = endLine,
            addedAt = LocalDateTime.of(2026, 5, 15, 12, 0)
        )
}
