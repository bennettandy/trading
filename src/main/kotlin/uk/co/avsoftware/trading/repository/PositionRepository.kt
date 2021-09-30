package uk.co.avsoftware.trading.repository

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.response.OrderResponse
import uk.co.avsoftware.trading.database.model.Position
import uk.co.avsoftware.trading.repository.service.PositionService

@Service
class PositionRepository(val positionService: PositionService) {

    fun getPosition(documentId: String): Mono<Position> = positionService.retrievePosition(documentId = documentId)

    fun createPosition( position: Position): Mono<String> = positionService.createNewPosition(position = position)

    fun updatePosition(position: Position?): Mono<Position> {
        return position?.let {
            positionService.updatePosition(it).map { position }
        } ?: Mono.empty()
    }

    fun addOpenOrder(documentId: String, orderResponse: OrderResponse): Mono<Position> {
        return getPosition(documentId)
            .map { addOpenOrderToPosition(it, orderResponse) }
            .flatMap { updatePosition(it) }
    }

    fun addCloseOrder(documentId: String, orderResponse: OrderResponse): Mono<Position> {
        return getPosition(documentId)
            .map { addCloseOrderToPosition(it, orderResponse) }
            .flatMap { updatePosition(it) }
            .map { calculateProfits(it) }
    }

    fun addOpenOrderToPosition(position: Position, orderResponse: OrderResponse): Position{
        return position.apply {
            with (orderResponse){
                open_commission = fills?.map { orderFill -> orderFill.commission  } ?: emptyList()
                open_quantity = fills?.map { orderFill -> orderFill.qty  } ?: emptyList()
                open_price = fills?.map { orderFill -> orderFill.price  } ?: emptyList()
                direction = orderResponse.side?.name ?: "missing"
            }
        }
    }

    fun addCloseOrderToPosition(position: Position, orderResponse: OrderResponse): Position{
        return position.apply {
            with (orderResponse){
                close_commission = fills?.map { orderFill -> orderFill.commission  } ?: emptyList()
                close_quantity = fills?.map { orderFill -> orderFill.qty  } ?: emptyList()
                close_price = fills?.map { orderFill -> orderFill.price } ?: emptyList()
            }
        }
    }

    private fun calculateProfits(position: Position): Position {
        return position
    }
}