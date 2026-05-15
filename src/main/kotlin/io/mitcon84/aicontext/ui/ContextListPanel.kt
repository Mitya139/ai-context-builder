package io.mitcon84.aicontext.ui

import io.mitcon84.aicontext.context.ContextItem
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

class ContextListPanel(
    private val onCopyItem: (ContextItem) -> Unit,
    private val onRemoveItem: (ContextItem) -> Unit
) : JPanel() {
    private val expandedItemIds = mutableSetOf<String>()
    private val knownItemIds = mutableSetOf<String>()

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    fun render(items: List<ContextItem>) {
        val currentIds = items.map { it.id }.toSet()
        val newIds = currentIds - knownItemIds
        expandedItemIds.retainAll(currentIds)
        expandedItemIds.addAll(newIds)
        knownItemIds.clear()
        knownItemIds.addAll(currentIds)

        removeAll()

        if (items.isEmpty()) {
            add(JPanel(BorderLayout()).apply {
                add(
                    JLabel(
                        "<html>No context items yet.<br><br>Select code in the editor, right-click, and choose <b>Add Selection to AI Context</b>.<br>Then describe your task above and click <b>Copy Prompt</b> or <b>Check Context Readiness</b>.</html>"
                    ),
                    BorderLayout.NORTH
                )
            })
        } else {
            items.forEachIndexed { index, item ->
                val panel = ContextItemPanel(
                    index = index,
                    item = item,
                    expanded = expandedItemIds.contains(item.id),
                    onToggle = {
                        if (expandedItemIds.contains(item.id)) {
                            expandedItemIds.remove(item.id)
                        } else {
                            expandedItemIds.add(item.id)
                        }
                        render(items)
                    },
                    onCopy = { onCopyItem(item) },
                    onRemove = { onRemoveItem(item) }
                )
                panel.alignmentX = Component.LEFT_ALIGNMENT
                add(panel)
                add(Box.createVerticalStrut(6))
            }
        }

        revalidate()
        repaint()
    }
}
