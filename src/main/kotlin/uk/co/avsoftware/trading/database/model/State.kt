package uk.co.avsoftware.trading.database.model

data class State(
    var symbol: String = "",
    var isLong: Boolean = false,
    var isShort: Boolean = false
)
