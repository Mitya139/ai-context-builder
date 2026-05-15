package io.mitcon84.aicontext

import io.mitcon84.aicontext.context.ContextItem
import io.mitcon84.aicontext.project.ProjectOutline
import io.mitcon84.aicontext.readiness.ReadinessPromptBuilder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadinessPromptBuilderTest {
    private val builder = ReadinessPromptBuilder()

    @Test
    fun `includes user task`() {
        val prompt = builder.build(
            userTask = "Check whether the context is enough.",
            contextItems = listOf(sampleItem()),
            projectOutline = sampleOutline()
        )

        assertTrue(prompt.contains("Check whether the context is enough."))
    }

    @Test
    fun `includes selected context item path and code`() {
        val prompt = builder.build(
            userTask = "Improve prompt formatting.",
            contextItems = listOf(sampleItem()),
            projectOutline = sampleOutline()
        )

        assertTrue(prompt.contains("src/main/kotlin/PromptBuilder.kt"))
        assertTrue(prompt.contains("class PromptBuilder"))
    }

    @Test
    fun `includes line range when available`() {
        val prompt = builder.build(
            userTask = "Improve prompt formatting.",
            contextItems = listOf(sampleItem(startLine = 12, endLine = 24)),
            projectOutline = sampleOutline()
        )

        assertTrue(prompt.contains("- Lines: `12-24`"))
    }

    @Test
    fun `includes project outline paths`() {
        val prompt = builder.build(
            userTask = "Improve prompt formatting.",
            contextItems = listOf(sampleItem()),
            projectOutline = sampleOutline()
        )

        assertTrue(prompt.contains("src/main/kotlin/io/mitcon84/aicontext/prompt/PromptBuilder.kt"))
        assertTrue(prompt.contains("src/test/kotlin/io/mitcon84/aicontext/PromptBuilderTest.kt"))
    }

    @Test
    fun `includes honesty constraint that full project contents are not provided`() {
        val prompt = builder.build(
            userTask = "Improve prompt formatting.",
            contextItems = listOf(sampleItem()),
            projectOutline = sampleOutline()
        )

        assertTrue(prompt.contains("You are not given the full project contents."))
        assertTrue(prompt.contains("you only know their paths"))
        assertTrue(prompt.contains("Do not claim that a file contains specific code"))
    }

    @Test
    fun `handles empty context item list`() {
        val prompt = builder.build(
            userTask = "Improve prompt formatting.",
            contextItems = emptyList(),
            projectOutline = sampleOutline()
        )

        assertTrue(prompt.contains("No selected context items were provided."))
    }

    @Test
    fun `handles truncated project outline`() {
        val prompt = builder.build(
            userTask = "Improve prompt formatting.",
            contextItems = listOf(sampleItem()),
            projectOutline = sampleOutline(truncated = true)
        )

        assertTrue(prompt.contains("The file list was truncated"))
    }

    @Test
    fun `omits line range when unavailable`() {
        val prompt = builder.build(
            userTask = "Improve prompt formatting.",
            contextItems = listOf(sampleItem(startLine = null, endLine = null)),
            projectOutline = sampleOutline()
        )

        assertFalse(prompt.contains("- Lines:"))
    }

    private fun sampleItem(startLine: Int? = 12, endLine: Int? = 24): ContextItem =
        ContextItem(
            projectName = "DemoProject",
            filePath = "src/main/kotlin/PromptBuilder.kt",
            language = "Kotlin",
            selectedText = "class PromptBuilder",
            startLine = startLine,
            endLine = endLine
        )

    private fun sampleOutline(truncated: Boolean = false): ProjectOutline =
        ProjectOutline(
            projectName = "DemoProject",
            basePath = "C:/projects/demo",
            files = listOf(
                "src/main/kotlin/io/mitcon84/aicontext/prompt/PromptBuilder.kt",
                "src/test/kotlin/io/mitcon84/aicontext/PromptBuilderTest.kt"
            ),
            truncated = truncated
        )
}
