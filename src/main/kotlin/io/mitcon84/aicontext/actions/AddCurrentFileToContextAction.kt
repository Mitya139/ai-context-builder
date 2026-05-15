package io.mitcon84.aicontext.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import io.mitcon84.aicontext.context.ContextStorageService

class AddCurrentFileToContextAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE)

        val item = EditorContextItemFactory.fromWholeFile(project, editor, virtualFile)
        if (item == null) {
            Messages.showInfoMessage(project, "Current file is empty.", "AI Context")
            return
        }

        ContextStorageService.getInstance(project).addOrMergeItem(item) { mergedStartLine, mergedEndLine ->
            EditorContextItemFactory.extractLineRange(editor.document, mergedStartLine, mergedEndLine)
        }
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.getData(CommonDataKeys.EDITOR) != null
    }
}
