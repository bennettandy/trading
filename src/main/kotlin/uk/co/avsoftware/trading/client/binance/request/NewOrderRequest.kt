package uk.co.avsoftware.trading.client.binance.request

import org.springframework.web.reactive.function.server.ServerRequest
import uk.co.avsoftware.trading.client.binance.model.trade.NewOrderResponseType
import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.client.binance.model.trade.TimeInForce
import uk.co.avsoftware.trading.client.binance.model.trade.OrderType

data class NewOrderRequest(
    val symbol: String,
    val side: OrderSide, // enum
    val type: OrderType, // enum
    val timeInForce: TimeInForce?, // enum
    val quantity: String?,
    val quoteOrderQty: String? = null,
    val price: String?,
    val newClientOrderId: String?, // A unique id among open orders. Automatically generated if not sent.
    val stopPrice: String? = null, // Used with STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, and TAKE_PROFIT_LIMIT orders.
    val icebergQty: String? = null, // Used with LIMIT, STOP_LOSS_LIMIT, and TAKE_PROFIT_LIMIT to create an iceberg order.
    val newOrderRespType: NewOrderResponseType? = null, // Set the response JSON. ACK, RESULT, or FULL; MARKET and LIMIT order types default to FULL, all other orders default to ACK.

) : BinanceRequest() {

    override fun getQueryString(): String =
        StringBuilder(baseQueryString()).apply {
            append("&symbol=${symbol}")
            append("&side=${side}")
            append("&type=${type}")
            timeInForce?.let { append("&timeInForce=${it}") }
            quantity?.let { append("&quantity=${it}") }
            quoteOrderQty?.let { append("&quoteOrderQty=${it}") }
            price?.let { append("&price=${it}") }
            newClientOrderId?.let { append("&newClientOrderId=${it}") }
            stopPrice?.let { append("&stopPrice=${it}") }
            icebergQty?.let { append("&icebergQty=${it}") }
            newOrderRespType?.let { append("&newOrderRespType=${it}") }
        }.toString()

    companion object {
        fun from(request: ServerRequest): NewOrderRequest =
            with(request) {
                NewOrderRequest(
                    symbol = queryParam("symbol").orElseThrow(),
                    side = queryParam("side").map { OrderSide.valueOf(it) }.orElseThrow(),
                    type = queryParam("type").map { OrderType.valueOf(it) }.orElseThrow(),
                    timeInForce = queryParam("timeInForce").map { TimeInForce.valueOf(it) }.orElse(null),
                    quantity = queryParam("quantity").orElse(null),
                    quoteOrderQty = queryParam("quoteOrderQty").orElse(null),
                    price = queryParam("price").orElse(null),
                    newClientOrderId = queryParam("newClientOrderId").orElse(null),
                    stopPrice = queryParam("stopPrice").orElse(null),
                    icebergQty = queryParam("icebergQty").orElse(null),
                    newOrderRespType = queryParam("newOrderRespType").map { NewOrderResponseType.valueOf(it) }
                        .orElse(null),
                )
            }
    }
}