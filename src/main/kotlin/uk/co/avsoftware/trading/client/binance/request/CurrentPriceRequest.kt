package uk.co.avsoftware.trading.client.binance.request

import org.springframework.web.reactive.function.server.ServerRequest

data class CurrentPriceRequest(
    val symbol: String
) : BinanceRequest() {

    override fun getQueryString(): String {
        return StringBuilder().apply{
            append("&symbol=${symbol}")
        }.toString()
    }

    companion object {
        fun from(request: ServerRequest): CurrentPriceRequest {
            return with (request) {
                CurrentPriceRequest(
                    symbol = pathVariable("symbol")
                )
            }
        }
    }
}