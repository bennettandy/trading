package uk.co.avsoftware.trading.client.binance.request

import org.springframework.web.reactive.function.server.ServerRequest

data class TradeFeesRequest(
    val symbol: String?,
) : BinanceRequest() {

    override fun getQueryString(): String {
        return StringBuilder(baseQueryString()).apply{
            symbol?.let { append("&symbol=${it}")}
        }.toString()
    }

    companion object {
        fun from(request: ServerRequest): TradeFeesRequest {
            return with (request) {
                TradeFeesRequest(
                    symbol = queryParam("symbol").orElse(null)
                )
            }
        }
    }
}