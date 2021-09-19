package uk.co.avsoftware.trading.client.binance.model.trade

import java.math.BigDecimal

data class Trade(
    val symbol: String,
    val id: Long,
    val orderId: Long,
    val orderListId: Long, // Unless OCO, the value will always be -1
    val price: BigDecimal,
    val qty: BigDecimal,
    val quoteQty: BigDecimal,
    val commission: BigDecimal,
    val commissionAsset: String,
    val time: Long,
    val isBuyer: Boolean,
    val isMaker: Boolean,
    val isBestMatch: Boolean
)
