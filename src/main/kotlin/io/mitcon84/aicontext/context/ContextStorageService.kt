package io.mitcon84.aicontext.context

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class ContextStorageService(private val project: Project) {
    private val items = mutableListOf<ContextItem>()
    private val listeners = mutableListOf<() -> Unit>()
    private val mergeDecider = ContextMergeDecider()
    private var statusMessage: String? = null

    fun addItem(item: ContextItem, statusMessage: String? = null) {
        items.add(item)
        this.statusMessage = statusMessage
        notifyChanged()
    }

    fun addOrMergeItem(
        newItem: ContextItem,
        textProvider: ((startLine: Int, endLine: Int) -> String)? = null
    ): AddContextResult {
        return when (val decision = mergeDecider.decide(items, newItem)) {
            MergeDecision.AddNew -> {
                items.add(newItem)
                val result = AddContextResult.Added(newItem)
                statusMessage = result.toStatusMessage()
                notifyChanged()
                result
            }

            is MergeDecision.SkipAlreadyCovered -> {
                val result = AddContextResult.SkippedAlreadyCovered(decision.coveringItem)
                statusMessage = result.toStatusMessage()
                notifyChanged()
                result
            }

            is MergeDecision.Merge -> {
                val firstMergedItemIndex = items.indexOfFirst { item ->
                    decision.itemsToMerge.any { it.id == item.id }
                }
                val mergedText = textProvider?.invoke(decision.startLine, decision.endLine)
                    ?: newItem.selectedText
                val mergedItem = newItem.copy(
                    id = decision.itemsToMerge.first().id,
                    selectedText = mergedText,
                    startLine = decision.startLine,
                    endLine = decision.endLine,
                    addedAt = decision.itemsToMerge.first().addedAt
                )

                items.removeAll { item -> decision.itemsToMerge.any { it.id == item.id } }
                items.add(firstMergedItemIndex.coerceAtLeast(0).coerceAtMost(items.size), mergedItem)

                val result = AddContextResult.Merged(mergedItem)
                statusMessage = result.toStatusMessage()
                notifyChanged()
                result
            }
        }
    }

    fun getItems(): List<ContextItem> = items.toList()

    fun getStatusMessage(): String? = statusMessage

    fun clear(statusMessage: String? = null) {
        items.clear()
        this.statusMessage = statusMessage
        notifyChanged()
    }

    fun removeLast(statusMessage: String? = null): ContextItem? {
        if (items.isEmpty()) {
            return null
        }

        val removedItem = items.removeAt(items.lastIndex)
        this.statusMessage = statusMessage
        notifyChanged()
        return removedItem
    }

    fun removeItem(id: String, statusMessage: String? = null): ContextItem? {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) {
            return null
        }

        val removedItem = items.removeAt(index)
        this.statusMessage = statusMessage ?: "Removed context item: ${removedItem.shortName()} ${removedItem.lineRangeText()}"
        notifyChanged()
        return removedItem
    }

    fun addChangeListener(listener: () -> Unit) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeChangeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyChanged() {
        listeners.toList().forEach { it.invoke() }
    }

    companion object {
        fun getInstance(project: Project): ContextStorageService =
            project.getService(ContextStorageService::class.java)
    }
}

private fun AddContextResult.toStatusMessage(): String {
    return when (this) {
        is AddContextResult.Added -> "Added selection: ${item.shortName()} ${item.lineRangeText()}"
        is AddContextResult.Merged -> "Merged with existing context: ${item.shortName()} ${item.lineRangeText()}"
        is AddContextResult.SkippedAlreadyCovered -> "Selection already covered by existing context: ${coveringItem.shortName()} ${coveringItem.lineRangeText()}"
    }
}

private fun ContextItem.shortName(): String =
    if (filePath == "Unknown file") filePath else filePath.substringAfterLast('/').substringAfterLast('\\')

private fun ContextItem.lineRangeText(): String =
    if (startLine != null && endLine != null) "lines $startLine-$endLine" else "lines unknown"
