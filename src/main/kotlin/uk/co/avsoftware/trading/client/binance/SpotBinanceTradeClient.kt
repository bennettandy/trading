package uk.co.avsoftware.trading.client.binance

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.model.OrderSide
import uk.co.avsoftware.trading.client.binance.model.NewOrderRequest
import uk.co.avsoftware.trading.client.binance.model.BinanceError
import uk.co.avsoftware.trading.client.binance.model.OrderFill
import uk.co.avsoftware.trading.client.binance.model.OrderResponse
import uk.co.avsoftware.trading.client.binance.sign.BinanceSigner
import java.io.IOException
import javax.annotation.PostConstruct

interface BinanceTradeClient {
    fun placeNewOrder(newOrderRequest: NewOrderRequest): Mono<OrderResponse>
}

@Component
@Profile("test")
class DummyBinanceTradeClient : BinanceTradeClient {
    private val logger = KotlinLogging.logger {}

    override fun placeNewOrder(newOrderRequest: NewOrderRequest): Mono<OrderResponse> {
        logger.warn { "Placing Dummy Trade: ${newOrderRequest.side}: ${newOrderRequest.quantity}" }
        fun createOrderResponse(quantity: Double, orderSide: OrderSide, clientOrderId: String): OrderResponse {
            return OrderResponse(
                fills = listOf(
                    OrderFill(
                        price = 10.0,
                        qty = quantity,
                        commission = 0.0023,
                        commissionAsset = "BNB"
                    )
                ),
                symbol = "SOLBTC",
                side = orderSide,
                clientOrderId = clientOrderId,
                orderId = (5000L..10000L).random(),
                transactTime = System.currentTimeMillis()
            )
        }
        return Mono.just(createOrderResponse(newOrderRequest.quantity?.toDouble() ?: 0.0, newOrderRequest.side, newOrderRequest.newClientOrderId ?: ""))
    }

    @PostConstruct
    fun postConstruct() = logger.info("--- Dummy Trade Client ---")
}

@Component
@Profile("production")
class SpotBinanceTradeClient(
    @Qualifier("binanceApiClient") val binanceWebClient: WebClient,
    val binanceSigner: BinanceSigner
    ): BinanceTradeClient {

    private val logger = KotlinLogging.logger {}

    override fun placeNewOrder(newOrderRequest: NewOrderRequest): Mono<OrderResponse> =
        with (binanceSigner){
            val queryString = signQueryString(newOrderRequest.getQueryString())
            logger.debug {"PLACE ORDER $queryString" }
            binanceWebClient.post().uri("/api/v3/order?${queryString}")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-MBX-APIKEY", getApiKey() )
                .retrieve()
                .onStatus(
                    { it==HttpStatus.BAD_REQUEST },
                    { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
                )
                .onStatus({ it.is5xxServerError }, { Mono.error( RuntimeException("Server is not responding"))})
                .bodyToMono(OrderResponse::class.java)
                .doOnSuccess { logger.info("Place Trade [SUCCESS]")}
                .doOnError { logger.error { "Failed to Place Trade ${it.message}"} }
        }

    @PostConstruct
    fun postConstruct() = logger.info("*** USING LIVE TRADE CLIENT ***")
}