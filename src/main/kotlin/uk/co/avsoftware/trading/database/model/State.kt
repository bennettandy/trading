package uk.co.avsoftware.trading.database.model

data class State(
    var exchange: String = "",
    var symbol: String = "",
    // open long position
    var long_position: String? = null,
    // open short position
    var short_position: String? = null
)
