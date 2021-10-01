package uk.co.avsoftware.trading.repository

import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.response.OrderResponse
import uk.co.avsoftware.trading.database.model.Position
import uk.co.avsoftware.trading.repository.service.PositionService

@Service
class PositionRepository(val positionService: PositionService) {

    private val logger = KotlinLogging.logger {}

    fun getPosition(documentId: String): Mono<Position> = positionService.retrievePosition(documentId = documentId)

    fun createPosition( position: Position): Mono<String> = positionService.createNewPosition(position = position)

    fun updatePosition(position: Position?): Mono<Position> {
        logger.info { "update position $position" }
        return position?.let {
            positionService.updatePosition(it).map { position }
        } ?: Mono.empty()
    }

    fun addOpenOrder(documentId: String, orderResponse: OrderResponse): Mono<Position> {
        logger.info { "add open order $documentId" }
        return getPosition(documentId)
            .map { addOpenOrderToPosition(it, orderResponse) }
            .flatMap { updatePosition(it) }
    }

    fun addCloseOrder(documentId: String, orderResponse: OrderResponse): Mono<Position> {
        logger.info { "add close order $documentId" }
        return getPosition(documentId)
            .map { addCloseOrderToPosition(it, orderResponse) }
            .map { calculateProfits(it) }
            .flatMap { updatePosition(it) }

    }

    fun addOpenOrderToPosition(position: Position, orderResponse: OrderResponse): Position{
        return position.apply {
            with (orderResponse){
                open_commission = fills?.map { orderFill -> orderFill.commission  } ?: emptyList()
                open_quantity = fills?.map { orderFill -> orderFill.qty  } ?: emptyList()
                open_price = fills?.map { orderFill -> orderFill.price  } ?: emptyList()
                direction = orderResponse.side?.name ?: "missing"
                open_commission_currency = fills?.first()?.commissionAsset ?: "unknown"
                open_order_id = orderResponse.orderId.toString()
                open_time_stamp = orderResponse.transactTime ?: -1
            }
            logger.info { "Added open order to Position: ${this}" }
        }
    }

    fun addCloseOrderToPosition(position: Position, orderResponse: OrderResponse): Position{
        return position.apply {
            with (orderResponse){
                close_commission = fills?.map { orderFill -> orderFill.commission  } ?: emptyList()
                close_quantity = fills?.map { orderFill -> orderFill.qty  } ?: emptyList()
                close_price = fills?.map { orderFill -> orderFill.price } ?: emptyList()
                direction = orderResponse.side?.name ?: "missing"
                close_commission_currency = fills?.first()?.commissionAsset ?: "unknown"
                close_order_id = orderResponse.orderId.toString()
                close_time_stamp = orderResponse.transactTime ?: -1
                logger.info { "Added close order to Position: ${this}" }
            }
        }
    }

    private fun calculateProfits(position: Position): Position {
        return position.apply {
            open_qty = position.open_quantity.sum()
            open_cost = position.open_quantity.mapIndexed { index, d -> d*position.open_price[index]  }.sum()
            open_comm = position.open_commission.sum()
            close_qty = position.close_quantity.sum()
            close_cost = position.close_quantity.mapIndexed { index, d -> d*position.close_price[index]  }.sum()
            close_comm = position.close_commission.sum()
            logger.info { "Calculated Totals: Position: ${this}" }
        }
    }
}