package io.mitcon84.aicontext.ui

import io.mitcon84.aicontext.context.ContextItem
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.io.File
import java.util.Locale
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

class ContextItemPanel(
    index: Int,
    private val item: ContextItem,
    private val expanded: Boolean,
    onToggle: () -> Unit,
    onCopy: () -> Unit,
    onRemove: () -> Unit
) : JPanel(BorderLayout()) {
    init {
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)
        )

        add(JButton(headerText(index)).apply {
            horizontalAlignment = JButton.LEFT
            addActionListener { onToggle() }
        }, BorderLayout.NORTH)

        if (expanded) {
            add(expandedContent(onCopy, onRemove), BorderLayout.CENTER)
        }
    }

    private fun expandedContent(onCopy: () -> Unit, onRemove: () -> Unit): JPanel {
        return JPanel(BorderLayout()).apply {
            add(
                JTextArea(metadataText()).apply {
                    isEditable = false
                    lineWrap = true
                    wrapStyleWord = true
                    border = BorderFactory.createEmptyBorder(6, 0, 6, 0)
                },
                BorderLayout.NORTH
            )
            add(
                JScrollPane(
                    JTextArea(item.selectedText).apply {
                        isEditable = false
                        lineWrap = false
                        rows = item.selectedText.lineSequence().count().coerceIn(4, 16)
                    }
                ).apply {
                    border = BorderFactory.createTitledBorder("Selected code")
                },
                BorderLayout.CENTER
            )
            add(
                JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                    add(JButton("Copy Item").apply {
                        addActionListener { onCopy() }
                    })
                    add(JButton("Remove").apply {
                        addActionListener { onRemove() }
                    })
                },
                BorderLayout.SOUTH
            )
        }
    }

    private fun headerText(index: Int): String {
        val toggle = if (expanded) "[v]" else "[>]"
        val range = lineRange()?.let { "lines $it" } ?: "lines unknown"
        val chars = item.selectedText.length
        val tokens = approxTokens(chars)
        return "$toggle ${index + 1}. ${fileName()} - $range - ${item.language} - ${formatNumber(chars)} chars - ~${formatNumber(tokens)} tokens"
    }

    private fun metadataText(): String {
        return buildString {
            appendLine("File: ${item.filePath}")
            appendLine("Project: ${item.projectName}")
            lineRange()?.let { appendLine("Lines: $it") }
        }.trimEnd()
    }

    private fun fileName(): String {
        return if (item.filePath == "Unknown file") {
            item.filePath
        } else {
            File(item.filePath).name
        }
    }

    private fun lineRange(): String? {
        val startLine = item.startLine ?: return null
        val endLine = item.endLine ?: return null
        return "$startLine-$endLine"
    }

    private fun approxTokens(chars: Int): Int = if (chars == 0) 0 else (chars + 3) / 4

    private fun formatNumber(value: Int): String = String.format(Locale.US, "%,d", value)
}
