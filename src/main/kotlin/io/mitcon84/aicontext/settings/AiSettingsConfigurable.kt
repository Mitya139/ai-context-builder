package io.mitcon84.aicontext.settings

import com.intellij.openapi.options.Configurable
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JTextField

class AiSettingsConfigurable : Configurable {
    private var panel: JPanel? = null
    private var providerComboBox: JComboBox<String>? = null
    private var baseUrlField: JTextField? = null
    private var modelField: JTextField? = null
    private var apiKeyField: JPasswordField? = null
    private var mockOnlyCheckBox: JCheckBox? = null

    override fun getDisplayName(): String = "AI Context Builder"

    override fun createComponent(): JComponent {
        val provider = JComboBox(arrayOf("OpenAI-compatible"))
        val baseUrl = JTextField(30)
        val model = JTextField(30)
        val apiKey = JPasswordField(30)
        val mockOnly = JCheckBox("Use mock AI client only")

        providerComboBox = provider
        baseUrlField = baseUrl
        modelField = model
        apiKeyField = apiKey
        mockOnlyCheckBox = mockOnly

        panel = JPanel(BorderLayout()).apply {
            add(
                JPanel(GridBagLayout()).apply {
                    addRow(0, "Provider:", provider)
                    addRow(1, "Base URL:", baseUrl)
                    addRow(2, "Model:", model)
                    addRow(3, "API Key:", apiKey)
                    addRow(4, "", mockOnly)
                },
                BorderLayout.NORTH
            )
        }

        reset()
        return panel!!
    }

    override fun isModified(): Boolean {
        val state = AiSettingsService.getInstance().state
        return baseUrlField?.text.orEmpty() != state.baseUrl ||
            modelField?.text.orEmpty() != state.model ||
            mockOnlyCheckBox?.isSelected != state.mockOnly ||
            String(apiKeyField?.password ?: CharArray(0)) != ApiKeyStore.getApiKey().orEmpty()
    }

    override fun apply() {
        val state = AiSettingsService.getInstance().state
        state.providerType = AiProviderType.OPENAI_COMPATIBLE.name
        state.baseUrl = baseUrlField?.text?.trim().orEmpty().ifBlank { DEFAULT_BASE_URL }
        state.model = modelField?.text?.trim().orEmpty().ifBlank { DEFAULT_MODEL }
        state.mockOnly = mockOnlyCheckBox?.isSelected == true
        ApiKeyStore.setApiKey(String(apiKeyField?.password ?: CharArray(0)))
    }

    override fun reset() {
        val state = AiSettingsService.getInstance().state
        providerComboBox?.selectedIndex = 0
        baseUrlField?.text = state.baseUrl.ifBlank { DEFAULT_BASE_URL }
        modelField?.text = state.model.ifBlank { DEFAULT_MODEL }
        mockOnlyCheckBox?.isSelected = state.mockOnly
        apiKeyField?.text = ApiKeyStore.getApiKey().orEmpty()
    }

    override fun disposeUIResources() {
        panel = null
        providerComboBox = null
        baseUrlField = null
        modelField = null
        apiKeyField = null
        mockOnlyCheckBox = null
    }

    private fun JPanel.addRow(row: Int, label: String, component: JComponent) {
        val labelConstraints = GridBagConstraints().apply {
            gridx = 0
            gridy = row
            anchor = GridBagConstraints.WEST
            insets.set(4, 4, 4, 8)
        }
        val fieldConstraints = GridBagConstraints().apply {
            gridx = 1
            gridy = row
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
            insets.set(4, 4, 4, 4)
        }
        add(JLabel(label), labelConstraints)
        add(component, fieldConstraints)
    }
}
