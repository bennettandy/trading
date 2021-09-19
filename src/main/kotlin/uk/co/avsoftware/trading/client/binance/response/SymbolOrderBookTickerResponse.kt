package uk.co.avsoftware.trading.client.binance.response

import java.math.BigDecimal

data class SymbolOrderBookTickerResponse(
    var symbol: String,
    var bidPrice: BigDecimal?,
    var bidQty: BigDecimal?,
    var askPrice: BigDecimal?,
    var askQty: BigDecimal?
)