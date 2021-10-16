package uk.co.avsoftware.trading.client.binance.model

data class BinanceError(
    val code: Int,
    val msg: String
) : Throwable()