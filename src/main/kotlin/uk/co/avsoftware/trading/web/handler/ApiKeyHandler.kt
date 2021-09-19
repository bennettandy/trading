package uk.co.avsoftware.trading.web.handler

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.ApiKeyClient

@Component
class ApiKeyHandler(var apiKeyClient: ApiKeyClient) {

    fun getApiKeyPermissions(): Mono<ServerResponse> =
        apiKeyClient.getApiKeyPermissions()
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it))
            }
}