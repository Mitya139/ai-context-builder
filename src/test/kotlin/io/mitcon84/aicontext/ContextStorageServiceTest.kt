package io.mitcon84.aicontext

import com.intellij.openapi.project.Project
import io.mitcon84.aicontext.context.AddContextResult
import io.mitcon84.aicontext.context.ContextItem
import io.mitcon84.aicontext.context.ContextStorageService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class ContextStorageServiceTest {
    @Test
    fun `add or merge replaces overlapping items with merged text`() {
        val storage = ContextStorageService(projectStub())
        storage.addOrMergeItem(item(id = "first", startLine = 10, endLine = 20, text = "first"))

        val result = storage.addOrMergeItem(
            item(id = "second", startLine = 18, endLine = 30, text = "second")
        ) { startLine, endLine ->
            "merged $startLine-$endLine"
        }

        assertTrue(result is AddContextResult.Merged)
        val items = storage.getItems()
        assertEquals(1, items.size)
        assertEquals("first", items.single().id)
        assertEquals(10, items.single().startLine)
        assertEquals(30, items.single().endLine)
        assertEquals("merged 10-30", items.single().selectedText)
    }

    @Test
    fun `covered item is skipped without changing storage`() {
        val storage = ContextStorageService(projectStub())
        storage.addOrMergeItem(item(id = "first", startLine = 10, endLine = 30, text = "existing"))

        val result = storage.addOrMergeItem(item(id = "nested", startLine = 15, endLine = 20, text = "nested"))

        assertTrue(result is AddContextResult.SkippedAlreadyCovered)
        val items = storage.getItems()
        assertEquals(1, items.size)
        assertEquals("first", items.single().id)
        assertEquals("existing", items.single().selectedText)
    }

    private fun item(id: String, startLine: Int, endLine: Int, text: String): ContextItem =
        ContextItem(
            id = id,
            projectName = "Demo",
            filePath = "src/App.kt",
            language = "Kotlin",
            selectedText = text,
            startLine = startLine,
            endLine = endLine
        )

    private fun projectStub(): Project =
        Proxy.newProxyInstance(
            Project::class.java.classLoader,
            arrayOf(Project::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getName" -> "Demo"
                "toString" -> "ProjectStub"
                "hashCode" -> 0
                "equals" -> false
                else -> null
            }
        } as Project
}
