package io.mitcon84.aicontext.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import io.mitcon84.aicontext.context.ContextItem
import io.mitcon84.aicontext.context.ContextStorageService
import io.mitcon84.aicontext.prompt.PromptBuilder
import io.mitcon84.aicontext.readiness.ReadinessCheckResult
import io.mitcon84.aicontext.readiness.ReadinessCheckRunner
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.datatransfer.StringSelection
import java.util.Locale
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTextArea
import javax.swing.SwingUtilities

class AiContextPanel(private val project: Project) : JPanel(BorderLayout()) {
    private val storage = ContextStorageService.getInstance(project)
    private val promptBuilder = PromptBuilder()
    private val readinessCheckRunner = ReadinessCheckRunner()

    private val userTaskArea = JTextArea(3, 40).apply {
        lineWrap = true
        wrapStyleWord = true
        UiComponentStyling.styleTextArea(this)
    }
    private val summaryLabel = JLabel()
    private val checkReadinessButton = JButton("Check Context Readiness")
    private val detailsPanel = ContextItemDetailsPanel(
        onCopySelectedItem = { item -> copyItem(item) },
        onRemoveSelectedItem = { item -> removeItem(item) }
    )
    private val contextListPanel = ContextItemListPanel(
        onSelectionChanged = { item -> detailsPanel.render(item) }
    )
    private val readinessResultPanel = ReadinessResultPanel()
    private val storageListener: () -> Unit = {
        SwingUtilities.invokeLater { refresh() }
    }

    init {
        val actionPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(checkReadinessButton.apply {
                addActionListener { checkContextReadiness() }
            })
            add(JButton("Copy Prompt").apply {
                addActionListener { copyPrompt() }
            })
            add(JButton("Copy Raw Context").apply {
                addActionListener { copyRawContext() }
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
                UiComponentStyling.styleScrollPane(this)
            }, BorderLayout.CENTER)
            add(summaryLabel, BorderLayout.SOUTH)
        }

        val contextPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder("Context Items")
            add(contextListPanel, BorderLayout.CENTER)
        }

        val contextAndDetailsPane = JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            contextPanel,
            detailsPanel
        ).apply {
            resizeWeight = 0.30
            isContinuousLayout = true
        }

        val splitPane = JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            contextAndDetailsPane,
            readinessResultPanel
        ).apply {
            resizeWeight = 0.65
            isContinuousLayout = true
        }

        add(topPanel, BorderLayout.NORTH)
        add(splitPane, BorderLayout.CENTER)
        add(actionPanel, BorderLayout.SOUTH)
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
        summaryLabel.text = buildSummary(items, statusMessage)
        contextListPanel.render(items)
    }

    private fun copyPrompt() {
        val prompt = promptBuilder.build(storage.getItems(), userTaskArea.text)
        CopyPasteManager.getInstance().setContents(StringSelection(prompt))
        refresh("Prompt copied to clipboard.")
    }

    private fun copyRawContext() {
        val rawContext = promptBuilder.buildRawContext(storage.getItems())
        CopyPasteManager.getInstance().setContents(StringSelection(rawContext))
        refresh("Raw context copied to clipboard.")
    }

    private fun copyItem(item: ContextItem) {
        CopyPasteManager.getInstance().setContents(StringSelection(promptBuilder.buildItemContext(item)))
        refresh("Context item copied to clipboard.")
    }

    private fun removeItem(item: ContextItem) {
        storage.removeItem(item.id)
    }

    private fun clearContext() {
        if (storage.getItems().isEmpty()) {
            refresh("Context is already empty.")
        } else {
            storage.clear("Cleared all context items.")
            readinessResultPanel.clear()
        }
    }

    private fun checkContextReadiness() {
        val userTask = userTaskArea.text
        val items = storage.getItems()

        checkReadinessButton.isEnabled = false
        readinessResultPanel.setLoading("Checking context readiness...")

        ApplicationManager.getApplication().executeOnPooledThread {
            val result = readinessCheckRunner.check(project, userTask, items)

            ApplicationManager.getApplication().invokeLater {
                checkReadinessButton.isEnabled = true
                when (result) {
                    is ReadinessCheckResult.Failure -> readinessResultPanel.setError(result.message)
                    is ReadinessCheckResult.Success -> readinessResultPanel.setResult(result.report)
                }
            }
        }
    }

    private fun buildSummary(items: List<ContextItem>, statusMessage: String?): String {
        val totalChars = items.sumOf { it.selectedText.length }
        val approxTokens = approxTokens(totalChars)
        val status = statusMessage?.takeIf { it.isNotBlank() } ?: "Idle"
        return "Items: ${items.size} - Characters: ${formatNumber(totalChars)} - Approx tokens: ${formatNumber(approxTokens)} - Status: $status"
    }

    private fun approxTokens(chars: Int): Int = if (chars == 0) 0 else (chars + 3) / 4

    private fun formatNumber(value: Int): String = String.format(Locale.US, "%,d", value)
}
