package uk.co.avsoftware.trading.client.bybit.model

data class ActiveOrderResponse(
    var ret_code: Int = 0,
    var ret_message: String = "",
    var ext_code: String? = null,
    var ext_info: String? = null,
    var result: ActiveOrderResult
)

data class ActiveOrderResult(
    var accountId: String = "",
    var symbol: String = "",
    var symbolName: String = "",
    var orderLinkId: String = "",
    var orderId: String = "",
    var transactTime: String = "",
    var price: String = "",
    var origQty: String = "",
    var executedQty: String = "",
    var status: String = "",
    var timeInForce: String = "",
    var type: OrderType = OrderType.Limit,
    var side: OrderSide = OrderSide.Buy
)

enum class OrderType {
    Limit, Market
}

enum class OrderSide {
    Buy, Sell
}
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