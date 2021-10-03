package uk.co.avsoftware.trading.repository

import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.model.trade.OrderResponse
import uk.co.avsoftware.trading.database.model.CompletedTrade
import uk.co.avsoftware.trading.repository.service.CompletedTradeService

@Service
class CompletedTradeRepository(val completedTradeService: CompletedTradeService) {

    private val logger = KotlinLogging.logger {}

    fun createCompletedTrade(openingOrder: OrderResponse, closingOrder: OrderResponse): Mono<String> {
        // todo: simplify this
        val completedTrade = CompletedTrade()
        addOpenOrderToCompletedTrade(completedTrade, openingOrder)
        addCloseOrderToCompletedTrade(completedTrade, closingOrder)
        calculateProfits(completedTrade)
        return completedTradeService.saveNewCompletedTrade(completedTrade)
    }


    fun addOpenOrderToCompletedTrade(completedTrade: CompletedTrade, orderResponse: OrderResponse) {
        completedTrade.apply {
            with (orderResponse){
                open_commission = fills?.map { orderFill -> orderFill.commission  } ?: emptyList()
                open_quantity = fills?.map { orderFill -> orderFill.qty  } ?: emptyList()
                open_price = fills?.map { orderFill -> orderFill.price  } ?: emptyList()
                direction = orderResponse.side?.name ?: "missing"
                open_commission_currency = fills?.first()?.commissionAsset ?: "unknown"
                open_order_id = orderResponse.orderId.toString()
                open_time_stamp = orderResponse.transactTime ?: -1
            }
            logger.info { "Added open order to CompletedTrade: $this" }
        }
    }

    fun addCloseOrderToCompletedTrade(completedTrade: CompletedTrade, orderResponse: OrderResponse) {
        completedTrade.apply {
            with (orderResponse){
                close_commission = fills?.map { orderFill -> orderFill.commission  } ?: emptyList()
                close_quantity = fills?.map { orderFill -> orderFill.qty  } ?: emptyList()
                close_price = fills?.map { orderFill -> orderFill.price } ?: emptyList()
                direction = orderResponse.side?.name ?: "missing"
                close_commission_currency = fills?.first()?.commissionAsset ?: "unknown"
                close_order_id = orderResponse.orderId.toString()
                close_time_stamp = orderResponse.transactTime ?: -1
            }
            logger.info { "Added close order to CompletedTrade: $this" }
        }
    }

    private fun calculateProfits(completedTrade: CompletedTrade) {
        completedTrade.apply {
            open_qty = completedTrade.open_quantity.sum()
            open_cost = completedTrade.open_quantity
                .mapIndexed { index, d -> d*completedTrade.open_price[index]  }.sum()
            open_comm = completedTrade.open_commission.sum()
            close_qty = completedTrade.close_quantity.sum()
            close_cost = completedTrade.close_quantity
                .mapIndexed { index, d -> d*completedTrade.close_price[index]  }.sum()
            close_comm = completedTrade.close_commission.sum()
            price_delta = open_cost - close_cost
            logger.info { "Calculated Totals: Position: $this" }
        }
    }
}