package uk.co.avsoftware.trading.client.binance.model.trade

data class BinanceError(
    val code: Int,
    val msg: String
) : Throwable()