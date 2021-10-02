package uk.co.avsoftware.trading.database.model

data class CompletedTrade(
    var exchange: String = "",
    var name: String = "",
    var symbol: String = "",
    var direction: String = "",

    var open_time_stamp: Long = 0L,
    var open_order_id: String = "",
    var open_quantity: List<Double> = emptyList(),
    var open_price: List<Double> = emptyList(),
    var open_commission: List<Double> = emptyList(),
    var open_commission_currency: String = "",

    var close_time_stamp: Long = 0L,
    var close_order_id: String = "",
    var close_quantity: List<Double> = emptyList(),
    var close_price: List<Double> = emptyList(),
    var close_commission: List<Double> = emptyList(),
    var close_commission_currency: String = "",

    // totals
    var open_qty: Double = 0.0,
    var open_cost: Double = 0.0,
    var open_comm: Double = 0.0,
    var close_qty: Double = 0.0,
    var close_cost: Double = 0.0,
    var close_comm: Double = 0.0
)