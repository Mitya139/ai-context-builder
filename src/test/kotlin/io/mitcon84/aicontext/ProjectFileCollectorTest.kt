package io.mitcon84.aicontext

import io.mitcon84.aicontext.project.ProjectFileCollector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

class ProjectFileCollectorTest {
    private val collector = ProjectFileCollector()

    @Test
    fun `excludes ignored directories`() {
        withTempProject { root ->
            write(root.resolve(".git/config"), "ignored")
            write(root.resolve("build/generated.kt"), "ignored")
            write(root.resolve("src/main/App.kt"), "included")

            val result = collector.collect(root)

            assertEquals(listOf("src/main/App.kt"), result.files)
        }
    }

    @Test
    fun `includes source and config files`() {
        withTempProject { root ->
            write(root.resolve("README.md"), "included")
            write(root.resolve("build.gradle.kts"), "included")
            write(root.resolve("src/main/plugin.xml"), "included")
            write(root.resolve("src/main/logo.png"), "ignored")

            val result = collector.collect(root)

            assertTrue(result.files.contains("README.md"))
            assertTrue(result.files.contains("build.gradle.kts"))
            assertTrue(result.files.contains("src/main/plugin.xml"))
            assertFalse(result.files.contains("src/main/logo.png"))
        }
    }

    @Test
    fun `sorts paths`() {
        withTempProject { root ->
            write(root.resolve("z.kt"), "z")
            write(root.resolve("a.kt"), "a")
            write(root.resolve("m.kt"), "m")

            val result = collector.collect(root)

            assertEquals(listOf("a.kt", "m.kt", "z.kt"), result.files)
        }
    }

    @Test
    fun `respects max files`() {
        withTempProject { root ->
            write(root.resolve("a.kt"), "a")
            write(root.resolve("b.kt"), "b")
            write(root.resolve("c.kt"), "c")

            val result = collector.collect(root, maxFiles = 2)

            assertEquals(2, result.files.size)
        }
    }

    @Test
    fun `sets truncated true when file limit is exceeded`() {
        withTempProject { root ->
            write(root.resolve("a.kt"), "a")
            write(root.resolve("b.kt"), "b")
            write(root.resolve("c.kt"), "c")

            val result = collector.collect(root, maxFiles = 2)

            assertTrue(result.truncated)
        }
    }

    private fun withTempProject(block: (Path) -> Unit) {
        val root = Files.createTempDirectory("ai-context-builder-test")
        try {
            block(root)
        } finally {
            root.toFile().deleteRecursively()
        }
    }

    private fun write(path: Path, content: String) {
        Files.createDirectories(path.parent)
        Files.writeString(path, content)
    }
}
