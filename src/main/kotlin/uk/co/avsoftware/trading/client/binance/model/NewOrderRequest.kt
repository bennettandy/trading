package uk.co.avsoftware.trading.client.binance.model

import org.springframework.web.reactive.function.server.ServerRequest

data class NewOrderRequest(
    val symbol: String,
    val side: OrderSide, // enum
    val type: OrderType, // enum
    val timeInForce: TimeInForce? = null, // enum
    val quantity: String?,
    val quoteOrderQty: String? = null,
    val price: String? = null,
    val newClientOrderId: String? = null, // A unique id among open orders. Automatically generated if not sent.
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

//    companion object {
//        fun from(request: ServerRequest): NewOrderRequest =
//            with(request) {
//                NewOrderRequest(
//                    symbol = queryParam("symbol").get(),
//                    side = queryParam("side").map { OrderSide.valueOf(it) }.get(),
//                    type = queryParam("type").map { OrderType.valueOf(it) }.get(),
//                    timeInForce = queryParam("timeInForce").map { TimeInForce.valueOf(it) }.get(),
//                    quantity = queryParam("quantity").get(),
//                    quoteOrderQty = queryParam("quoteOrderQty").get(),
//                    price = queryParam("price").get(),
//                    newClientOrderId = queryParam("newClientOrderId").get(),
//                    stopPrice = queryParam("stopPrice").get(),
//                    icebergQty = queryParam("icebergQty").get(),
//                    newOrderRespType = queryParam("newOrderRespType").map { NewOrderResponseType.valueOf(it) }
//                        .get(),
//                )
//            }
//    }
}