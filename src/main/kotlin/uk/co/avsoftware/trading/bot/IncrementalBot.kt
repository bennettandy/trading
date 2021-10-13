package uk.co.avsoftware.trading.bot

import mu.KotlinLogging
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.TradeClient
import uk.co.avsoftware.trading.client.binance.model.trade.BotDirection
import uk.co.avsoftware.trading.client.binance.model.trade.OrderResponse
import uk.co.avsoftware.trading.database.model.State
import uk.co.avsoftware.trading.repository.CompletedTradeRepository
import uk.co.avsoftware.trading.repository.StateRepository
import uk.co.avsoftware.trading.repository.TradeRepository

@Component
class IncrementalBot(
    val tradeClient: TradeClient,
    val stateRepository: StateRepository,
    val tradeRepository: TradeRepository,
    val completedTradeRepository: CompletedTradeRepository
) : Bot {

    private val logger = KotlinLogging.logger {}

    // TODO: NEED TO REWRITE THIS
    override fun longTrigger(symbol: String): Mono<State> {
        stateRepository.getState(symbol)
            .checkpoint("get state")
            .doOnSuccess { logger.info{"LONG TRIGGER $symbol : State $it"} }
            .filter { it.direction != BotDirection.LONG  } // only pass if not currently long
            .flatMap { initialState: State ->
                stateRepository.getTrade(initialState) // can be empty
                    .doOnSuccess { logger.info { "Found open trade: ${it.clientOrderId}"} }
                    .flatMap { orderResponse: OrderResponse ->
                        val orderRequest = BotHelper.reversalTradeRequest(orderResponse, state = initialState)
                        tradeClient.placeNewOrder(orderRequest)
                            .flatMap {
                                val state:State = initialState.copy(
                                    open_qty = initialState.position_size,
                                    open_position =
                                )
                                tradeRepository.saveOrderResponse(it)
                            }
                    }
            }





                filterAlreadyLong(it)
                    .doOnSuccess { logger.info { "passed filter $symbol already long $it" } }
                    .flatMap { state ->
                        stateRepository.getTrade(state)
                            .doOnSuccess { logger.info { "Found open trade: ${it.clientOrderId}"} }
                            .checkpoint("close any open $symbol trade")
                            // if no trade, get trade is Mono.empty() -> thenReturn always emits 'state'
                            .flatMap { openTrade -> closeAndCompleteOpenTrade(openTrade, state) }
                            .defaultIfEmpty(state)
                            .doOnSuccess { logger.info { "passing state to part2" } }
                    }
            }

            .doOnSuccess { state -> logger.info { "part 2 closed any existing $symbol trade, got state $state" } }
            .checkpoint("place new long trade")
            .flatMap { state ->
                tradeClient.placeNewOrder(longRequest(state.position_size, state))
                    .doOnSuccess { logger.info { "save $symbol long order response" } }
                    .checkpoint("save long order response")
                    .flatMap { orderResponse ->
                        tradeRepository.saveOrderResponse(orderResponse)
                            .doOnSuccess { logger.info { "Saved new $symbol long order response ref: $it" } }
                            .checkpoint("update state with new open long order")
                            .flatMap { orderDocReference -> stateRepository.updateState(state.copy(open_position = orderDocReference, direction = BotDirection.LONG)) }
                    }
            }
    }

    override fun shortTrigger(symbol: String): Mono<State> {
        TODO("Not yet implemented")
    }

    override fun shortTakeProfit(symbol: String): Mono<State> {
        TODO("Not yet implemented")
    }

    override fun longTakeProfit(symbol: String): Mono<State> {
        TODO("Not yet implemented")
    }


}