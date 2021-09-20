package uk.co.avsoftware.trading.client.binance.request

import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Mono

// class to allow Jackson Deserialisation
class WebHookOpenRequest(){
    var symbol: String = ""
    var side: String = ""
    var type: String = ""
    var timeInForce: String = ""
    var quantity: String? = null
    var quoteOrderQty: String? = null
    var price: String? = null
    var newClientOrderId: String? = null // A unique id among open orders. Automatically generated if not sent.
    var stopPrice: String? = null // Used with STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, and TAKE_PROFIT_LIMIT orders.
    var icebergQty: String? = null // Used with LIMIT, STOP_LOSS_LIMIT, and TAKE_PROFIT_LIMIT to create an iceberg order.

    companion object {
        fun from(request: ServerRequest): Mono<WebHookOpenRequest> {
            return request.bodyToMono(WebHookOpenRequest::class.java)
        }
    }
}