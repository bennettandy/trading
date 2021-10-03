package uk.co.avsoftware.trading.repository

import com.google.cloud.firestore.DocumentReference
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.model.trade.OrderResponse
import uk.co.avsoftware.trading.repository.service.TradeService

@Service
class TradeRepository( val tradeService: TradeService ) {

    fun saveOrderResponse(orderResponse: OrderResponse?): Mono<DocumentReference> =
        orderResponse?.let { tradeService.saveOrderResponse(it) }
            ?: Mono.empty()
}