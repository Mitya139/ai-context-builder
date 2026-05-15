package io.mitcon84.aicontext.ai

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

internal class JdkAiHttpTransport(
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(20))
        .build()
) : AiHttpTransport {
    override fun postJson(uri: URI, apiKey: String, body: String, timeout: Duration): AiHttpResponse {
        val request = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(timeout)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return AiHttpResponse(
            statusCode = response.statusCode(),
            body = response.body()
        )
    }
}
