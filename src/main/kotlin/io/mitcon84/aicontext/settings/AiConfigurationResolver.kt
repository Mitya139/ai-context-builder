package io.mitcon84.aicontext.settings

class AiConfigurationResolver {
    fun resolve(
        settings: AiSettingsSnapshot,
        environment: Map<String, String?> = System.getenv()
    ): ResolvedAiConfiguration {
        if (settings.mockOnly) {
            return ResolvedAiConfiguration.Mock("Mock AI client is enabled in Settings.")
        }

        val settingsApiKey = settings.apiKey?.takeIf { it.isNotBlank() }
        if (settingsApiKey != null) {
            return ResolvedAiConfiguration.OpenAiCompatible(
                apiKey = settingsApiKey,
                baseUrl = settings.baseUrl.ifBlank { DEFAULT_BASE_URL },
                model = settings.model.ifBlank { DEFAULT_MODEL },
                statusMessage = null
            )
        }

        val envApiKey = environment["OPENAI_API_KEY"]?.takeIf { it.isNotBlank() }
        if (envApiKey != null) {
            return ResolvedAiConfiguration.OpenAiCompatible(
                apiKey = envApiKey,
                baseUrl = settings.baseUrl.ifBlank {
                    environment["OPENAI_BASE_URL"]?.takeIf { it.isNotBlank() } ?: DEFAULT_BASE_URL
                },
                model = settings.model.ifBlank {
                    environment["OPENAI_MODEL"]?.takeIf { it.isNotBlank() } ?: DEFAULT_MODEL
                },
                statusMessage = "Using OPENAI_API_KEY from environment because no API key is configured in Settings."
            )
        }

        return ResolvedAiConfiguration.Mock("API key is not configured in Settings. Using mock readiness response.")
    }
}

sealed class ResolvedAiConfiguration {
    data class Mock(val statusMessage: String) : ResolvedAiConfiguration()

    data class OpenAiCompatible(
        val apiKey: String,
        val baseUrl: String,
        val model: String,
        val statusMessage: String?
    ) : ResolvedAiConfiguration()
}
