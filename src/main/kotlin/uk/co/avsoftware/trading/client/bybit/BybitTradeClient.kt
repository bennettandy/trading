package uk.co.avsoftware.trading.client.bybit

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.model.BinanceError
import uk.co.avsoftware.trading.client.binance.sign.BybitSigner
import uk.co.avsoftware.trading.client.bybit.model.*
import java.io.IOException
import java.time.Instant
import javax.annotation.PostConstruct

@Component
class BybitTradeClient(
    @Qualifier("bybitApiClient") val bybitClient: WebClient,
    val bybitSigner: BybitSigner,
    ) {

    private val logger = KotlinLogging.logger {}

    fun placeOrder( symbol: String, side: OrderSide, quantity: Double): Mono<ActiveOrderResponse> =
        with (bybitSigner){
            val order = ActiveOrderRequest(
                symbol = symbol,
                side = side,
                quantity = quantity,
                timestamp = Instant.now().toEpochMilli(),
                type = OrderType.Market
            )
            val queryString = signQueryString(order.getQueryString(getApiKey()))

            logger.debug {"PLACE ORDER $queryString" }

            bybitClient.post().uri("/spot/v1/order?${queryString}")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                    { it== HttpStatus.BAD_REQUEST },
                    { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
                )
                .onStatus({ it.is5xxServerError }, { Mono.error( RuntimeException("Server is not responding"))})
                .bodyToMono(ActiveOrderResponse::class.java)
                .doOnSuccess { logger.info("Place Trade [SUCCESS]")}
                .doOnError { logger.error { "Failed to Place Trade ${it.message}"} }

        }

    @PostConstruct
    fun postConstruct() = logger.info("*** CONSTRUCTED BYBIT TRADE CLIENT ***")
}


// print(client.Order.Order_new(side="Buy",symbol="BTCUSD",order_type="Market",qty=1,time_in_force="GoodTillCancel").result())