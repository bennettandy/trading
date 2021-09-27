package uk.co.avsoftware.trading.repository

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.database.model.Position
import uk.co.avsoftware.trading.repository.service.PositionService

@Service
class PositionRepository(val positionService: PositionService) {

    fun getPosition(): Mono<Position> = positionService.retrieveOpenPosition(symbol = "SOLBTC")

    fun updatePosition(position: Position?): Mono<Position> {
        return position?.let {
            positionService.updatePosition(it).map { position }
        } ?: Mono.empty()
    }
}