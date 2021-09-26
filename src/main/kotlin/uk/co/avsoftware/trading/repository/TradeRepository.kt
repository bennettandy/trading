package uk.co.avsoftware.trading.repository

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.request.NewOrderRequest
import uk.co.avsoftware.trading.client.binance.response.OrderResponse
import uk.co.avsoftware.trading.database.model.Configuration
import uk.co.avsoftware.trading.database.model.Trade
import uk.co.avsoftware.trading.database.model.TradeStatus
import uk.co.avsoftware.trading.repository.service.ConfigurationService
import uk.co.avsoftware.trading.repository.service.TradeService

@Service
class TradeRepository( val tradeService: TradeService, val configurationService: ConfigurationService) {

    fun createTrade( newOrderRequest: NewOrderRequest ){

        //val trade: Trade = tradeFromNewOrderRequest( newOrderRequest )

        // todo: implement storage
        //tradeService.saveTrade(trade)
    }

    fun updateTrade(it: OrderResponse) {

        //val trade: Trade = tradeService.getTrade()
    }

    fun saveOrderResponse(orderResponse: OrderResponse?): Mono<String> =
        orderResponse?.let { tradeService.saveOrderResponse(it) }
            ?: Mono.empty()

    companion object {


        fun tradeFromNewOrderRequest( newOrderRequest: NewOrderRequest): Trade =
            with (newOrderRequest) {
                Trade(
                    owner = "andy",
                    exchange = "binance",
                    pair = symbol,
                    status = TradeStatus.IDLE,
                    openingOrder = null,
                    closingOrder = null,
                    profit = null
                )
            }
    }
}