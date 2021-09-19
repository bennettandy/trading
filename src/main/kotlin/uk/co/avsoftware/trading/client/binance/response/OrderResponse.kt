package uk.co.avsoftware.trading.client.binance.response

import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.client.binance.model.trade.OrderStatus
import uk.co.avsoftware.trading.client.binance.model.trade.OrderType
import uk.co.avsoftware.trading.client.binance.model.trade.TimeInForce
import java.math.BigDecimal

data class OrderResponse(
    // ACK
    val symbol: String,
    val orderId: Long,
    val orderListId: Long,
    val clientOrderId: String,
    val transactTime: Long,
    // RESULT
    val price: BigDecimal?,
    val origQty: BigDecimal?,
    val executedQty: BigDecimal?,
    val cumulativeQuoteQty: BigDecimal?,
    val status: OrderStatus?,
    val timeInForce: TimeInForce?,
    val type: OrderType?,
    val side: OrderSide?,
    // FULL
    val fills: Array<OrderFill>?
)

data class OrderFill(
    val price: BigDecimal,
    val qty: BigDecimal,
    val commission: BigDecimal,
    val commissionAsset: String
)