package io.mitcon84.aicontext.project

import java.nio.file.Files
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.relativeTo

class ProjectFileCollector {
    fun collect(basePath: Path, maxFiles: Int = 200): CollectionResult {
        if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
            return CollectionResult(emptyList(), truncated = false)
        }

        val collected = mutableListOf<String>()
        var matchingFiles = 0

        Files.walkFileTree(basePath, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (dir != basePath && dir.fileName.toString() in ignoredDirectories) {
                    return FileVisitResult.SKIP_SUBTREE
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (attrs.isRegularFile && shouldInclude(basePath, file)) {
                    matchingFiles++
                    if (collected.size < maxFiles) {
                        collected.add(toRelativePath(basePath, file))
                    }
                }
                return FileVisitResult.CONTINUE
            }
        })

        return CollectionResult(
            files = collected.sorted(),
            truncated = matchingFiles > maxFiles
        )
    }

    private fun shouldInclude(basePath: Path, file: Path): Boolean {
        val relative = file.relativeTo(basePath)
        val segments = relative.map { it.fileName.toString() }.toList()
        if (segments.any { it in ignoredDirectories }) {
            return false
        }

        val fileName = file.fileName.toString()
        if (ignoredExtensions.any { fileName.endsWith(it, ignoreCase = true) }) {
            return false
        }

        val normalized = toRelativePath(basePath, file)
        return includedExtensions.any { normalized.endsWith(it, ignoreCase = true) }
    }

    private fun toRelativePath(basePath: Path, file: Path): String =
        file.relativeTo(basePath).joinToString("/")

    data class CollectionResult(
        val files: List<String>,
        val truncated: Boolean
    )

    private companion object {
        val ignoredDirectories = setOf(
            ".git",
            ".gradle",
            ".idea",
            "build",
            "out",
            "target",
            "node_modules",
            "dist",
            ".intellijPlatform"
        )

        val includedExtensions = listOf(
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
            ".toml"
        )

        val ignoredExtensions = listOf(
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
}
