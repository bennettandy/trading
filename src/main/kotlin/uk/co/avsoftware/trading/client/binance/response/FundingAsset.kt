package uk.co.avsoftware.trading.client.binance.response

import java.math.BigDecimal

data class FundingAsset(
    val asset: String,
    val free: BigDecimal,
    val locked: BigDecimal,
    val freeze: BigDecimal,
    val withdrawing: BigDecimal,
    val btcValuation: BigDecimal
)
