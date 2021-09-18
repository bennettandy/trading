package uk.co.avsoftware.trading.client.binance.model

import java.math.BigDecimal

data class DustLog(
    val total: Int,
    val userAssetDribblets: Array<UserAssetDribblet>
)

data class UserAssetDribblet(
    val operateTime: Long,
    val totalTransferedAmount: BigDecimal,
    val totalServiceChargeAmount: BigDecimal,
    val transId: Long,
    val userAssetDribbletDetails: Array<DribbletDetails>
)

data class DribbletDetails(
    val transId: Long,
    val serviceChargeAmount: BigDecimal,
    val amount: BigDecimal,
    val operateTime: Long,
    val transferedAmount: BigDecimal,
    val fromAsset: String
)