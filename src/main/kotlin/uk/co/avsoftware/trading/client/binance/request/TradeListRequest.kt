package uk.co.avsoftware.trading.client.binance.request

import org.springframework.web.reactive.function.server.ServerRequest

data class TradeListRequest(
    val symbol: String,
    val orderId: Long?,
    val startTime: Long?,
    val endTime: Long?,
    val fromId: Long?,
    val limit: Int?,

) : BinanceRequest() {

    override fun getQueryString(): String {
        return StringBuilder(baseQueryString()).apply {
            append("&symbol=${symbol}")
            orderId?.let { append("&orderId=${it}")}
            startTime?.let { append("&startTime=${it}")}
            endTime?.let { append("&endTime=${it}")}
            fromId?.let { append("&fromId=${it}")}
            limit?.let { append("&limit=${it}")}
        }.toString()
    }

    companion object {
        fun from(request: ServerRequest): TradeListRequest {
            return with (request) {
                TradeListRequest(
                    symbol = queryParam("symbol").get(),
                    orderId = queryParam("orderId").map { it.toLong() }.get(),
                    startTime = queryParam("startTime").map { it.toLong() }.get(),
                    endTime = queryParam("endTime").map { it.toLong() }.get(),
                    fromId = queryParam("fromId").map { it.toLong() }.get(),
                    limit = queryParam("limit").map { it.toInt() }.get(),
                )
            }
        }
    }
}