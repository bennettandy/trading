package uk.co.avsoftware.trading.client.binance.request

import org.springframework.web.reactive.function.server.ServerRequest

data class AssetDetailRequest(
    val asset: String?
) : BinanceRequest() {

    override fun getQueryString(): String {
        return StringBuilder(baseQueryString()).apply{
            asset?.let { append("&asset=${it}") }
        }.toString()
    }

    companion object {
        fun from(request: ServerRequest): AssetDetailRequest {
            return with (request) {
                AssetDetailRequest(
                    asset = queryParam("asset").orElse(null)
                )
            }
        }
    }
}