package io.mitcon84.aicontext.ai

import io.mitcon84.aicontext.settings.AiConfigurationResolver
import io.mitcon84.aicontext.settings.AiSettingsService
import io.mitcon84.aicontext.settings.ResolvedAiConfiguration

class AiClientFactory {
    private val resolver = AiConfigurationResolver()

    fun createFromSettings(): AiClientSelection {
        return when (val configuration = resolver.resolve(AiSettingsService.getInstance().getSnapshot())) {
            is ResolvedAiConfiguration.Mock -> AiClientSelection(
                client = MockAiClient(),
                statusMessage = configuration.statusMessage
            )

            is ResolvedAiConfiguration.OpenAiCompatible -> AiClientSelection(
                client = OpenAiCompatibleClient(
                    apiKey = configuration.apiKey,
                    baseUrl = configuration.baseUrl,
                    model = configuration.model
                ),
                statusMessage = configuration.statusMessage
            )
        }
    }

    fun createFromEnvironment(): AiClientSelection {
        val apiKey = System.getenv("OPENAI_API_KEY")
        if (apiKey.isNullOrBlank()) {
            return AiClientSelection(
                client = MockAiClient(),
                statusMessage = "OPENAI_API_KEY is not configured. Using mock readiness response."
            )
        }

        return AiClientSelection(
            client = OpenAiCompatibleClient(
                apiKey = apiKey,
                baseUrl = System.getenv("OPENAI_BASE_URL")?.takeIf { it.isNotBlank() }
                    ?: OpenAiCompatibleClient.DEFAULT_BASE_URL,
                model = System.getenv("OPENAI_MODEL")?.takeIf { it.isNotBlank() }
                    ?: OpenAiCompatibleClient.DEFAULT_MODEL
            ),
            statusMessage = null
        )
    }
}

data class AiClientSelection(
    val client: AiClient,
    val statusMessage: String?
)
