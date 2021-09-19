package uk.co.avsoftware.trading.web.handler

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.SpotTradeClient
import uk.co.avsoftware.trading.client.binance.parameters.TradeListRequest

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
}