package uk.co.avsoftware.trading.client.bybit.model

import uk.co.avsoftware.trading.client.binance.model.TimeInForce


data class ActiveOrderRequest(
    val symbol: String,
    val quantity: Double,
    val orderID: String? = null,
    val side: OrderSide,
    val price: Double? = null,
    val type: OrderType,
    val timeInForce: TimeInForce = TimeInForce.GTC,
    val timestamp: Long
) : BybitRequest() {

    override fun getQueryString(apiKey: String): String =
        StringBuilder(baseQueryString(apiKey)).apply {
            append("&symbol=${symbol}")
            append("&qty=${quantity}")
            append("&price=${price}")
            append("&type=${type}")
            append("&time_in_force=${timeInForce}")
            append("&timestamp=${timestamp}")

        }.toString()

}