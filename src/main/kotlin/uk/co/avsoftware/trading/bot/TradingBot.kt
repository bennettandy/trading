package uk.co.avsoftware.trading.bot

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.SpotTradeClient
import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.client.binance.model.trade.OrderType
import uk.co.avsoftware.trading.client.binance.request.NewOrderRequest
import uk.co.avsoftware.trading.client.binance.response.OrderResponse

@Component
class TradingBot( val tradeClient: SpotTradeClient) {

    private val logger = KotlinLogging.logger {}

    var isLong: Boolean = false
    var isShort: Boolean = false


    fun longTrigger(): Mono<ServerResponse> {
        logger.info("LONG TRIGGER : S $isShort, L $isLong")

        val closeShort: Mono<OrderResponse> =
            if (isShort){
                // close any short
                tradeClient.placeNewOrder(longRequest())
                    .doOnSuccess {
                        isShort = false
                        logger.info("Close Short Success")
                    }
            } else Mono.empty()

        val result: Mono<ServerResponse> = if (!isLong) {
            tradeClient.placeNewOrder(longRequest())
                .flatMap { closeShort }
                .flatMap { ServerResponse.ok().build() }
                .doOnSuccess {
                    isLong = true
                    logger.info("Open Long Success")
                }
                .onErrorResume {
                    logger.error("ERROR: $it")
                    ServerResponse.notFound().build()
                }
        } else { ServerResponse.notFound().build() }
        return result
    }

    fun longTakeProfit(): Mono<ServerResponse> {
        logger.info("LONG TP : S $isShort, L $isLong")
        val result: Mono<ServerResponse> = if (isLong) {
            tradeClient.placeNewOrder(shortRequest())
                .flatMap { ServerResponse.ok().build() }
                .doOnSuccess { isLong = false }
                .onErrorResume { ServerResponse.notFound().build() }
        } else { ServerResponse.notFound().build() }
        return result
    }

    fun shortTrigger(): Mono<ServerResponse> {
        logger.info("SHORT TRIGGER : S $isShort, L $isLong" )

        val closeLong: Mono<OrderResponse> =
        if (isLong) {
            // close any long
            tradeClient.placeNewOrder(shortRequest())
                .doOnSuccess {
                    isLong = false
                    logger.info("Close Long Success")
                }
        } else Mono.empty()

        val result: Mono<ServerResponse> = if (!isShort) {
            // not short so place short to open position
            tradeClient.placeNewOrder(shortRequest())
                .flatMap { closeLong } // close long if we have one
                .flatMap { ServerResponse.ok().build() }
                .doOnSuccess { isShort = true
                    logger.info("Open Short Success")
                }
                .onErrorResume {
                    logger.error("ERROR", it)
                    ServerResponse.notFound().build() }
        } else { ServerResponse.notFound().build() }
        return result
    }

    fun shortTakeProfit(): Mono<ServerResponse> {
        logger.info("SHORT TP : S $isShort, L $isLong")

        val result: Mono<ServerResponse> = if (isShort) {
            // we are short - place long order to TP
            tradeClient.placeNewOrder(longRequest())
                .flatMap { ServerResponse.ok().build() }
                .doOnSuccess { isShort = false }
                .onErrorResume { ServerResponse.notFound().build() }

        } else { ServerResponse.notFound().build() }

        return result
    }

    fun bullish(): Mono<ServerResponse> {
        logger.info("BULLISH")
        return ServerResponse.ok().build()
    }

    fun bearish(): Mono<ServerResponse> {
        logger.info("BEARISH")
        return ServerResponse.ok().build()
    }

    private fun placeLong(): Mono<OrderResponse> =
            tradeClient.placeNewOrder(longRequest())
                .doOnSuccess { isLong = true }

    private fun placeShort(): Mono<OrderResponse> =
        tradeClient.placeNewOrder(shortRequest())
            .doOnSuccess { isShort = true }

    private fun longRequest() =
        NewOrderRequest(
            symbol = "SOLBTC",
            side = OrderSide.BUY,
            type = OrderType.MARKET,
            quantity = "5.0"
        )

    private fun shortRequest() =
        NewOrderRequest(
            symbol = "SOLBTC",
            side = OrderSide.SELL,
            type = OrderType.MARKET,
            quantity = "5.0"
        )
}