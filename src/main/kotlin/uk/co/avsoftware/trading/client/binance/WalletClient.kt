package uk.co.avsoftware.trading.client.binance

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.model.SystemStatus

@Component
class WalletClient(@Qualifier("binanceApiClient") val webClient: WebClient) {

    fun getSystemStatus(): Mono<SystemStatus> =
        webClient.get().uri("/sapi/v1/system/status").accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(SystemStatus::class.java)

}