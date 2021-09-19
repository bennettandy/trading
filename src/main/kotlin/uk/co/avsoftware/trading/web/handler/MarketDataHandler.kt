package uk.co.avsoftware.trading.web.handler

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.MarketDataClient

@Component
class MarketDataHandler(var marketDataClient: MarketDataClient) {

    fun pingServer(): Mono<ServerResponse> =
        marketDataClient.pingServer()
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue("Pong"))
            }

    fun getServerTime(): Mono<ServerResponse> =
        marketDataClient.getServerTime()
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it))
            }
}