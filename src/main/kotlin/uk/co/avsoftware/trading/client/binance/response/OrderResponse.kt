package uk.co.avsoftware.trading.client.binance.response

import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.client.binance.model.trade.OrderStatus
import uk.co.avsoftware.trading.client.binance.model.trade.OrderType
import uk.co.avsoftware.trading.client.binance.model.trade.TimeInForce

data class OrderResponse(
    // ACK
    val symbol: String?,
    val orderId: Long?,
    val orderListId: Long?,
    val clientOrderId: String?,
    val transactTime: Long?,
    // RESULT
    val price: String?,
    val origQty: String?,
    val executedQty: String?,
    val cumulativeQuoteQty: String?,
    val status: OrderStatus?,
    val timeInForce: TimeInForce?,
    val type: OrderType?,
    val side: OrderSide?,
    // FULL
    val fills: List<OrderFill>?
)

data class OrderFill(
    val price: String,
    val qty: String,
    val commission: String,
    val commissionAsset: String
)