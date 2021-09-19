package uk.co.avsoftware.trading.web.handler

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.WalletClient
import uk.co.avsoftware.trading.client.binance.request.TradeFeesRequest

@Component
class WalletHandler(var walletClient: WalletClient) {

    fun systemStatus(): Mono<ServerResponse> =
        walletClient.getSystemStatus()
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it))
            }

    fun getAllCoinsInfo(): Mono<ServerResponse> =
        walletClient.getAllCoinsInfo()
            .collectList()
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it))
            }

    fun getDustLog(): Mono<ServerResponse> =
        walletClient.getDustLog()
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it))
            }

    fun getTradeFees(request: TradeFeesRequest): Mono<ServerResponse> =
        walletClient.getTradeFees(request)
            .collectList()
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it))
            }
}