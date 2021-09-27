package uk.co.avsoftware.trading.database.model

data class Position(
    var name: String = "",
    var symbol: String = "",
    var direction: String = "",
    var status: String = "",
    var open_quantity: List<String> = emptyList(),
    var open_price: List<String> = emptyList(),
    var open_commission: List<String> = emptyList(),
    var close_quantity: List<String> = emptyList(),
    var close_price: List<String> = emptyList(),
    var close_commission: List<String> = emptyList(),
)