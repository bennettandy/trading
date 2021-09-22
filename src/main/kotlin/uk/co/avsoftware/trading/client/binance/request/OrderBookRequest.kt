package uk.co.avsoftware.trading.client.binance.request

import org.springframework.web.reactive.function.server.ServerRequest

data class OrderBookRequest(
    val symbol: String,
    val limit: Int?
) : BinanceRequest() {

    override fun getQueryString(): String {
        return StringBuilder().apply{
            append("&symbol=${symbol}")
            limit?.let { append("&limit=${it}")}
        }.toString()
    }

    companion object {
        fun from(request: ServerRequest): OrderBookRequest {
            return with (request) {
                OrderBookRequest(
                    symbol = queryParam("symbol").orElse(""),
                    limit = queryParam("limit").map { it.toInt() }.get()
                )
            }
        }
    }
}