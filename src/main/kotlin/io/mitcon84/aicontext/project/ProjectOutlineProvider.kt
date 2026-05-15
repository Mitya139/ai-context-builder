package io.mitcon84.aicontext.project

import com.intellij.openapi.project.Project
import java.nio.file.Path

class ProjectOutlineProvider(
    private val fileCollector: ProjectFileCollector = ProjectFileCollector()
) {
    fun collect(project: Project, maxFiles: Int = 200): ProjectOutline {
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
}
