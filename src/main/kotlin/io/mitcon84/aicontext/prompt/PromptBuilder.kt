package io.mitcon84.aicontext.prompt

import io.mitcon84.aicontext.context.ContextItem

class PromptBuilder {
    fun build(items: List<ContextItem>, userTask: String = ""): String {
        return buildString {
            appendLine("# AI Coding Agent Context")
            appendLine()
            appendLine("You are helping with a coding task inside an IntelliJ-based IDE.")
            appendLine("Use the context below to reason about the project and provide a precise, safe answer.")
            appendLine()

            if (userTask.isNotBlank()) {
                appendLine("## User Task")
                appendLine()
                appendLine(userTask.trim())
                appendLine()
            }

            appendLine("## Context Items")
            appendLine()

            if (items.isEmpty()) {
                appendLine("No context items were added.")
                appendLine()
            } else {
                items.forEachIndexed { index, item ->
                    appendLine("### Context Item ${index + 1}")
                    appendLine()
                    appendLine("- Project: `${item.projectName}`")
                    appendLine("- File: `${item.filePath}`")
                    appendLine("- Language: `${item.language}`")
                    appendLine()
                    appendLine("```text")
                    appendLine(item.selectedText.trim())
                    appendLine("```")
                    appendLine()
                }
            }

            appendLine("## Instructions")
            appendLine()
            appendLine("- Use only the provided context unless you clearly state an assumption.")
            appendLine("- Prefer minimal, safe changes.")
            appendLine("- Explain the reasoning briefly.")
            appendLine("- If code changes are needed, show the exact files and snippets to modify.")
        }
    }
}
