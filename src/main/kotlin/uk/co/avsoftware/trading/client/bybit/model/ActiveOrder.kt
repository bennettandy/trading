package uk.co.avsoftware.trading.client.bybit.model

data class ActiveOrder(
    val symbol: String? = null,
    var orderID: String? = null,
    val side: String? = null,
    var orderCreationPrice: Double = 0.0,
    var currentPrice: Double = 0.0,
    val executedPrice: Double = 0.0,
    val quantity: Double = 0.0,
    val expiration: Double = 0.0,
    var isLong: Boolean = false
)


/*

{
    "ret_code": 0,
    "ret_msg": "",
    "ext_code": null,
    "ext_info": null,
    "result": {
        "accountId": "279416",
        "symbol": "ETHUSDT",
        "symbolName": "ETHUSDT",
        "orderLinkId": "1634416813639208",
        "orderId": "1003955430791056384",
        "transactTime": "1634416813653",
        "price": "0",
        "origQty": "1000",
        "executedQty": "0",
        "status": "FILLED",
        "timeInForce": "GTC",
        "type": "MARKET",
        "side": "BUY"
    }
}

 */