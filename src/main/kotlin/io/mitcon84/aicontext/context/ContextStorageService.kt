package io.mitcon84.aicontext.context

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class ContextStorageService(private val project: Project) {
    private val items = mutableListOf<ContextItem>()

    fun addItem(item: ContextItem) {
        items.add(item)
    }

    fun getItems(): List<ContextItem> = items.toList()

    fun clear() {
        items.clear()
    }

    companion object {
        fun getInstance(project: Project): ContextStorageService =
            project.getService(ContextStorageService::class.java)
    }
}
