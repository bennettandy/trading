package uk.co.avsoftware.trading.client.binance

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyExtractors
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.model.*
import uk.co.avsoftware.trading.client.binance.model.trade.Trade
import uk.co.avsoftware.trading.client.binance.request.BinanceRequest
import uk.co.avsoftware.trading.client.binance.request.NewOrderRequest
import uk.co.avsoftware.trading.client.binance.request.TradeListRequest
import uk.co.avsoftware.trading.client.binance.response.BinanceError
import uk.co.avsoftware.trading.client.binance.response.OrderResponse
import uk.co.avsoftware.trading.client.binance.sign.BinanceSigner
import uk.co.avsoftware.trading.repository.TradeRepository
import java.io.IOException

@Component
class SpotTradeClient(@Qualifier("binanceApiClient") val webClient: WebClient, val binanceSigner: BinanceSigner, val tradeRepository: TradeRepository) {

    private val logger = KotlinLogging.logger {}

    fun getAccountInformation(): Mono<AccountInfo> =
        with (binanceSigner){
            webClient.get().uri("/api/v3/account?${signQueryString(BinanceRequest().baseQueryString())}")
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
                .onStatus(
                    { it.is4xxClientError },
                    { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
                )
                .onStatus({ it.is5xxServerError }, { Mono.error( RuntimeException("Server is not responding"))})
                .bodyToFlux(Trade::class.java)
        }

    fun testNewOrder(newOrderRequest: NewOrderRequest): Mono<OrderResponse> =
        with (binanceSigner){
            val queryString = signQueryString(newOrderRequest.getQueryString())
            webClient.post().uri("/api/v3/order/test?${queryString}")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-MBX-APIKEY", getApiKey() )
                .retrieve()
                .onStatus(
                    { it==HttpStatus.BAD_REQUEST },
                    { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
                )
                .onStatus({ it.is5xxServerError }, { Mono.error( RuntimeException("Server is not responding"))})

                .bodyToMono(OrderResponse::class.java)

        }

    fun placeNewOrder(newOrderRequest: NewOrderRequest): Mono<String> =
        with (binanceSigner){
            val queryString = signQueryString(newOrderRequest.getQueryString())
            println("PLACE ORDER $queryString")
            webClient.post().uri("/api/v3/order?${queryString}")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-MBX-APIKEY", getApiKey() )
                .retrieve()
                .onStatus(
                    { it==HttpStatus.BAD_REQUEST },
                    { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
                )
                .onStatus({ it.is5xxServerError }, { Mono.error( RuntimeException("Server is not responding"))})
                .bodyToMono(OrderResponse::class.java)
                .flatMap { tradeRepository.saveOrderResponse(it) }
                .doOnSuccess{ logger.info("Saved Result: $it")}

        }
}