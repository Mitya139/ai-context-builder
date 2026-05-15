package io.mitcon84.aicontext.project

object ProjectOutlineFileFilter {
    fun shouldSkipDirectory(directoryName: String): Boolean =
        directoryName in ignoredDirectories

    fun shouldIncludePath(relativePath: String): Boolean {
        val segments = relativePath.split('/').filter { it.isNotBlank() }
        if (segments.any { it in ignoredDirectories }) {
            return false
        }

        val fileName = segments.lastOrNull().orEmpty()
        if (ignoredExtensions.any { fileName.endsWith(it, ignoreCase = true) }) {
            return false
        }

        return includedExtensions.any { relativePath.endsWith(it, ignoreCase = true) }
    }

    private val ignoredDirectories = setOf(
        ".git",
        ".gradle",
        ".idea",
        "build",
        "out",
        "target",
        "node_modules",
        "dist",
        ".intellijPlatform",
        ".venv",
        "venv",
        ".next",
        ".turbo",
        "coverage"
    )

    private val includedExtensions = listOf(
        ".kt",
        ".java",
        ".xml",
        ".kts",
        ".gradle",
        ".properties",
        ".md",
        ".json",
        ".yml",
        ".yaml",
        ".toml",
        ".py",
        ".js",
        ".jsx",
        ".ts",
        ".tsx",
        ".go",
        ".rs",
        ".sql",
        ".sh",
        ".html",
        ".css"
    )

    private val ignoredExtensions = listOf(
        ".class",
        ".jar",
        ".zip",
        ".png",
        ".jpg",
        ".jpeg",
        ".gif",
        ".svg",
        ".ico"
    )
}
