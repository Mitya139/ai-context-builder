package io.mitcon84.aicontext.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import io.mitcon84.aicontext.context.ContextItem
import io.mitcon84.aicontext.context.ContextStorageService

class AddSelectionToContextAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val selectedText = editor.selectionModel.selectedText

        if (selectedText.isNullOrEmpty()) {
            Messages.showInfoMessage(project, "Please select code before adding it to AI context.", "AI Context")
            return
        }

        val item = ContextItem(
            projectName = project.name,
            filePath = virtualFile.path,
            language = virtualFile.fileType.name,
            selectedText = selectedText
        )

        ContextStorageService.getInstance(project).addItem(item)
        Messages.showInfoMessage(project, "Selection added to AI context.", "AI Context")
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.getData(CommonDataKeys.EDITOR) != null
    }
}
