package uk.co.avsoftware.trading.database.model

import com.google.cloud.Timestamp

data class Configuration (
    val version: Int = 0,
    var timestamp: Long = 0L,
    var tradingState: TradingState? = null
)