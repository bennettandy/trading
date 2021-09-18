package uk.co.avsoftware.trading.client.binance

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.api.config.BinanceConfigProperties
import uk.co.avsoftware.trading.client.binance.model.ApiKeyPermissions

@Component
class ApiKeyClient(@Qualifier("binanceApiClient") val webClient: WebClient, val clientProperties: BinanceConfigProperties, val signature: Signature) {

    fun getApiKeyPermissions(): Mono<ApiKeyPermissions> =
        webClient.get().uri("/sapi/v1/account/apiRestrictions/?${queryString()}")
            .accept(MediaType.APPLICATION_JSON)
            .header("X-MBX-APIKEY", clientProperties.key )
            .retrieve()
            .bodyToMono(ApiKeyPermissions::class.java)

    fun queryString(): String {
        val queryPath = "timestamp=${System.currentTimeMillis()}"
        val sig = sign(queryPath)
        return "${queryPath}&signature=" + sig;
    }

    fun sign(message: String): String = signature.getSignature(message, clientProperties.secret)



}