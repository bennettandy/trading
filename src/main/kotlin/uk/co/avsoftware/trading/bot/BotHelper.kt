package uk.co.avsoftware.trading.bot

import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.model.trade.*
import uk.co.avsoftware.trading.database.model.State

object BotHelper {

     fun reversalTradeRequest(orderResponse: OrderResponse, state: State): NewOrderRequest {

        // close remaining open quantity
        val remainingOpenQuantity: Double = state.open_qty

        return when (orderResponse.side) {
            OrderSide.SELL -> longRequest(remainingOpenQuantity, state) // closing a sell order with a corresponding buy
            else -> shortRequest(remainingOpenQuantity, state) // else close buy order with a corresponding sell
        }
    }

     fun longRequest(tradeAmount: Double, state: State) =
        NewOrderRequest(
            symbol = state.symbol,
            side = OrderSide.BUY,
            type = OrderType.MARKET,
            quantity = String.format("%.8f", tradeAmount)
        )

     fun shortRequest(tradeAmount: Double, state: State) =
        NewOrderRequest(
            symbol = state.symbol,
            side = OrderSide.SELL,
            type = OrderType.MARKET,
            quantity = String.format("%.8f", tradeAmount)
        )
}