package uk.co.avsoftware.trading.database.model

data class Configuration (
    var version: Long = 0,
    var timestamp: Long = 0L,
    var tradingState: TradingState? = null
)