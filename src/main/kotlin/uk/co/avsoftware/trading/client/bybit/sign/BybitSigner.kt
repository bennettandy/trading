package uk.co.avsoftware.trading.client.binance.sign

interface BybitSigner {
    fun signQueryString( queryString: String): String
    fun getApiKey(): String
}