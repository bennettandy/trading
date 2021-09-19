package uk.co.avsoftware.trading.client.binance.request

import org.springframework.web.reactive.function.server.ServerRequest

data class FundingAssetRequest(
    val asset: String?,
    val needBtcValuation: Boolean?
) : BinanceRequest() {

    override fun getQueryString(): String {
        return StringBuilder(baseQueryString()).apply{
            asset?.let { append("&asset=${it}")}
            needBtcValuation?.let { append("&needBtcValuation=${it}")}
        }.toString()
    }

    companion object {
        fun from(request: ServerRequest): FundingAssetRequest {
            return with (request) {
                FundingAssetRequest(
                    asset = queryParam("asset").orElse(null),
                    needBtcValuation = queryParam("needBtcValuation").map { it.toBoolean() }.orElse(null)
                )
            }
        }
    }
}