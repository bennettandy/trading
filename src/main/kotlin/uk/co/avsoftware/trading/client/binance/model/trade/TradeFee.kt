package uk.co.avsoftware.trading.client.binance.model.trade

import java.math.BigDecimal

data class TradeFee(
    val symbol: String,
    val makerCommission: BigDecimal,
    val takerCommission: BigDecimal
)
