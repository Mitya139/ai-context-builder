package io.mitcon84.aicontext.context

import java.time.LocalDateTime
import java.util.UUID

data class ContextItem(
    val id: String = UUID.randomUUID().toString(),
    val projectName: String,
    val filePath: String,
    val language: String,
    val selectedText: String,
    val startLine: Int? = null,
    val endLine: Int? = null,
    val addedAt: LocalDateTime = LocalDateTime.now()
)
