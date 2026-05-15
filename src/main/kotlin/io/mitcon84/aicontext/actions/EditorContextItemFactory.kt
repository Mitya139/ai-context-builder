package io.mitcon84.aicontext.actions

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import io.mitcon84.aicontext.context.ContextItem
import io.mitcon84.aicontext.project.ProjectPathFormatter

object EditorContextItemFactory {
    fun fromSelection(project: Project, editor: Editor, virtualFile: VirtualFile?): ContextItem? {
        val selectedText = editor.selectionModel.selectedText?.takeIf { it.isNotBlank() } ?: return null
        val selectionStart = editor.selectionModel.selectionStart
        val selectionEnd = editor.selectionModel.selectionEnd
        val endOffsetForLine = (selectionEnd - 1).coerceAtLeast(selectionStart)

        return create(
            project = project,
            document = editor.document,
            virtualFile = virtualFile,
            text = selectedText,
            startLine = editor.document.getLineNumber(selectionStart) + 1,
            endLine = editor.document.getLineNumber(endOffsetForLine) + 1
        )
    }

    fun fromWholeFile(project: Project, editor: Editor, virtualFile: VirtualFile?): ContextItem? {
        val document = editor.document
        val text = document.text.takeIf { it.isNotBlank() } ?: return null
        val lineCount = document.lineCount.coerceAtLeast(1)

        return create(
            project = project,
            document = document,
            virtualFile = virtualFile,
            text = text,
            startLine = 1,
            endLine = lineCount
        )
    }

    fun extractLineRange(document: Document, startLine: Int, endLine: Int): String {
        val startLineIndex = (startLine - 1).coerceIn(0, document.lineCount - 1)
        val endLineIndex = (endLine - 1).coerceIn(startLineIndex, document.lineCount - 1)
        val startOffset = document.getLineStartOffset(startLineIndex)
        val endOffset = document.getLineEndOffset(endLineIndex)
        return document.getText(TextRange(startOffset, endOffset))
    }

    private fun create(
        project: Project,
        document: Document,
        virtualFile: VirtualFile?,
        text: String,
        startLine: Int,
        endLine: Int
    ): ContextItem {
        val normalizedEndLine = endLine.coerceAtLeast(startLine).coerceAtMost(document.lineCount.coerceAtLeast(1))
        return ContextItem(
            projectName = project.name,
            filePath = ProjectPathFormatter.displayPath(project, virtualFile),
            language = virtualFile?.fileType?.name ?: "unknown",
            selectedText = text,
            startLine = startLine,
            endLine = normalizedEndLine
        )
    }
}
