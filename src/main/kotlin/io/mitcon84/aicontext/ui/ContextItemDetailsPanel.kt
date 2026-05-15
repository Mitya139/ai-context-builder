package io.mitcon84.aicontext.ui

import io.mitcon84.aicontext.context.ContextItem
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

class ContextItemDetailsPanel(
    private val onCopySelectedItem: (ContextItem) -> Unit,
    private val onRemoveSelectedItem: (ContextItem) -> Unit
) : JPanel(BorderLayout()) {
    private var selectedItem: ContextItem? = null
    private val metadataArea = JTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        rows = 4
        UiComponentStyling.styleTextArea(this)
    }
    private val codeArea = JTextArea().apply {
        isEditable = false
        lineWrap = false
        rows = 8
        UiComponentStyling.styleTextArea(this)
    }
    private val copyButton = JButton("Copy Item").apply {
        addActionListener {
            selectedItem?.let(onCopySelectedItem)
        }
    }
    private val removeButton = JButton("Remove").apply {
        addActionListener {
            selectedItem?.let(onRemoveSelectedItem)
        }
    }

    init {
        minimumSize = Dimension(0, 220)
        preferredSize = Dimension(0, 260)
        border = BorderFactory.createTitledBorder("Selected Item Details")
        add(metadataArea, BorderLayout.NORTH)
        add(
            JScrollPane(codeArea).apply {
                border = BorderFactory.createTitledBorder("Selected code")
                UiComponentStyling.styleScrollPane(this, preferredHeight = 150)
            },
            BorderLayout.CENTER
        )
        add(
            JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(copyButton)
                add(removeButton)
            },
            BorderLayout.SOUTH
        )
        render(null)
    }

    fun render(item: ContextItem?) {
        selectedItem = item
        if (item == null) {
            metadataArea.text = "No context item selected."
            codeArea.text = ""
            copyButton.isEnabled = false
            removeButton.isEnabled = false
            return
        }

        metadataArea.text = buildString {
            appendLine("File: ${item.filePath}")
            appendLine("Project: ${item.projectName}")
            appendLine("Lines: ${lineRange(item) ?: "unknown"}")
            appendLine("Language: ${item.language}")
        }.trimEnd()
        codeArea.text = item.selectedText
        codeArea.caretPosition = 0
        copyButton.isEnabled = true
        removeButton.isEnabled = true
    }

    private fun lineRange(item: ContextItem): String? {
        val startLine = item.startLine ?: return null
        val endLine = item.endLine ?: return null
        return "$startLine-$endLine"
    }
}
