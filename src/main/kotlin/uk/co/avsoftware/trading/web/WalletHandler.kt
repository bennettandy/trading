package uk.co.avsoftware.trading.web

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.WalletClient

@Component
class WalletHandler(var walletClient: WalletClient) {

    fun systemStatus(): Mono<ServerResponse> =
        walletClient.getSystemStatus()
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it.toString()))
            }

    fun getAllCoinsInfo(): Mono<ServerResponse> =
        walletClient.getAllCoinsInfo()
            .collectList()
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it.toString()))
            }

    fun getDustLog(): Mono<ServerResponse> =
        walletClient.getDustLog()
            .flatMap {
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(it.toString()))
            }
}