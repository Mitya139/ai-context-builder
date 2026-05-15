package io.mitcon84.aicontext.readiness

import io.mitcon84.aicontext.context.ContextItem
import io.mitcon84.aicontext.project.ProjectOutline

class ReadinessPromptBuilder {
    fun build(
        userTask: String,
        contextItems: List<ContextItem>,
        projectOutline: ProjectOutline
    ): String {
        return buildString {
            appendLine("# Context Readiness Check")
            appendLine()
            appendLine("You are evaluating whether the selected IDE context is sufficient for an AI coding agent.")
            appendLine()
            appendLine("## User Task")
            appendLine()
            appendLine(userTask.ifBlank { "No user task was provided." }.trim())
            appendLine()
            appendLine("## Selected Context Items")
            appendLine()

            if (contextItems.isEmpty()) {
                appendLine("No selected context items were provided.")
                appendLine()
            } else {
                contextItems.forEachIndexed { index, item ->
                    appendLine("### Context Item ${index + 1}")
                    appendLine()
                    appendLine("- File: `${item.filePath}`")
                    lineRange(item)?.let { appendLine("- Lines: `$it`") }
                    appendLine("- Language: `${item.language}`")
                    appendLine()
                    appendLine("```text")
                    appendLine(item.selectedText.trim())
                    appendLine("```")
                    appendLine()
                }
            }

            appendLine("## Project Outline")
            appendLine()
            appendLine("You only know the paths of these files, not their contents.")
            if (projectOutline.truncated) {
                appendLine("The file list was truncated because the project has more matching files than the collection limit.")
            }
            appendLine()
            appendLine("```text")
            projectOutline.files.forEach { appendLine(it) }
            appendLine("```")
            appendLine()
            appendLine("## Evaluation Rules")
            appendLine()
            appendLine("- You are not given the full project contents.")
            appendLine("- You can inspect the full code only for selected context items.")
            appendLine("- For other project files, you only know their paths.")
            appendLine("- When suggesting missing files, mark them as candidates and explain that the suggestion is based on file names, paths, and the user task.")
            appendLine("- Do not claim that a file contains specific code unless it was provided in the selected context.")
            appendLine("- Classify the context as one of: Ready, Partial, Insufficient.")
            appendLine()
            appendLine("## Required Output Format")
            appendLine()
            appendLine("Return Markdown with exactly these sections:")
            appendLine()
            appendLine("# Context Readiness Report")
            appendLine()
            appendLine("## Status")
            appendLine("Ready | Partial | Insufficient")
            appendLine()
            appendLine("## Short Explanation")
            appendLine("...")
            appendLine()
            appendLine("## Useful Provided Context")
            appendLine("- ...")
            appendLine()
            appendLine("## Candidate Missing Context")
            appendLine("- `path/to/file.kt` - reason")
            appendLine()
            appendLine("## Suggested Next Step")
            appendLine("...")
        }
    }

    private fun lineRange(item: ContextItem): String? {
        val startLine = item.startLine ?: return null
        val endLine = item.endLine ?: return null
        return "$startLine-$endLine"
    }
}
