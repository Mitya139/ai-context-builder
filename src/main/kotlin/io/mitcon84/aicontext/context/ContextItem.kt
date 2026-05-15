package io.mitcon84.aicontext.context

import java.time.LocalDateTime

data class ContextItem(
    val projectName: String,
    val filePath: String,
    val language: String,
    val selectedText: String,
    val addedAt: LocalDateTime = LocalDateTime.now()
)
