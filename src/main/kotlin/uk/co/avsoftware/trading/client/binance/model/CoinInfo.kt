package uk.co.avsoftware.trading.client.binance.model

import java.math.BigDecimal

data class CoinInfo(
    val coin: String,
    val depositAllEnable: Boolean,
    val free: BigDecimal,
    val freeze: BigDecimal,
    val ipoable: BigDecimal,
    val ipoing: BigDecimal,
    val isLegalMoney: Boolean,
    val locked: BigDecimal,
    val name: String,
    val networkList: Array<CoinNetwork>
)
