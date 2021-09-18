package uk.co.avsoftware.trading.client.binance

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.model.ApiKeyPermissions
import uk.co.avsoftware.trading.client.binance.model.CoinInfo
import uk.co.avsoftware.trading.client.binance.model.SystemStatus
import uk.co.avsoftware.trading.client.binance.sign.BinanceSigner

@Component
class WalletClient(@Qualifier("binanceApiClient") val webClient: WebClient, val binanceSigner: BinanceSigner) {

    fun getSystemStatus(): Mono<SystemStatus> =
        webClient.get().uri("/sapi/v1/system/status").accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(SystemStatus::class.java)

    fun getAllCoinsInfo(): Flux<CoinInfo> =
        with (binanceSigner){
            webClient.get().uri("/sapi/v1/capital/config/getall/?${signQueryString(getTimestampQueryString())}")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-MBX-APIKEY", getApiKey() )
                .retrieve()
                .bodyToFlux(CoinInfo::class.java)
        }

}