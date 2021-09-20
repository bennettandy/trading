package uk.co.avsoftware.trading.client.binance.response

import java.math.BigDecimal

data class RecentTradeResponse(
    val id: Long,
    val price: BigDecimal,
    val qty: BigDecimal,
    val quoteQty: BigDecimal,
    val time: Long,
    val isBuyerMaker: Boolean,
    val isBestMatch: Boolean
)