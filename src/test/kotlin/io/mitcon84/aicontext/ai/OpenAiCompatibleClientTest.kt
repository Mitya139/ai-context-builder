package io.mitcon84.aicontext.ai

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI
import java.time.Duration

class OpenAiCompatibleClientTest {
    @Test
    fun `sends serialized chat completion request and parses response content`() {
        val transport = FakeTransport(
            response = AiHttpResponse(
                statusCode = 200,
                body = """
                    {
                      "choices": [
                        {
                          "message": {
                            "content": "Ready\nLooks good."
                          }
                        }
                      ]
                    }
                """.trimIndent()
            )
        )
        val client = OpenAiCompatibleClient(
            apiKey = "test-key",
            baseUrl = "https://example.test/",
            model = "test-model",
            transport = transport
        )

        val result = client.complete("Check \"quoted\" prompt\nwith newline.")

        assertEquals("Ready\nLooks good.", result)
        assertEquals(URI.create("https://example.test/v1/chat/completions"), transport.uri)
        assertEquals("test-key", transport.apiKey)
        assertEquals(Duration.ofSeconds(60), transport.timeout)

        val requestJson = JsonParser.parseString(transport.body).asJsonObject
        assertEquals("test-model", requestJson.get("model").asString)
        val messages = requestJson.getAsJsonArray("messages")
        assertEquals("system", messages[0].asJsonObject.get("role").asString)
        assertEquals("user", messages[1].asJsonObject.get("role").asString)
        assertEquals("Check \"quoted\" prompt\nwith newline.", messages[1].asJsonObject.get("content").asString)
    }

    @Test
    fun `throws client exception for non success status`() {
        val client = OpenAiCompatibleClient(
            apiKey = "test-key",
            transport = FakeTransport(response = AiHttpResponse(statusCode = 401, body = "{}"))
        )

        val exception = assertThrowsAiClientException {
            client.complete("prompt")
        }

        assertTrue(exception.message.orEmpty().contains("HTTP 401"))
    }

    @Test
    fun `throws client exception for malformed json response`() {
        val client = OpenAiCompatibleClient(
            apiKey = "test-key",
            transport = FakeTransport(response = AiHttpResponse(statusCode = 200, body = "{invalid"))
        )

        val exception = assertThrowsAiClientException {
            client.complete("prompt")
        }

        assertTrue(exception.message.orEmpty().contains("malformed JSON"))
    }

    private fun assertThrowsAiClientException(block: () -> Unit): AiClientException {
        return try {
            block()
            throw AssertionError("Expected AiClientException")
        } catch (exception: AiClientException) {
            exception
        }
    }

    private class FakeTransport(
        private val response: AiHttpResponse
    ) : AiHttpTransport {
        lateinit var uri: URI
        lateinit var apiKey: String
        lateinit var body: String
        lateinit var timeout: Duration

        override fun postJson(uri: URI, apiKey: String, body: String, timeout: Duration): AiHttpResponse {
            this.uri = uri
            this.apiKey = apiKey
            this.body = body
            this.timeout = timeout
            return response
        }
    }
}
