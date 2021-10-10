package uk.co.avsoftware.trading.bot

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.TradeClient
import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.client.binance.model.trade.OrderType
import uk.co.avsoftware.trading.client.binance.model.trade.NewOrderRequest
import uk.co.avsoftware.trading.client.binance.model.trade.OrderResponse
import uk.co.avsoftware.trading.client.binance.model.trade.orderQuantity
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
) {

    private val logger = KotlinLogging.logger {}

    fun longTrigger(symbol: String): Mono<State> =
        stateRepository.getState(symbol)
            .checkpoint("get state")
            .doOnSuccess { logger.info("LONG TRIGGER $symbol : State $it") }
            .flatMap {
                filterAlreadyLong(it)
                    .doOnSuccess { logger.info { "passed filter $symbol already long $it" } }
                    .flatMap { state ->
                        stateRepository.getTrade(state)
                            .checkpoint("close any open $symbol trade")
                            // if no trade, get trade is Mono.empty() -> thenReturn always emits 'state'
                            .flatMap { openTrade -> closeAndCompleteOpenTrade(openTrade, state) }
                            .thenReturn(state)
                    }
            }

            .doOnSuccess { state -> logger.info { "closed any existing $symbol trade, got state $state" } }
            .checkpoint("place new long trade")
            .flatMap { state ->
                tradeClient.placeNewOrder(longRequest(state.position_size))
                    .doOnSuccess { logger.info { "save $symbol long order response" } }
                    .checkpoint("save long order response")
                    .flatMap { orderResponse ->
                        tradeRepository.saveOrderResponse(orderResponse)
                            .doOnSuccess { logger.info { "Saved new $symbol long order response ref: $it" } }
                            .checkpoint("update state with new open long order")
                            .flatMap { orderDocReference -> stateRepository.updateState(state.copy(open_position = orderDocReference)) }
                    }
            }


    fun shortTrigger(symbol: String): Mono<State> =
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
                            .thenReturn(state)
                    }
            }
            .doOnSuccess { state -> logger.info { "closed any existing $symbol trade, got state $state" } }
            .checkpoint("place new short trade")
            .flatMap { state ->
                tradeClient.placeNewOrder(shortRequest(state.position_size))
                    .doOnSuccess { logger.info { "save $symbol short order response" } }
                    .checkpoint("save short order response")
                    .flatMap { orderResponse ->
                        tradeRepository.saveOrderResponse(orderResponse)
                            .doOnSuccess { logger.info { "Saved new short $symbol order response ref: $it" } }
                            .checkpoint("update state with new open short order")
                            .flatMap { orderDocReference -> stateRepository.updateState(state.copy(open_position = orderDocReference)) }
                    }
            }


    fun longTakeProfit(symbol: String): Mono<State> =
        stateRepository.getState(symbol)
            .checkpoint("get state")
            .doOnSuccess { logger.info("LONG TAKE PROFIT $symbol : State $it") }
            .flatMap { state ->
                stateRepository.getTrade(state)
                    .checkpoint("close any open $symbol trade")
                    // if no trade, get trade is Mono.empty() -> thenReturn always emits 'state'
                    .flatMap { openTrade ->
                        closeAndCompleteOpenTrade(openTrade, state)
                            .flatMap { stateRepository.updateState(state.copy(open_position = null)) }
                            .doOnSuccess { logger.info { "Closed $symbol Open Trade: $it" } }
                            .thenReturn(state)
                    }
            }
            .doOnSuccess { _ -> logger.info { "closed any existing $symbol trade" } }

    // fixme: identical to longTakeProfit
    fun shortTakeProfit(symbol: String): Mono<State> =
        stateRepository.getState(symbol)
            .checkpoint("get state")
            .doOnSuccess { logger.info("SHORT TAKE PROFIT $symbol : State $it") }
            .flatMap { state ->
                stateRepository.getTrade(state)
                    .checkpoint("close any open $symbol trade")
                    // if no trade, get trade is Mono.empty() -> thenReturn always emits 'state'
                    .flatMap { openTrade ->
                        closeAndCompleteOpenTrade(openTrade, state)
                            .flatMap { stateRepository.updateState(state.copy(open_position = null)) }
                            .doOnSuccess { logger.info { "Closed Open $symbol Trade: $it" } }
                            .thenReturn(state)
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

    private fun closeAndCompleteOpenTrade(orderResponse: OrderResponse, state: State): Mono<String> {
        return Mono.just(reversalTrade(orderResponse))
            .flatMap { newOrderRequest ->
            tradeClient.placeNewOrder(newOrderRequest)
                .checkpoint("place new order to close position")
                .doOnSuccess { logger.info { "Closed trade with ${newOrderRequest.side}" } }
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
        }
    }

    private fun reversalTrade(orderResponse: OrderResponse): NewOrderRequest {
        return when (orderResponse.side) {
            OrderSide.SELL -> longRequest(orderResponse.orderQuantity()) // closing a sell order with a corresponding buy
            else -> shortRequest(orderResponse.orderQuantity()) // else close buy order with a corresponding sell
        }
    }

    private fun longRequest(tradeAmount: Double) =
        NewOrderRequest(
            symbol = "SOLBTC",
            side = OrderSide.BUY,
            type = OrderType.MARKET,
            quantity = String.format("%.8f", tradeAmount)
        )

    private fun shortRequest(tradeAmount: Double) =
        NewOrderRequest(
            symbol = "SOLBTC",
            side = OrderSide.SELL,
            type = OrderType.MARKET,
            quantity = String.format("%.8f", tradeAmount)
        )

    private fun filterAlreadyLong(state: State): Mono<State> {
        if (state.open_position == null) return Mono.just(state)
        return stateRepository.getTrade(state)
            .filter { it.side == OrderSide.SELL }
            .map { state }
    }

    private fun filterAlreadyShort(state: State): Mono<State> {
        if (state.open_position == null) return Mono.just(state)
        return stateRepository.getTrade(state)
            .filter { it.side == OrderSide.BUY }
            .map { state }
    }

    companion object {
        //const val SYMBOL = "SOLBTC"
    }

}