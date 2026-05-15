package io.mitcon84.aicontext.project

data class ProjectOutline(
    val projectName: String,
    val basePath: String?,
    val files: List<String>,
    val truncated: Boolean
)
