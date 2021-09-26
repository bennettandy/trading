package uk.co.avsoftware.trading.database.model

import java.math.BigDecimal
import java.math.BigDecimal.ZERO

data class TradingState(
    val symbol: String = "",
    val longPosition: String = ZERO.toPlainString(),
    val shortPosition: String = ZERO.toPlainString()
){
    fun isShort(): Boolean = BigDecimal(shortPosition).compareTo(ZERO) == 0
    fun isLong(): Boolean = BigDecimal(longPosition).compareTo(ZERO) == 0
}
