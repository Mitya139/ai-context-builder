package io.mitcon84.aicontext.ui

import java.awt.Dimension
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.UIManager

object UiComponentStyling {
    fun styleTextArea(textArea: JTextArea) {
        textArea.background = UIManager.getColor("TextArea.background")
            ?: UIManager.getColor("TextField.background")
            ?: UIManager.getColor("Panel.background")
        textArea.foreground = UIManager.getColor("TextArea.foreground")
            ?: UIManager.getColor("TextField.foreground")
        textArea.caretColor = UIManager.getColor("TextArea.caretForeground")
            ?: UIManager.getColor("TextField.caretForeground")
        textArea.isOpaque = true
    }

    fun styleScrollPane(scrollPane: JScrollPane, preferredHeight: Int? = null) {
        val background = UIManager.getColor("TextArea.background")
            ?: UIManager.getColor("TextField.background")
            ?: UIManager.getColor("Panel.background")
        scrollPane.background = background
        scrollPane.viewport.background = background
        preferredHeight?.let {
            scrollPane.preferredSize = Dimension(0, it)
            scrollPane.minimumSize = Dimension(0, it.coerceAtMost(120))
        }
    }
}
