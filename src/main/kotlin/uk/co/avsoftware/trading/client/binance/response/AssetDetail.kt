package uk.co.avsoftware.trading.client.binance.response

import java.math.BigDecimal

data class AssetDetail(
    val withdrawFee: BigDecimal,
    val minWithdrawAmount: BigDecimal,
    val withdrawStatus: Boolean,
    val depositStatus: Boolean,
    val depositTip: String?
)
