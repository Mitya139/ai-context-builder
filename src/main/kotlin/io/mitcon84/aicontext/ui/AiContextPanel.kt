package io.mitcon84.aicontext.ui

import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import io.mitcon84.aicontext.context.ContextItem
import io.mitcon84.aicontext.context.ContextStorageService
import io.mitcon84.aicontext.prompt.PromptBuilder
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.datatransfer.StringSelection
import java.util.Locale
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.SwingUtilities

class AiContextPanel(private val project: Project) : JPanel(BorderLayout()) {
    private val storage = ContextStorageService.getInstance(project)
    private val userTaskArea = JTextArea(3, 30).apply {
        lineWrap = true
        wrapStyleWord = true
    }
    private val textArea = JTextArea().apply {
        isEditable = false
        lineWrap = false
    }
    private val storageListener: () -> Unit = {
        SwingUtilities.invokeLater { refresh() }
    }

    init {
        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(JButton("Copy Prompt").apply {
                addActionListener { copyPrompt() }
            })
            add(JButton("Copy Raw Context").apply {
                addActionListener { copyRawContext() }
            })
            add(JButton("Remove Last").apply {
                addActionListener { removeLast() }
            })
            add(JButton("Clear").apply {
                addActionListener { clearContext() }
            })
            add(JButton("Refresh").apply {
                addActionListener { refresh() }
            })
        }

        val topPanel = JPanel(BorderLayout()).apply {
            add(JScrollPane(userTaskArea).apply {
                border = BorderFactory.createTitledBorder("User Task")
            }, BorderLayout.CENTER)
            add(buttonPanel, BorderLayout.SOUTH)
        }

        add(topPanel, BorderLayout.NORTH)
        add(JScrollPane(textArea), BorderLayout.CENTER)
        refresh()
    }

    override fun addNotify() {
        super.addNotify()
        storage.addChangeListener(storageListener)
    }

    override fun removeNotify() {
        storage.removeChangeListener(storageListener)
        super.removeNotify()
    }

    private fun refresh(statusMessage: String? = storage.getStatusMessage()) {
        val items = storage.getItems()
        textArea.text = renderItems(items, statusMessage)
        textArea.caretPosition = 0
    }

    private fun copyPrompt() {
        val items = storage.getItems()
        val prompt = PromptBuilder().build(items, userTaskArea.text)
        CopyPasteManager.getInstance().setContents(StringSelection(prompt))
        refresh("Prompt copied to clipboard.")
    }

    private fun copyRawContext() {
        val rawContext = PromptBuilder().buildRawContext(storage.getItems())
        CopyPasteManager.getInstance().setContents(StringSelection(rawContext))
        refresh("Raw context copied to clipboard.")
    }

    private fun removeLast() {
        val removedItem = storage.removeLast("Removed last context item.")
        if (removedItem == null) {
            refresh("No context items to remove.")
        }
    }

    private fun clearContext() {
        if (storage.getItems().isEmpty()) {
            refresh("Context is already empty.")
        } else {
            storage.clear("Context cleared.")
        }
    }

    private fun renderItems(items: List<ContextItem>, statusMessage: String?): String {
        return if (items.isEmpty()) {
            buildString {
                appendStatus(statusMessage)
                appendLine(
                    """
            No context items yet.

            Select code in the editor, right-click, and choose 'Add Selection to AI Context'.
            Then describe your task above and click 'Copy Prompt'.
                    """.trimIndent()
                )
            }.trimEnd()
        } else {
            buildDetailedPreview(items, statusMessage)
        }
    }

    private fun buildDetailedPreview(items: List<ContextItem>, statusMessage: String?): String {
        val totalChars = items.sumOf { it.selectedText.length }
        val approxTokens = if (totalChars == 0) 0 else (totalChars + 3) / 4

        return buildString {
            appendStatus(statusMessage)
            appendLine("AI Context Builder")
            appendLine()
            appendLine("Items: ${items.size}")
            appendLine("Total characters: ${formatNumber(totalChars)}")
            appendLine("Approx. tokens: ~${formatNumber(approxTokens)}")
            appendLine()
            items.forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.filePath}")
                appendLine("   Language: ${item.language}")
                lineRange(item)?.let { appendLine("   Lines: $it") }
                appendLine("   Selected chars: ${item.selectedText.length}")
                appendLine()
                appendLine("   -- Selected code --")
                appendLine(item.selectedText.trimEnd())
                appendLine()
                appendLine("----------------------------------------")
                appendLine()
            }
        }.trimEnd()
    }

    private fun StringBuilder.appendStatus(statusMessage: String?) {
        if (!statusMessage.isNullOrBlank()) {
            appendLine(statusMessage)
            appendLine()
        }
    }

    private fun lineRange(item: ContextItem): String? {
        val startLine = item.startLine ?: return null
        val endLine = item.endLine ?: return null
        return "$startLine-$endLine"
    }

    private fun formatNumber(value: Int): String = String.format(Locale.US, "%,d", value)
}
