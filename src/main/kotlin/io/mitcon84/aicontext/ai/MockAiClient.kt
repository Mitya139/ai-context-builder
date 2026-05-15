package io.mitcon84.aicontext.ai

class MockAiClient : AiClient {
    override fun complete(prompt: String): String {
        return """
            # Context Readiness Report

            ## Status
            Partial

            ## Short Explanation
            The selected context appears relevant, but this is a mock response. Configure an OpenAI-compatible API key to run a real readiness check.

            ## Useful Provided Context
            - The selected snippets were included in the readiness prompt.

            ## Candidate Missing Context
            - Check the project outline for related action, service, UI, prompt, and test files.

            ## Suggested Next Step
            Configure an API key or copy the generated prompt into an external AI assistant.
        """.trimIndent()
    }
}
