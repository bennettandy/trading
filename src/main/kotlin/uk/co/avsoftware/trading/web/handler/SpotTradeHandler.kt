package uk.co.avsoftware.trading.web.handler

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.json
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.onErrorReturn
import reactor.kotlin.core.publisher.toMono
import uk.co.avsoftware.trading.client.binance.SpotTradeClient
import uk.co.avsoftware.trading.client.binance.request.NewOrderRequest
import uk.co.avsoftware.trading.client.binance.request.TradeListRequest
import java.io.IOException

@Component
class SpotTradeHandler(var tradeClient: SpotTradeClient) {

    fun getAccountInformation(): Mono<ServerResponse> =
        tradeClient.getAccountInformation()
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it))
            }

    fun getAccountTradeList(tradeListRequest: TradeListRequest): Mono<ServerResponse> =
        tradeClient.getAccountTradeList(tradeListRequest)
            .collectList()
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it))
            }

    fun testNewOrder(newOrderRequest: NewOrderRequest): Mono<ServerResponse> =
        tradeClient.testNewOrder(newOrderRequest)
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it))
            }
            .onErrorResume { error -> ServerResponse.badRequest()
                .bodyValue(error.message ?: "null")
            }

}