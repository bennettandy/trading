package uk.co.avsoftware.trading.client.bybit.model

import uk.co.avsoftware.trading.client.binance.model.TimeInForce
import java.time.Instant
import java.util.*


data class ActiveOrderRequest(
    val symbol: String,
    val quantity: Double,
    val orderID: String? = null,
    val side: OrderSide,
    val price: Double? = null,
    val type: OrderType,
    val timeInForce: TimeInForce = TimeInForce.GTC,
    val timestamp: Long
) {

    fun getQueryParameters(apiKey: String): TreeMap<String, String> =
        TreeMap<String, String>().apply {
            put("api_key", apiKey)
            put("recv_window", "5000")
            put("timestamp", System.currentTimeMillis().toString())
            put("symbol", symbol)
            price?.let { put("price", "$it") }
            put("qty", "$quantity")
            put("type", type.name)
            put("side", side.name)
            put("time_in_force", timeInForce.name)
        }
}