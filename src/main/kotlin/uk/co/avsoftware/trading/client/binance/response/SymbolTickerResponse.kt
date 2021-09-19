package uk.co.avsoftware.trading.client.binance.response

import java.math.BigDecimal

data class SymbolTickerResponse(
    val symbol: String,
    val price: BigDecimal
)