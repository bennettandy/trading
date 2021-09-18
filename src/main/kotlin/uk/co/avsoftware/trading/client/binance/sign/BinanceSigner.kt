package uk.co.avsoftware.trading.client.binance.sign

interface BinanceSigner {
    fun signQueryString( queryString: String): String
    fun getApiKey(): String
    fun getTimestampQueryString(): String
}