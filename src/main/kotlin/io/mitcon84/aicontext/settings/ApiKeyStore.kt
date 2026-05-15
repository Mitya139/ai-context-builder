package io.mitcon84.aicontext.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe

object ApiKeyStore {
    private const val SERVICE_NAME = "AI Context Builder OpenAI API Key"

    fun getApiKey(): String? =
        PasswordSafe.instance.getPassword(credentialAttributes())

    fun setApiKey(apiKey: String) {
        val normalized = apiKey.trim()
        val credentials = if (normalized.isBlank()) null else Credentials("openai-compatible", normalized)
        PasswordSafe.instance.set(credentialAttributes(), credentials)
    }

    private fun credentialAttributes(): CredentialAttributes =
        CredentialAttributes(SERVICE_NAME)
}
