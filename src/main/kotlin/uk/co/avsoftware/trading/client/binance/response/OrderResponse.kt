package uk.co.avsoftware.trading.client.binance.response

import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.client.binance.model.trade.OrderStatus
import uk.co.avsoftware.trading.client.binance.model.trade.OrderType
import uk.co.avsoftware.trading.client.binance.model.trade.TimeInForce

data class OrderResponse(
    // ACK
    val symbol: String? = "",
    val orderId: Long? = 0L,
    val orderListId: Long? = 0L,
    val clientOrderId: String? = "",
    val transactTime: Long? =0L,
    // RESULT
    val price: String? = "",
    val origQty: String? = "",
    val executedQty: String? = "",
    val cumulativeQuoteQty: String? = "",
    val status: OrderStatus? = null,
    val timeInForce: TimeInForce? = null,
    val type: OrderType? = null,
    val side: OrderSide? = null,
    // FULL
    val fills: List<OrderFill>? = emptyList()
)

data class OrderFill(
    val price: Double = 0.0,
    val qty: Double = 0.0,
    val commission: Double = 0.0,
    val commissionAsset: String = ""
)