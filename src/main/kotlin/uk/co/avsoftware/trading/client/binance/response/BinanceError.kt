package uk.co.avsoftware.trading.client.binance.response

data class BinanceError(
    val code: Int,
    val msg: String
) : Throwable()