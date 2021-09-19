package uk.co.avsoftware.trading.client.binance.response

import java.math.BigDecimal

data class PriceChangeResponse(
    val symbol: String,
    val priceChange: BigDecimal,
    val priceChangePercent: BigDecimal,
    val weightedAvgPrice: BigDecimal,
    val prevClosePrice: BigDecimal,
    val lastPrice: BigDecimal,
    val lastQty: BigDecimal,
    val bidPrice: BigDecimal,
    val askPrice: BigDecimal,
    val openPrice: BigDecimal,
    val highPrice: BigDecimal,
    val lowPrice: BigDecimal,
    val volume: BigDecimal,
    val quoteVolume: BigDecimal,
    val openTime: Long,
    val closeTime: Long,
    val firstId: Long,
    val lastId: Long,
    val count: Long
)