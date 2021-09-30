package uk.co.avsoftware.trading

import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.client.binance.response.OrderFill
import uk.co.avsoftware.trading.client.binance.response.OrderResponse

object TestDataHelper {
    fun createOrderResponse(orderSide: OrderSide, clientOrderId: String): OrderResponse {
        return OrderResponse(
            fills = listOf(
                OrderFill(
                    price = 10.0,
                    qty = 20.0,
                    commission = 0.0023,
                    commissionAsset = "BTC"
                )
            ),
            symbol = "SOLBTC",
            side = orderSide,
            clientOrderId = clientOrderId,
            orderId = (5000L..10000L).random(),
            transactTime = System.currentTimeMillis()
        )
    }
}