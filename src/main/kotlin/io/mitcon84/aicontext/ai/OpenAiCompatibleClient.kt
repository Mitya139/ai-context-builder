package io.mitcon84.aicontext.ai

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.net.URI
import java.time.Duration

internal class OpenAiCompatibleClient(
    private val apiKey: String,
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val model: String = DEFAULT_MODEL,
    private val gson: Gson = Gson(),
    private val transport: AiHttpTransport = JdkAiHttpTransport()
) : AiClient {
    override fun complete(prompt: String): String {
        val requestBody = buildRequestBody(prompt)

        val response = try {
            transport.postJson(
                uri = URI.create("${baseUrl.trimEnd('/')}/v1/chat/completions"),
                apiKey = apiKey,
                body = requestBody,
                timeout = Duration.ofSeconds(60)
            )
        } catch (exception: Exception) {
            throw AiClientException("Network error while running AI readiness check: ${exception.message}", exception)
        }

        if (response.statusCode !in 200..299) {
            throw AiClientException("HTTP ${response.statusCode} from AI provider. Check OPENAI_API_KEY and OPENAI_BASE_URL.")
        }

        return parseContent(response.body)
            ?: throw AiClientException("AI provider returned an invalid or empty response.")
    }

    private fun buildRequestBody(prompt: String): String {
        return gson.toJson(
            ChatCompletionRequest(
                model = model,
                messages = listOf(
                    ChatMessage(
                        role = "system",
                        content = "You evaluate IDE context quality for coding tasks. Return concise Markdown only."
                    ),
                    ChatMessage(
                        role = "user",
                        content = prompt
                    )
                ),
                temperature = 0.2
            )
        )
    }

    private fun parseContent(json: String): String? {
        val response = try {
            gson.fromJson(json, ChatCompletionResponse::class.java)
        } catch (exception: JsonSyntaxException) {
            throw AiClientException("AI provider returned malformed JSON.", exception)
        }
        return response.choices.orEmpty()
            .asSequence()
            .mapNotNull { it.message?.content?.takeIf(String::isNotBlank) }
            .firstOrNull()
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://api.openai.com"
        const val DEFAULT_MODEL = "gpt-4.1-mini"
    }
}

private data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double
)

private data class ChatMessage(
    val role: String,
    val content: String
)

private data class ChatCompletionResponse(
    val choices: List<ChatChoice>? = null
)

private data class ChatChoice(
    val message: ChatResponseMessage? = null
)

private data class ChatResponseMessage(
    val content: String? = null
)
