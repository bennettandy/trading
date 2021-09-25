package uk.co.avsoftware.trading.client.binance

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.request.NewOrderRequest
import uk.co.avsoftware.trading.client.binance.response.BinanceError
import uk.co.avsoftware.trading.client.binance.response.OrderResponse
import uk.co.avsoftware.trading.client.binance.sign.BinanceSigner
import uk.co.avsoftware.trading.repository.TradeRepository
import java.io.IOException

@Component
class SpotTradeClient(@Qualifier("binanceApiClient") val webClient: WebClient, val binanceSigner: BinanceSigner, val tradeRepository: TradeRepository) {

    private val logger = KotlinLogging.logger {}

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