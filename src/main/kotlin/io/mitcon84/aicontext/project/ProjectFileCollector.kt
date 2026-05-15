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
                if (dir != basePath && ProjectOutlineFileFilter.shouldSkipDirectory(dir.fileName.toString())) {
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
                    if (matchingFiles > maxFiles) {
                        return FileVisitResult.TERMINATE
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
        return ProjectOutlineFileFilter.shouldIncludePath(relative.joinToString("/"))
    }

    private fun toRelativePath(basePath: Path, file: Path): String =
        file.relativeTo(basePath).joinToString("/")

    data class CollectionResult(
        val files: List<String>,
        val truncated: Boolean
    )

}
