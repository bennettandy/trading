package uk.co.avsoftware.trading.client.binance.request

import org.springframework.web.reactive.function.server.ServerRequest

data class SymbolPriceTickerRequest(
    val symbol: String?
) : BinanceRequest() {

    override fun getQueryString(): String {
        return StringBuilder().apply{
            symbol?.let { append("&symbol=${it}") }
        }.toString()
    }

    companion object {
        fun from(request: ServerRequest): SymbolPriceTickerRequest {
            return with (request) {
                SymbolPriceTickerRequest(
                    symbol = queryParam("symbol").orElse(null)
                )
            }
        }
    }
}