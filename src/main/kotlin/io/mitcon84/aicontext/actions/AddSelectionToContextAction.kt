package io.mitcon84.aicontext.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.ui.Messages
import io.mitcon84.aicontext.context.ContextItem
import io.mitcon84.aicontext.context.ContextStorageService

class AddSelectionToContextAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE)
        val selectedText = editor.selectionModel.selectedText

        if (selectedText.isNullOrBlank()) {
            Messages.showInfoMessage(project, "Please select code before adding it to AI context.", "AI Context")
            return
        }

        val selectionStart = editor.selectionModel.selectionStart
        val selectionEnd = editor.selectionModel.selectionEnd
        val startLine = editor.document.getLineNumber(selectionStart) + 1
        val endOffsetForLine = (selectionEnd - 1).coerceAtLeast(selectionStart)
        val endLine = editor.document.getLineNumber(endOffsetForLine) + 1

        val item = ContextItem(
            projectName = project.name,
            filePath = virtualFile?.path ?: "Unknown file",
            language = virtualFile?.fileType?.name ?: "unknown",
            selectedText = selectedText,
            startLine = startLine,
            endLine = endLine
        )

        ContextStorageService.getInstance(project).addOrMergeItem(item) { mergedStartLine, mergedEndLine ->
            extractLineRange(editor.document, mergedStartLine, mergedEndLine)
        }
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.getData(CommonDataKeys.EDITOR) != null
    }

    private fun extractLineRange(document: Document, startLine: Int, endLine: Int): String {
        val startLineIndex = (startLine - 1).coerceIn(0, document.lineCount - 1)
        val endLineIndex = (endLine - 1).coerceIn(startLineIndex, document.lineCount - 1)
        val startOffset = document.getLineStartOffset(startLineIndex)
        val endOffset = document.getLineEndOffset(endLineIndex)
        return document.getText(com.intellij.openapi.util.TextRange(startOffset, endOffset))
    }
}
