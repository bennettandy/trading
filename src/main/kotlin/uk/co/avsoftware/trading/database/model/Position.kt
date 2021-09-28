package uk.co.avsoftware.trading.database.model

data class Position(
    var name: String = "",
    var symbol: String = "",
    var direction: String = "",
    var status: String = "",
    var open_quantity: List<Double> = emptyList(),
    var open_price: List<Double> = emptyList(),
    var open_commission: List<Double> = emptyList(),
    var close_quantity: List<Double> = emptyList(),
    var close_price: List<Double> = emptyList(),
    var close_commission: List<Double> = emptyList(),
)