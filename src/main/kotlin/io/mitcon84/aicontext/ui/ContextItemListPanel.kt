package io.mitcon84.aicontext.ui

import io.mitcon84.aicontext.context.ContextItem
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Component
import java.io.File
import java.util.Locale
import javax.swing.DefaultListCellRenderer
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ListSelectionModel
import javax.swing.SwingConstants

class ContextItemListPanel(
    private val onSelectionChanged: (ContextItem?) -> Unit
) : JPanel(CardLayout()) {
    private val listModel = DefaultListModel<ContextItem>()
    private var previousSelectedItemId: String? = null
    private val knownItemIds = mutableSetOf<String>()
    private val cardLayout: CardLayout
        get() = layout as CardLayout

    private val list = JList(listModel).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        fixedCellHeight = 28
        cellRenderer = ContextItemRenderer()
        addListSelectionListener {
            if (!it.valueIsAdjusting) {
                val selected = selectedValue
                previousSelectedItemId = selected?.id
                onSelectionChanged(selected)
            }
        }
    }

    init {
        add(
            JLabel(
                "<html>No context items yet.<br><br>Select code in the editor, right-click, and choose <b>Add Selection to AI Context</b>.</html>",
                SwingConstants.CENTER
            ),
            EMPTY_CARD
        )
        add(JScrollPane(list), LIST_CARD)
    }

    fun render(items: List<ContextItem>) {
        val selectedId = previousSelectedItemId
        val currentIds = items.map { it.id }.toSet()
        val newItemId = (currentIds - knownItemIds).firstOrNull()
        knownItemIds.clear()
        knownItemIds.addAll(currentIds)

        listModel.clear()
        items.forEach { listModel.addElement(it) }

        if (items.isEmpty()) {
            previousSelectedItemId = null
            onSelectionChanged(null)
            cardLayout.show(this, EMPTY_CARD)
            return
        }

        cardLayout.show(this, LIST_CARD)
        val preferredSelectedId = newItemId ?: selectedId
        val selectedIndex = items.indexOfFirst { it.id == preferredSelectedId }
            .takeIf { it >= 0 }
            ?: items.lastIndex
        list.selectedIndex = selectedIndex
        list.ensureIndexIsVisible(selectedIndex)
    }

    private class ContextItemRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
            val item = value as? ContextItem
            component.text = if (item == null) {
                ""
            } else {
                rowText(index, item)
            }
            return component
        }

        private fun rowText(index: Int, item: ContextItem): String {
            val range = lineRange(item)?.let { "lines $it" } ?: "lines unknown"
            val chars = item.selectedText.length
            val tokens = if (chars == 0) 0 else (chars + 3) / 4
            return "${index + 1}. ${fileName(item)} - $range - ${item.language} - ${formatNumber(chars)} chars - ~${formatNumber(tokens)} tokens"
        }

        private fun fileName(item: ContextItem): String {
            return if (item.filePath == "Unknown file") {
                item.filePath
            } else {
                File(item.filePath).name
            }
        }

        private fun lineRange(item: ContextItem): String? {
            val startLine = item.startLine ?: return null
            val endLine = item.endLine ?: return null
            return "$startLine-$endLine"
        }

        private fun formatNumber(value: Int): String = String.format(Locale.US, "%,d", value)
    }

    private companion object {
        const val EMPTY_CARD = "empty"
        const val LIST_CARD = "list"
    }
}
