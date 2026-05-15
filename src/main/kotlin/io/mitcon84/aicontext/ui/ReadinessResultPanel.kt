package io.mitcon84.aicontext.ui

import com.intellij.openapi.ide.CopyPasteManager
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.datatransfer.StringSelection
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

class ReadinessResultPanel : JPanel(BorderLayout()) {
    private var latestReport: String = ""
    private val textArea = JTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        text = "No AI readiness check has been run yet."
        UiComponentStyling.styleTextArea(this)
    }

    init {
        border = BorderFactory.createTitledBorder("AI Result")
        add(JScrollPane(textArea).apply {
            UiComponentStyling.styleScrollPane(this, preferredHeight = 180)
        }, BorderLayout.CENTER)
        add(
            JPanel(FlowLayout(FlowLayout.LEFT)).apply {
                add(JButton("Copy Readiness Report").apply {
                    addActionListener { copyReport() }
                })
            },
            BorderLayout.SOUTH
        )
    }

    fun setLoading(message: String) {
        latestReport = ""
        textArea.text = message
        textArea.caretPosition = 0
    }

    fun setResult(report: String) {
        latestReport = report
        textArea.text = report
        textArea.caretPosition = 0
    }

    fun setError(message: String) {
        latestReport = message
        textArea.text = message
        textArea.caretPosition = 0
    }

    fun clear() {
        latestReport = ""
        textArea.text = "No AI readiness check has been run yet."
        textArea.caretPosition = 0
    }

    private fun copyReport() {
        if (latestReport.isNotBlank()) {
            CopyPasteManager.getInstance().setContents(StringSelection(latestReport))
        }
    }
}
