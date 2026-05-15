package io.mitcon84.aicontext.readiness

import com.intellij.openapi.project.Project
import io.mitcon84.aicontext.ai.AiClientException
import io.mitcon84.aicontext.ai.AiClientFactory
import io.mitcon84.aicontext.context.ContextItem
import io.mitcon84.aicontext.project.ProjectOutlineProvider

class ReadinessCheckRunner(
    private val projectOutlineProvider: ProjectOutlineProvider = ProjectOutlineProvider(),
    private val readinessPromptBuilder: ReadinessPromptBuilder = ReadinessPromptBuilder(),
    private val aiClientFactory: AiClientFactory = AiClientFactory()
) {
    fun check(project: Project, userTask: String, items: List<ContextItem>): ReadinessCheckResult {
        return runCatching {
            val outline = projectOutlineProvider.collect(project)
            val prompt = readinessPromptBuilder.build(userTask, items, outline)
            val clientSelection = aiClientFactory.createFromSettings()
            val report = clientSelection.client.complete(prompt)

            ReadinessCheckResult.Success(
                report = if (clientSelection.statusMessage == null) {
                    report
                } else {
                    "${clientSelection.statusMessage}\n\n$report"
                }
            )
        }.getOrElse { error ->
            ReadinessCheckResult.Failure(
                message = if (error is AiClientException) {
                    "AI readiness check failed: ${error.message}"
                } else {
                    "AI readiness check failed: ${error.message ?: error::class.java.simpleName}"
                }
            )
        }
    }
}

sealed class ReadinessCheckResult {
    data class Success(val report: String) : ReadinessCheckResult()
    data class Failure(val message: String) : ReadinessCheckResult()
}
