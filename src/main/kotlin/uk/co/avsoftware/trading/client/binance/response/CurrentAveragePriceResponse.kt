package uk.co.avsoftware.trading.client.binance.response

import java.math.BigDecimal

data class CurrentAveragePriceResponse(
    val mins: Int,
    val price: BigDecimal
)