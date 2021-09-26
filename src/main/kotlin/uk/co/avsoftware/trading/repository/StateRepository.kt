package uk.co.avsoftware.trading.repository

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.database.model.State
import uk.co.avsoftware.trading.repository.service.StateService

@Service
class StateRepository(val stateService: StateService) {

    fun getState(): Mono<State> = stateService.retrieveState()

    fun updateState(state: State?): Mono<State> {
        return state?.let {
            stateService.updateState(it).map { state }
        } ?: Mono.empty()
    }
}