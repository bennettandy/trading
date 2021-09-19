package uk.co.avsoftware.trading.client.binance.parameters

open class BinanceRequest {
    open fun getQueryString(): String = "recvWindow=4000&timestamp=${System.currentTimeMillis()}"
}