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
import uk.co.avsoftware.trading.client.bybit.config.BybitConfigProperties
import uk.co.avsoftware.trading.client.bybit.model.*
import uk.co.avsoftware.trading.client.bybit.sign.Encryption
import java.io.IOException
import java.time.Instant
import javax.annotation.PostConstruct

@Component
class BybitTradeClient(
    @Qualifier("bybitApiClient") val bybitClient: WebClient,
    val encryption: Encryption,
    val bybitConfigProperties: BybitConfigProperties
    ) {

    private val logger = KotlinLogging.logger {}

    fun placeOrder( symbol: String, side: OrderSide, quantity: Double): Mono<ActiveOrderResponse> =
        with (encryption){
            val order = ActiveOrderRequest(
                symbol = symbol,
                side = side,
                quantity = quantity,
                timestamp = Instant.now().toEpochMilli(),
                type = OrderType.MARKET
            )
            val queryString = genQueryString(order.getQueryParameters(bybitConfigProperties.key), bybitConfigProperties.secret )

            val uri = "/spot/v1/order?${queryString}"
            logger.debug {"PLACE ORDER $uri" }

            bybitClient.post().uri(uri)
                .accept(MediaType.ALL)
                .retrieve()
                .onStatus(
                    { it== HttpStatus.BAD_REQUEST },
                    { response -> response.bodyToMono(BybitError::class.java).map { error -> IOException(error) } }
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