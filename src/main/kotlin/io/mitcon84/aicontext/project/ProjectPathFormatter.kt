package io.mitcon84.aicontext.project

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

object ProjectPathFormatter {
    fun displayPath(project: Project, file: VirtualFile?): String {
        if (file == null) {
            return UNKNOWN_FILE
        }

        val basePath = project.basePath ?: return file.name
        val projectDir = LocalFileSystem.getInstance().findFileByPath(basePath.replace('\\', '/'))
        val relativePath = projectDir?.let { VfsUtilCore.getRelativePath(file, it, '/') }
        return relativePath ?: file.name
    }

    const val UNKNOWN_FILE = "Unknown file"
}
