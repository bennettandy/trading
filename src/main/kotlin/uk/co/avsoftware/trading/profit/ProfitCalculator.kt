package uk.co.avsoftware.trading.profit

import uk.co.avsoftware.trading.database.model.Position
import uk.co.avsoftware.trading.database.model.Profits

object ProfitCalculator {
    fun calculateProfits(position: Position): Position {
        if (position.open_quantity.isNotEmpty()) {
            val openCount = position.open_quantity.size
            val totalOpenCost: Double = (0..openCount).sumOf { i -> position.open_quantity[i] * position.open_price[i] }
            val totalOpenQuantity: Double = position.open_quantity.sum()
            val totalOpenCommission: Double = position.open_commission.sum()

            position.open_cost = totalOpenCost
            position.open_comm = totalOpenCommission
            position.open_qty = totalOpenQuantity
        }

        if (position.close_quantity.isNotEmpty()) {
            val closeCount = position.close_quantity.size
            val totalCloseCost: Double =
                (0..closeCount).sumOf { i -> position.close_quantity[i] * position.close_price[i] }
            val totalCloseQuantity: Double = position.close_quantity.sum()
            val totalCloseCommission: Double = position.close_commission.sum()

            position.close_cost = totalCloseCost
            position.close_comm = totalCloseCommission
            position.close_qty = totalCloseQuantity
        }

        return position
    }
}


/*
data class Position(
    var name: String = "",
    var symbol: String = "",
    var direction: String = "",
    var status: String = "",

    var open_order_id: String = "",
    var open_quantity: List<Double> = emptyList(),
    var open_price: List<Double> = emptyList(),
    var open_commission: List<Double> = emptyList(),
    var open_commission_currency: String = "",

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
 */