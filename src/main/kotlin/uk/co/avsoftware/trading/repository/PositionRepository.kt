package uk.co.avsoftware.trading.repository

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.response.OrderResponse
import uk.co.avsoftware.trading.database.model.Position
import uk.co.avsoftware.trading.repository.service.PositionService

@Service
class PositionRepository(val positionService: PositionService) {

    fun getPosition(symbol: String = "SOLBTC"): Mono<Position> = positionService.retrieveOpenPosition(symbol = symbol)

    fun updatePosition(position: Position?): Mono<Position> {
        return position?.let {
            positionService.updatePosition(it).map { position }
        } ?: Mono.empty()
    }

    fun addOpenOrder(symbol: String = "SOLBTC", orderResponse: OrderResponse): Mono<Position> {
        return getPosition(symbol)
            .map { addOpenOrderToPosition(it, orderResponse) }
            .flatMap { updatePosition(it) }
    }

    fun addCloseOrder(symbol: String = "SOLBTC", orderResponse: OrderResponse): Mono<Position> {
        return getPosition(symbol)
            .map { addCloseOrderToPosition(it, orderResponse) }
            .flatMap { updatePosition(it) }
            .map { calculateProfits(it) }
    }

    private fun addOpenOrderToPosition(position: Position, orderResponse: OrderResponse): Position{
        return position.apply {
            with (orderResponse){
                open_commission = fills?.map { orderFill -> orderFill.commission  } ?: emptyList()
                open_quantity = fills?.map { orderFill -> orderFill.qty  } ?: emptyList()
                open_price = fills?.map { orderFill -> orderFill.price  } ?: emptyList()
            }
            status = "OPEN"
        }
    }

    private fun addCloseOrderToPosition(position: Position, orderResponse: OrderResponse): Position{
        return position.apply {
            with (orderResponse){
                close_commission = fills?.map { orderFill -> orderFill.commission  } ?: emptyList()
                close_quantity = fills?.map { orderFill -> orderFill.qty  } ?: emptyList()
                close_price = fills?.map { orderFill -> orderFill.price } ?: emptyList()
            }
            status = "CLOSED"
        }
    }

    private fun calculateProfits(position: Position): Position {
        return position
    }
}