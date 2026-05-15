package io.mitcon84.aicontext.ui

import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import io.mitcon84.aicontext.context.ContextItem
import io.mitcon84.aicontext.context.ContextStorageService
import io.mitcon84.aicontext.prompt.PromptBuilder
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.datatransfer.StringSelection
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

class AiContextPanel(private val project: Project) : JPanel(BorderLayout()) {
    private val storage = ContextStorageService.getInstance(project)
    private val textArea = JTextArea().apply {
        isEditable = false
        lineWrap = false
    }

    init {
        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(JButton("Refresh").apply {
                addActionListener { refresh() }
            })
            add(JButton("Copy Prompt").apply {
                addActionListener { copyPrompt() }
            })
            add(JButton("Clear").apply {
                addActionListener {
                    storage.clear()
                    refresh()
                }
            })
        }

        add(buttonPanel, BorderLayout.NORTH)
        add(JScrollPane(textArea), BorderLayout.CENTER)
        refresh()
    }

    private fun refresh() {
        val items = storage.getItems()
        textArea.text = renderItems(items)
        textArea.caretPosition = 0
    }

    private fun copyPrompt() {
        val items = storage.getItems()
        val prompt = PromptBuilder().build(items)
        CopyPasteManager.getInstance().setContents(StringSelection(prompt))
        textArea.text = "${renderItems(items)}\n\nPrompt copied to clipboard."
        textArea.caretPosition = 0
    }

    private fun renderItems(items: List<ContextItem>): String {
        return if (items.isEmpty()) {
            """
            No context items yet.

            Select code in the editor, right-click, and choose 'Add Selection to AI Context'.
            """.trimIndent()
        } else {
            buildSummary(items)
        }
    }

    private fun buildSummary(items: List<ContextItem>): String {
        return buildString {
            appendLine("AI Context Items: ${items.size}")
            appendLine()
            items.forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.filePath}")
                appendLine("   Language: ${item.language}")
                appendLine("   Selected chars: ${item.selectedText.length}")
                if (index != items.lastIndex) {
                    appendLine()
                }
            }
        }.trimEnd()
    }
}
