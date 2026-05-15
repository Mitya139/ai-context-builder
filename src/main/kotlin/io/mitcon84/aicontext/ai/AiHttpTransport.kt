package io.mitcon84.aicontext.ai

import java.net.URI
import java.time.Duration

internal interface AiHttpTransport {
    fun postJson(uri: URI, apiKey: String, body: String, timeout: Duration): AiHttpResponse
}

internal data class AiHttpResponse(
    val statusCode: Int,
    val body: String
)
