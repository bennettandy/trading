package uk.co.avsoftware.trading.client.binance.model

open class BinanceRequest {
    open fun getQueryString(): String = baseQueryString()
    fun baseQueryString(): String = "recvWindow=4000&timestamp=${System.currentTimeMillis()}"
}