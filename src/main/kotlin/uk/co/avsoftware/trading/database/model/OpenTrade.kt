package uk.co.avsoftware.trading.database.model

data class OpenTrade(
    var exchange: String = "",
    var symbol: String = "",
    var direction: Direction = Direction.LONG,
    var currently_open: Double = 0.0,
    var open_fills: List<TradeFill> = emptyList(),
    var closing_fills: List<TradeFill> = emptyList(),
    var completed: Boolean = false,
    var profit: Double = 0.0,
    var open_commission: String = "",
    var close_commission: String = ""
)

data class TradeFill(
    var timestamp: Long = 0L,
    var symbol: String = "",
    var price: Double = 0.0,
    var qty: Double = 0.0,
    var commission: Double = 0.0,
    var commissionAsset: String = ""
)