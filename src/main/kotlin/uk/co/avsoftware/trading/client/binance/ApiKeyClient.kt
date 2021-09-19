package uk.co.avsoftware.trading.client.binance

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.model.ApiKeyPermissions
import uk.co.avsoftware.trading.client.binance.request.BinanceRequest
import uk.co.avsoftware.trading.client.binance.sign.BinanceSigner

@Component
class ApiKeyClient(@Qualifier("binanceApiClient") val webClient: WebClient, val binanceSigner: BinanceSigner) {

    fun getApiKeyPermissions(): Mono<ApiKeyPermissions> =
        with (binanceSigner){
            webClient.get().uri("/sapi/v1/account/apiRestrictions/?${signQueryString(BinanceRequest().baseQueryString())}")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-MBX-APIKEY", getApiKey() )
                .retrieve()
                .bodyToMono(ApiKeyPermissions::class.java)
        }
}