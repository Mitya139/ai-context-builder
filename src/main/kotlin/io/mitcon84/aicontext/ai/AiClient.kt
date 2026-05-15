package io.mitcon84.aicontext.ai

interface AiClient {
    fun complete(prompt: String): String
}
