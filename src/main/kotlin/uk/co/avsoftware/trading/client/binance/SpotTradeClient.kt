package uk.co.avsoftware.trading.client.binance

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.model.*
import uk.co.avsoftware.trading.client.binance.parameters.BinanceRequest
import uk.co.avsoftware.trading.client.binance.parameters.TradeListRequest
import uk.co.avsoftware.trading.client.binance.sign.BinanceSigner

@Component
class SpotTradeClient(@Qualifier("binanceApiClient") val webClient: WebClient, val binanceSigner: BinanceSigner) {

    fun getAccountInformation(): Mono<AccountInfo> =
        with (binanceSigner){
            webClient.get().uri("/api/v3/account?${signQueryString(BinanceRequest().getQueryString())}")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-MBX-APIKEY", getApiKey() )
                .retrieve()
                .bodyToMono(AccountInfo::class.java)
        }

    fun getAccountTradeList(tradeListParameters: TradeListRequest): Flux<Trade> =
        with (binanceSigner){
            webClient.get().uri("/api/v3/myTrades?${signQueryString(tradeListParameters.getQueryString())}")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-MBX-APIKEY", getApiKey() )
                .retrieve()
                .bodyToFlux(Trade::class.java)
        }
}