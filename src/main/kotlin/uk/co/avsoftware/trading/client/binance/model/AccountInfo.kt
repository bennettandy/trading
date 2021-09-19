package uk.co.avsoftware.trading.client.binance.model

import java.math.BigDecimal

data class AccountInfo(
    val makerCommission: Int,
    val takerCommission: Int,
    val buyerCommission: Int,
    val sellerCommission: Int,
    val canTrade: Boolean,
    val canWithdraw: Boolean,
    val canDeposit: Boolean,
    val updateTime: Long,
    val accountType: String,
    val balances: Array<AccountBalance>,
    val permissions: Array<String>
)

data class AccountBalance(
    val asset: String,
    val free: BigDecimal,
    val locked: BigDecimal
)
