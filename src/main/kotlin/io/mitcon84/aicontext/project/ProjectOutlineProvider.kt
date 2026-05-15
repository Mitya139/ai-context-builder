package io.mitcon84.aicontext.project

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import java.nio.file.Path

class ProjectOutlineProvider(
    private val fileCollector: ProjectFileCollector = ProjectFileCollector()
) {
    fun collect(project: Project, maxFiles: Int = 200): ProjectOutline {
        val projectModelOutline = collectFromProjectModel(project, maxFiles)
        if (projectModelOutline != null) {
            return projectModelOutline
        }

        val basePath = project.basePath ?: return ProjectOutline(
            projectName = project.name,
            basePath = null,
            files = emptyList(),
            truncated = false
        )

        val result = fileCollector.collect(Path.of(basePath), maxFiles)
        return ProjectOutline(
            projectName = project.name,
            basePath = basePath,
            files = result.files,
            truncated = result.truncated
        )
    }

    private fun collectFromProjectModel(project: Project, maxFiles: Int): ProjectOutline? {
        val basePath = project.basePath ?: return null
        return runCatching {
            val fileIndex = ProjectRootManager.getInstance(project).fileIndex
            val files = mutableListOf<String>()
            var truncated = false

            fileIndex.iterateContent { file ->
                if (file.isDirectory || file.fileType.isBinary) {
                    return@iterateContent true
                }

                val relativePath = ProjectPathFormatter.displayPath(project, file)
                if (!ProjectOutlineFileFilter.shouldIncludePath(relativePath)) {
                    return@iterateContent true
                }

                if (files.size >= maxFiles) {
                    truncated = true
                    return@iterateContent false
                }

                files.add(relativePath)
                true
            }

            ProjectOutline(
                projectName = project.name,
                basePath = basePath,
                files = files.sorted(),
                truncated = truncated
            )
        }.getOrNull()
    }
}
