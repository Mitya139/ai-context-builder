package io.mitcon84.aicontext.ai

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class OpenAiCompatibleClient(
    private val apiKey: String,
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val model: String = DEFAULT_MODEL,
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(20))
        .build()
) : AiClient {
    override fun complete(prompt: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${baseUrl.trimEnd('/')}/v1/chat/completions"))
            .timeout(Duration.ofSeconds(60))
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(prompt)))
            .build()

        val response = try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (exception: Exception) {
            throw AiClientException("Network error while running AI readiness check: ${exception.message}", exception)
        }

        if (response.statusCode() !in 200..299) {
            throw AiClientException("HTTP ${response.statusCode()} from AI provider. Check OPENAI_API_KEY and OPENAI_BASE_URL.")
        }

        return extractContent(response.body())
            ?: throw AiClientException("AI provider returned an invalid or empty response.")
    }

    private fun buildRequestBody(prompt: String): String {
        return """
            {
              "model": "${escapeJson(model)}",
              "messages": [
                {
                  "role": "system",
                  "content": "You evaluate IDE context quality for coding tasks. Return concise Markdown only."
                },
                {
                  "role": "user",
                  "content": "${escapeJson(prompt)}"
                }
              ],
              "temperature": 0.2
            }
        """.trimIndent()
    }

    private fun extractContent(json: String): String? {
        val keyIndex = json.indexOf("\"content\"")
        if (keyIndex < 0) {
            return null
        }

        val colonIndex = json.indexOf(':', keyIndex)
        if (colonIndex < 0) {
            return null
        }

        val startQuote = json.indexOf('"', colonIndex + 1)
        if (startQuote < 0) {
            return null
        }

        val result = StringBuilder()
        var index = startQuote + 1
        var escaping = false
        while (index < json.length) {
            val char = json[index]
            if (escaping) {
                when (char) {
                    '"' -> result.append('"')
                    '\\' -> result.append('\\')
                    '/' -> result.append('/')
                    'b' -> result.append('\b')
                    'f' -> result.append('\u000C')
                    'n' -> result.append('\n')
                    'r' -> result.append('\r')
                    't' -> result.append('\t')
                    'u' -> {
                        val hex = json.substring(index + 1, (index + 5).coerceAtMost(json.length))
                        if (hex.length == 4) {
                            result.append(hex.toInt(16).toChar())
                            index += 4
                        }
                    }
                    else -> result.append(char)
                }
                escaping = false
            } else if (char == '\\') {
                escaping = true
            } else if (char == '"') {
                return result.toString().takeIf { it.isNotBlank() }
            } else {
                result.append(char)
            }
            index++
        }

        return null
    }

    private fun escapeJson(value: String): String {
        return buildString {
            value.forEach { char ->
                when (char) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
        }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://api.openai.com"
        const val DEFAULT_MODEL = "gpt-4.1-mini"
    }
}
