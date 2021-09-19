package uk.co.avsoftware.trading.client.binance.response

import java.math.BigDecimal

data class OrderBookResponse(
    val lastUpdateId: Long,
    val bids: Array<Array<BigDecimal>>,
    val asks: Array<Array<BigDecimal>>
)