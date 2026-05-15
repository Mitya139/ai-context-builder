package io.mitcon84.aicontext

import io.mitcon84.aicontext.settings.AiConfigurationResolver
import io.mitcon84.aicontext.settings.AiProviderType
import io.mitcon84.aicontext.settings.AiSettingsSnapshot
import io.mitcon84.aicontext.settings.DEFAULT_BASE_URL
import io.mitcon84.aicontext.settings.DEFAULT_MODEL
import io.mitcon84.aicontext.settings.ResolvedAiConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiConfigurationResolverTest {
    private val resolver = AiConfigurationResolver()

    @Test
    fun `settings values override environment values`() {
        val resolved = resolver.resolve(
            settings = snapshot(
                baseUrl = "https://settings.example.com",
                model = "settings-model",
                apiKey = "settings-key"
            ),
            environment = mapOf(
                "OPENAI_API_KEY" to "env-key",
                "OPENAI_BASE_URL" to "https://env.example.com",
                "OPENAI_MODEL" to "env-model"
            )
        )

        assertTrue(resolved is ResolvedAiConfiguration.OpenAiCompatible)
        resolved as ResolvedAiConfiguration.OpenAiCompatible
        assertEquals("settings-key", resolved.apiKey)
        assertEquals("https://settings.example.com", resolved.baseUrl)
        assertEquals("settings-model", resolved.model)
    }

    @Test
    fun `mock fallback is used when API key is missing`() {
        val resolved = resolver.resolve(
            settings = snapshot(apiKey = null),
            environment = emptyMap()
        )

        assertTrue(resolved is ResolvedAiConfiguration.Mock)
    }

    @Test
    fun `default base URL and model are applied when settings are blank`() {
        val resolved = resolver.resolve(
            settings = snapshot(baseUrl = "", model = "", apiKey = "settings-key"),
            environment = emptyMap()
        )

        assertTrue(resolved is ResolvedAiConfiguration.OpenAiCompatible)
        resolved as ResolvedAiConfiguration.OpenAiCompatible
        assertEquals(DEFAULT_BASE_URL, resolved.baseUrl)
        assertEquals(DEFAULT_MODEL, resolved.model)
    }

    private fun snapshot(
        baseUrl: String = DEFAULT_BASE_URL,
        model: String = DEFAULT_MODEL,
        apiKey: String? = "settings-key",
        mockOnly: Boolean = false
    ): AiSettingsSnapshot =
        AiSettingsSnapshot(
            providerType = AiProviderType.OPENAI_COMPATIBLE.name,
            baseUrl = baseUrl,
            model = model,
            mockOnly = mockOnly,
            apiKey = apiKey
        )
}
