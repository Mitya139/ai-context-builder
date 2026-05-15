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
                    lineRange(item)?.let { appendLine("- Lines: `$it`") }
                    appendLine()
                    appendCodeBlock(item.selectedText)
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

    fun buildRawContext(items: List<ContextItem>): String {
        return buildString {
            appendLine("# Raw IDE Context")
            appendLine()

            if (items.isEmpty()) {
                appendLine("No context items were added.")
                return@buildString
            }

            items.forEachIndexed { index, item ->
                appendLine("## Item ${index + 1}")
                appendLine()
                appendLine("- Project: `${item.projectName}`")
                appendLine("- File: `${item.filePath}`")
                appendLine("- Language: `${item.language}`")
                lineRange(item)?.let { appendLine("- Lines: `$it`") }
                appendLine()
                appendCodeBlock(item.selectedText)
                if (index != items.lastIndex) {
                    appendLine()
                }
            }
        }
    }

    fun buildItemContext(item: ContextItem): String {
        return buildString {
            appendLine("## Context Item")
            appendLine()
            appendLine("- Project: `${item.projectName}`")
            appendLine("- File: `${item.filePath}`")
            lineRange(item)?.let { appendLine("- Lines: `$it`") }
            appendLine("- Language: `${item.language}`")
            appendLine()
            appendCodeBlock(item.selectedText)
        }
    }

    private fun StringBuilder.appendCodeBlock(text: String) {
        appendLine("```text")
        append(text)
        if (!text.endsWith("\n") && !text.endsWith("\r")) {
            appendLine()
        }
        appendLine("```")
    }

    private fun lineRange(item: ContextItem): String? {
        val startLine = item.startLine ?: return null
        val endLine = item.endLine ?: return null
        return "$startLine-$endLine"
    }
}
