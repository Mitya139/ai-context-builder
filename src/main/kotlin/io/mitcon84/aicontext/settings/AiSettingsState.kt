package io.mitcon84.aicontext.settings

data class AiSettingsState(
    var providerType: String = AiProviderType.OPENAI_COMPATIBLE.name,
    var baseUrl: String = DEFAULT_BASE_URL,
    var model: String = DEFAULT_MODEL,
    var mockOnly: Boolean = false
)

enum class AiProviderType {
    OPENAI_COMPATIBLE
}

const val DEFAULT_BASE_URL = "https://api.openai.com"
const val DEFAULT_MODEL = "gpt-4.1-mini"
