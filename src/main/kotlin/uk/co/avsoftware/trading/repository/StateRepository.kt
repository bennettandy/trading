package uk.co.avsoftware.trading.repository

import com.google.cloud.firestore.DocumentReference
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.model.trade.OrderResponse
import uk.co.avsoftware.trading.database.model.SignalEvent
import uk.co.avsoftware.trading.database.model.State
import uk.co.avsoftware.trading.repository.service.StateService
import uk.co.avsoftware.trading.repository.service.TradeService

@Service
class StateRepository(val stateService: StateService, val tradeService: TradeService) {

    fun getState(symbol: String): Mono<State> = stateService.retrieveState(symbol)

    fun updateState(state: State?): Mono<State> {
        return state?.let {
            stateService.updateState(it).map { state }
        } ?: Mono.empty()
    }

    fun getTrade(state: State): Mono<OrderResponse> {
        val openPosition: DocumentReference? = state.open_position
        return openPosition?.let { tradeService.loadOrderResponse(it) } ?: Mono.empty()
    }

    fun updateStateWithEvent(symbol: String, event: SignalEvent): Mono<State> =
        getState(symbol)
            .map { it.copy(last_event = event)}
            .flatMap { stateService.updateState(it).thenReturn(it) }
}