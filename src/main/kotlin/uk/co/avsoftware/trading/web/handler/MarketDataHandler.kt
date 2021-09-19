package uk.co.avsoftware.trading.web.handler

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.MarketDataClient
import uk.co.avsoftware.trading.client.binance.request.OrderBookRequest

@Component
class MarketDataHandler(var marketDataClient: MarketDataClient) {

    fun pingServer(): Mono<ServerResponse> =
        marketDataClient.pingServer()
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue("Pong"))
            }
            .onErrorResume { error -> ServerResponse.badRequest()
                .bodyValue(error.message ?: "null")
            }

    fun getServerTime(): Mono<ServerResponse> =
        marketDataClient.getServerTime()
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it))
            }
            .onErrorResume { error -> ServerResponse.badRequest()
                .bodyValue(error.message ?: "null")
            }

    fun getOrderBookDepth(orderBookRequest: OrderBookRequest): Mono<ServerResponse> =
        marketDataClient.getOrderBookDepth(orderBookRequest)
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it))
            }
            .onErrorResume { error -> ServerResponse.badRequest()
                .bodyValue(error.message ?: "null")
            }

    fun getRecentTrades(orderBookRequest: OrderBookRequest): Mono<ServerResponse> =
        marketDataClient.getRecentTrades(orderBookRequest)
            .collectList()
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it))
            }
            .onErrorResume { error -> ServerResponse.badRequest()
                .bodyValue(error.message ?: "null")
            }
}