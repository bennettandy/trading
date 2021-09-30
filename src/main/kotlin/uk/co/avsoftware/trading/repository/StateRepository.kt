package uk.co.avsoftware.trading.repository

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.database.model.State
import uk.co.avsoftware.trading.repository.service.StateService

@Service
class StateRepository(val stateService: StateService) {

    fun isLong(symbol: String): Mono<Boolean> =
        getState(symbol).map { it.long_position != null }

    fun isShort(symbol: String): Mono<Boolean> =
        getState(symbol).map { it.short_position != null }



    fun getState(symbol: String): Mono<State> = stateService.retrieveState(symbol)

    fun updateState(state: State?): Mono<State> {
        return state?.let {
            stateService.updateState(it).map { state }
        } ?: Mono.empty()
    }
}