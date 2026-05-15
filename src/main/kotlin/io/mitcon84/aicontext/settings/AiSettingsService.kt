package io.mitcon84.aicontext.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(
    name = "AiContextBuilderSettings",
    storages = [Storage("aiContextBuilder.xml")]
)
class AiSettingsService : PersistentStateComponent<AiSettingsState> {
    private var state = AiSettingsState()

    override fun getState(): AiSettingsState = state

    override fun loadState(state: AiSettingsState) {
        this.state = state
    }

    fun getSnapshot(apiKey: String? = ApiKeyStore.getApiKey()): AiSettingsSnapshot =
        AiSettingsSnapshot(
            providerType = state.providerType,
            baseUrl = state.baseUrl,
            model = state.model,
            mockOnly = state.mockOnly,
            apiKey = apiKey
        )

    companion object {
        fun getInstance(): AiSettingsService =
            ApplicationManager.getApplication().getService(AiSettingsService::class.java)
    }
}

data class AiSettingsSnapshot(
    val providerType: String,
    val baseUrl: String,
    val model: String,
    val mockOnly: Boolean,
    val apiKey: String?
)
