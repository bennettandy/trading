package uk.co.avsoftware.trading.client.bybit.model

import org.springframework.beans.factory.annotation.Value

open class BybitRequest {
    open fun getQueryString(apiKey: String): String = baseQueryString(apiKey)
    fun baseQueryString(apiKey: String): String = "apiKey=${apiKey}&recvWindow=4000&timestamp=${System.currentTimeMillis()}"
}