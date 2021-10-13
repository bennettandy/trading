package uk.co.avsoftware.trading.bot

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.TradeClient
import uk.co.avsoftware.trading.client.binance.model.trade.*
import uk.co.avsoftware.trading.database.model.State
import uk.co.avsoftware.trading.repository.CompletedTradeRepository
import uk.co.avsoftware.trading.repository.StateRepository
import uk.co.avsoftware.trading.repository.TradeRepository

@Component
class TradingBot(
    val tradeClient: TradeClient,
    val stateRepository: StateRepository,
    val tradeRepository: TradeRepository,
    val completedTradeRepository: CompletedTradeRepository
) : Bot {

    private val logger = KotlinLogging.logger {}

    override fun longTrigger(symbol: String): Mono<State> =
        stateRepository.getState(symbol)
            .checkpoint("get state")
            .doOnSuccess { logger.info{"LONG TRIGGER $symbol : State $it"} }
            .flatMap {
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


    override fun shortTrigger(symbol: String): Mono<State> =
        stateRepository.getState(symbol)
            .checkpoint("get state")
            .doOnSuccess { logger.info("SHORT TRIGGER $symbol : State $it") }
            .flatMap {
                filterAlreadyShort(it)
                    .doOnSuccess { logger.info("Passed $symbol already short : State $it") }
                    .flatMap { state ->
                        stateRepository.getTrade(state)
                            .checkpoint("close any open trade")
                            // if no trade, get trade is Mono.empty() -> thenReturn always emits 'state'
                            .flatMap { openTrade -> closeAndCompleteOpenTrade(openTrade, state) }
                            .defaultIfEmpty(state)
                    }
            }
            .doOnSuccess { state -> logger.info { "closed any existing $symbol trade, got state $state" } }
            .checkpoint("place new short trade")
            .flatMap { state ->
                tradeClient.placeNewOrder(shortRequest(state.position_size, state))
                    .doOnSuccess { logger.info { "save $symbol short order response" } }
                    .checkpoint("save short order response")
                    .flatMap { orderResponse ->
                        tradeRepository.saveOrderResponse(orderResponse)
                            .doOnSuccess { logger.info { "Saved new short $symbol order response ref: $it" } }
                            .checkpoint("update state with new open short order")
                            .flatMap { orderDocReference -> stateRepository.updateState(state.copy(open_position = orderDocReference, direction = BotDirection.SHORT)) }
                    }
            }


    override fun longTakeProfit(symbol: String): Mono<State> =
        stateRepository.getState(symbol)
            .checkpoint("get state")
            .doOnSuccess { logger.info("LONG TAKE PROFIT $symbol : State $it") }
            .flatMap { state ->
                stateRepository.getTrade(state)
                    .checkpoint("close any open $symbol trade")
                    // if no trade, get trade is Mono.empty() -> thenReturn always emits 'state'
                    .flatMap { openTrade ->
                        closeAndCompleteOpenTrade(openTrade, state)
                            .flatMap { stateRepository.updateState(state.copy(open_position = null, direction = BotDirection.IDLE)) }
                            .doOnSuccess { logger.info { "Closed $symbol Open Trade: $it" } }
                    }
            }
            .doOnSuccess { _ -> logger.info { "closed any existing $symbol trade" } }

    // fixme: identical to longTakeProfit
    override fun shortTakeProfit(symbol: String): Mono<State> =
        stateRepository.getState(symbol)
            .checkpoint("get state")
            .doOnSuccess { logger.info("SHORT TAKE PROFIT $symbol : State $it") }
            .flatMap { state ->
                stateRepository.getTrade(state)
                    .checkpoint("close any open $symbol trade")
                    // if no trade, get trade is Mono.empty() -> thenReturn always emits 'state'
                    .flatMap { openTrade ->
                        closeAndCompleteOpenTrade(openTrade, state)
                            .flatMap { stateRepository.updateState(state.copy(open_position = null, direction = BotDirection.IDLE)) }
                            .doOnSuccess { logger.info { "Closed Open $symbol Trade: $it" } }
                    }
            }
            .doOnSuccess { _ -> logger.info { "closed any existing $symbol trade" } }


    fun bullish(symbol: String): Mono<ServerResponse> {
        logger.info("BULLISH $symbol")
        return ServerResponse.ok().build()
    }

    fun bearish(symbol: String): Mono<ServerResponse> {
        logger.info("BEARISH $symbol")
        return ServerResponse.ok().build()
    }

    fun test(): Mono<ServerResponse> {
        return ServerResponse.ok().build()
    }

    fun testOpen(): Mono<ServerResponse> {
        return ServerResponse.ok().build()
    }

    fun testClose(): Mono<ServerResponse> {
        return ServerResponse.ok().build()
    }

    private fun closeAndCompleteOpenTrade(orderResponse: OrderResponse, state: State): Mono<State> {
        return Mono.just(reversalTradeRequest(orderResponse, state))
            .flatMap { newOrderRequest ->
            tradeClient.placeNewOrder(newOrderRequest)
                .checkpoint("place new order to close position")
                .doOnSuccess { logger.info { "Closed trade with ${newOrderRequest.side}" } }
                .doOnSuccess { state.apply {
                    // no open position
                    open_qty = 0.0
                    direction = BotDirection.IDLE
                } }
                .flatMap { closeOrderResponse ->
                    tradeRepository.saveOrderResponse(closeOrderResponse)
                        .checkpoint("saved closing trade to DB")
                        .doOnSuccess { logger.info { "saved closing trade to DB $it" } }
                        .flatMap {
                            completedTradeRepository.createCompletedTrade(orderResponse, closeOrderResponse, state)
                                .checkpoint("crete Completed Trade document")
                        }
                        .doOnSuccess { logger.info { "Saved Completed Trade to DB" } }
                }
                .thenReturn(state)
        }
    }

    private fun reversalTradeRequest(orderResponse: OrderResponse, state: State): NewOrderRequest {

        // close remaining open quantity
        val remainingOpenQuantity: Double = state.open_qty

        return when (orderResponse.side) {
            OrderSide.SELL -> longRequest(remainingOpenQuantity, state) // closing a sell order with a corresponding buy
            else -> shortRequest(remainingOpenQuantity, state) // else close buy order with a corresponding sell
        }
    }

    private fun longRequest(tradeAmount: Double, state: State) =
        NewOrderRequest(
            symbol = state.symbol,
            side = OrderSide.BUY,
            type = OrderType.MARKET,
            quantity = String.format("%.8f", tradeAmount)
        ).also { state.open_qty = tradeAmount }

    private fun shortRequest(tradeAmount: Double, state: State) =
        NewOrderRequest(
            symbol = state.symbol,
            side = OrderSide.SELL,
            type = OrderType.MARKET,
            quantity = String.format("%.8f", tradeAmount)
        ).also { state.open_qty = tradeAmount }

    private fun filterAlreadyLong(state: State): Mono<State> {
        if (state.open_position == null) return Mono.just(state)
        return stateRepository.getTrade(state)
            .filter { it.side == OrderSide.SELL } // allow short to pass so we can close
            .map { state }
    }

    private fun filterAlreadyShort(state: State): Mono<State> {
        if (state.open_position == null) return Mono.just(state)
        return stateRepository.getTrade(state)
            .filter { it.side == OrderSide.BUY } // allow long to pass so we can close
            .map { state }
    }

}

interface Bot {
    fun longTrigger(symbol: String): Mono<State>
    fun shortTrigger(symbol: String): Mono<State>
    fun shortTakeProfit(symbol: String): Mono<State>
    fun longTakeProfit(symbol: String): Mono<State>
}