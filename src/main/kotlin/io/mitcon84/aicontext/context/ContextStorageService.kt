package io.mitcon84.aicontext.context

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class ContextStorageService(private val project: Project) {
    private val items = mutableListOf<ContextItem>()
    private val listeners = mutableListOf<() -> Unit>()
    private var statusMessage: String? = null

    fun addItem(item: ContextItem, statusMessage: String? = null) {
        items.add(item)
        this.statusMessage = statusMessage
        notifyChanged()
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
        this.statusMessage = statusMessage
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
