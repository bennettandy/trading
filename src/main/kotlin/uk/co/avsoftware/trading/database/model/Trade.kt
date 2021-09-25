package uk.co.avsoftware.trading.database.model

data class Trade(
    val owner: String,
    val exchange: String,
    val pair: String,
    val status: TradeStatus = TradeStatus.CLOSED,
    val openingOrder: OrderResult?,
    val closingOrder: OrderResult?,
    val profit: Profit?
    ){
}



data class OrderResult(
    val symbol: String,
    val orderId: Long,
    val timestamp: Long,
    val price: String,
    val status: String,
    val type: String,
    val side: String,
    val fills: Array<Fill>,
    val profit: Profit
)

data class Fill(
    val price: String,
    val quantity: String,
    val commission: String,
    val commissionAsset: String
)

data class Profit(
    val profit: String,
    val commission: String,
)

