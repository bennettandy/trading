package uk.co.avsoftware.trading.client.binance.model

import java.math.BigDecimal

data class CoinNetwork(
    val addressRegex: String,
    val coin: String,
    val depositDesc: String?, // shown only when "depositEnable" is false.
    val depositEnable: Boolean,
    val isDefault: Boolean,
    val memoRegex: String,
    val minConfirm: Int, // min number for balance confirmation
    val name: String,
    val network: String,
    val resetAddressStatus: Boolean,
    val specialTips: String?,
    val unlockConfirm: Int, // confirmation number for balance unlock
    val withdrawDesc: String,
    val withdrawEnable: Boolean,
    val withdrawFee: BigDecimal,
    val withdrawIntegerMultiple: BigDecimal,
    val withdrawMax: BigDecimal,
    val withdrawMin: BigDecimal,
    val sameAddress: Boolean  // If the coin needs to provide memo to withdraw
)
