package uk.co.avsoftware.trading.client.binance.parameters

import org.springframework.web.reactive.function.server.ServerRequest
import java.math.BigDecimal

data class TradeListRequest(
    val symbol: String,
    val side: String, // enum
    val type: String, // enum
    val timeInForce: String?, // enum
    val quantity: BigDecimal?,
    val quoteOrderQty: BigDecimal?,
    val price: BigDecimal?,
    val newClientOrderId: String?, // A unique id among open orders. Automatically generated if not sent.
    val stopPrice: BigDecimal?, // Used with STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, and TAKE_PROFIT_LIMIT orders.
    val icebergQty: BigDecimal?, // Used with LIMIT, STOP_LOSS_LIMIT, and TAKE_PROFIT_LIMIT to create an iceberg order.
    val newOrderRespType: String?, // Set the response JSON. ACK, RESULT, or FULL; MARKET and LIMIT order types default to FULL, all other orders default to ACK.

) : BinanceRequest() {

    override fun getQueryString(): String {
        return with(StringBuilder(super.getQueryString())){
            append("&symbol=${symbol}")
            append("&side=${side}")
            append("&type=${type}")
            timeInForce?.let { append("&timeInForce=${it}")}
            quantity?.let { append("&quantity=${it}")}
            quoteOrderQty?.let { append("&quoteOrderQty=${it}")}
            price?.let { append("&price=${it}")}
            newClientOrderId?.let { append("&newClientOrderId=${it}")}
            stopPrice?.let { append("&stopPrice=${it}")}
            icebergQty?.let { append("&icebergQty=${it}")}
            newOrderRespType?.let { append("&newOrderRespType=${it}")}
        }.toString()
    }

    companion object {
        fun from(request: ServerRequest): TradeListRequest {
            return with (request) {
                TradeListRequest(
                    symbol = queryParam("symbol").orElseThrow(),
                    side = queryParam("side").orElseThrow(),
                    type = queryParam("type").orElseThrow(),
                    timeInForce = queryParam("timeInForce").orElse(null),
                    quantity = queryParam("quantity").map { BigDecimal(it) }.orElse(null),
                    quoteOrderQty = queryParam("quoteOrderQty").map { BigDecimal(it) }.orElse(null),
                    price = queryParam("price").map { BigDecimal(it) }.orElse(null),
                    newClientOrderId = queryParam("newClientOrderId").orElse(null),
                    stopPrice = queryParam("stopPrice").map { BigDecimal(it) }.orElse(null),
                    icebergQty = queryParam("icebergQty").map { BigDecimal(it) }.orElse(null),
                    newOrderRespType = queryParam("newOrderRespType").orElse(null),
                )
            }
        }
    }
}