package uk.co.avsoftware.trading.bot

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.SpotTradeClient
import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.client.binance.model.trade.OrderType
import uk.co.avsoftware.trading.client.binance.request.NewOrderRequest

@Component
class TradingBot( val tradeClient: SpotTradeClient) {

    var isLong: Boolean = false
    var isShort: Boolean = false


    fun longTrigger(): Mono<ServerResponse> {
        println("LONG TRIGGER : S $isShort, L $isLong")
        val result: Mono<ServerResponse> = if (!isLong) {
            tradeClient.placeNewOrder(longRequest())
                .flatMap { ServerResponse.ok().build() }
                .doOnSuccess { isLong = true }
                .onErrorResume { ServerResponse.notFound().build() }
        } else { ServerResponse.notFound().build() }
        return result
    }

    fun longTakeProfit(): Mono<ServerResponse> {
        println("LONG TP : S $isShort, L $isLong")
        val result: Mono<ServerResponse> = if (isLong) {
            tradeClient.placeNewOrder(shortRequest())
                .flatMap { ServerResponse.ok().build() }
                .doOnSuccess { isLong = false }
                .onErrorResume { ServerResponse.notFound().build() }
        } else { ServerResponse.notFound().build() }
        return result
    }

    fun shortTrigger(): Mono<ServerResponse> {
        println("SHORT TRIGGER : S $isShort, L $isLong" )
        val result: Mono<ServerResponse> = if (!isShort) {
            tradeClient.placeNewOrder(shortRequest())
                .flatMap { ServerResponse.ok().build() }
                .doOnSuccess { isShort = true }
                .onErrorResume { ServerResponse.notFound().build() }
        } else { ServerResponse.notFound().build() }
        return result
    }

    fun shortTakeProfit(): Mono<ServerResponse> {
        println("SHORT TP : S $isShort, L $isLong")
        val result: Mono<ServerResponse> = if (isShort) {
            tradeClient.placeNewOrder(longRequest())
                .flatMap { ServerResponse.ok().build() }
                .doOnSuccess { isShort = false }
                .onErrorResume { ServerResponse.notFound().build() }
        } else { ServerResponse.notFound().build() }
        return result
    }

    fun bullish(): Mono<ServerResponse> {
        println("BULLISH")
        return ServerResponse.ok().build()
    }

    fun bearish(): Mono<ServerResponse> {
        println("BEARISH")
        return ServerResponse.ok().build()
    }

    private fun longRequest() =
        NewOrderRequest(
            symbol = "BTCUSDT",
            side = OrderSide.BUY,
            type = OrderType.MARKET,
            quantity = "0.01"
        )

    private fun shortRequest() =
        NewOrderRequest(
            symbol = "BTCUSDT",
            side = OrderSide.SELL,
            type = OrderType.MARKET,
            quantity = "0.01"
        )
}