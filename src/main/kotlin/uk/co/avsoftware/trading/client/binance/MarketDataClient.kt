package uk.co.avsoftware.trading.client.binance

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.response.ServerTimeResponse
import uk.co.avsoftware.trading.client.binance.sign.BinanceSigner

@Component
class MarketDataClient(@Qualifier("binanceApiClient") val webClient: WebClient, val binanceSigner: BinanceSigner) {

    fun pingServer(): Mono<Unit> =
            webClient.get().uri("/api/v3/ping")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Unit::class.java)

    fun getServerTime(): Mono<ServerTimeResponse> =
        webClient.get().uri("/api/v3/time")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(ServerTimeResponse::class.java)
}