package uk.co.avsoftware.trading.bot

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.BinanceTradeClient
import uk.co.avsoftware.trading.client.binance.model.OrderSide
import uk.co.avsoftware.trading.client.binance.model.OrderType
import uk.co.avsoftware.trading.client.binance.model.NewOrderRequest
import uk.co.avsoftware.trading.client.binance.model.OrderResponse
import uk.co.avsoftware.trading.client.binance.model.orderQuantity
import uk.co.avsoftware.trading.client.bybit.BybitTradeClient
import uk.co.avsoftware.trading.database.model.Direction
import uk.co.avsoftware.trading.database.model.SignalEvent
import uk.co.avsoftware.trading.database.model.State
import uk.co.avsoftware.trading.repository.CompletedTradeRepository
import uk.co.avsoftware.trading.repository.StateRepository
import uk.co.avsoftware.trading.repository.TradeRepository

@Component
class TradingBot(
    val binanceTradeClient: BinanceTradeClient,
    val bybitTradeClient: BybitTradeClient,
    val stateRepository: StateRepository,
    val tradeRepository: TradeRepository,
    val completedTradeRepository: CompletedTradeRepository
) {

    private val logger = KotlinLogging.logger {}

    fun longTrigger(symbol: String): Mono<State> =
        stateRepository.updateStateWithEvent(symbol, SignalEvent.LONG)
            .checkpoint("get state")
            .doOnSuccess { logger.info("LONG TRIGGER $symbol : State $it") }
            .filter { it.direction != Direction.LONG }
            .doOnSuccess { logger.info { "passed filter $symbol NOT already long $it" } }
            .checkpoint("place new long trade")
            .flatMap { state ->
                binanceTradeClient.placeNewOrder(longRequest(state.position_size, state.symbol))
                    .doOnSuccess { logger.info { "save $symbol long order response ${state.position_size}" } }
                    .checkpoint("save long order response")
                    .flatMap { orderResponse ->
                        tradeRepository.saveOrderResponse(orderResponse)
                            .doOnSuccess { logger.info { "Saved new $symbol long order response ref: $it" } }
                            .checkpoint("update state with new open long order")
                            .flatMap { orderDocReference ->
                                stateRepository.updateState(
                                    state.copy(
                                        open_position = orderDocReference,
                                        // fully open position
                                        remaining_position = state.position_size,
                                        direction = Direction.LONG // set direction
                                    )
                                )
                            }
                    }
            }


    fun shortTrigger(symbol: String): Mono<State> =
        stateRepository.updateStateWithEvent(symbol, SignalEvent.SHORT)
            .checkpoint("get state")
            .doOnSuccess { logger.info("SHORT TRIGGER $symbol : State $it") }
            .filter { it.direction == Direction.LONG } // close any long position fully
            .doOnSuccess { logger.info("Passed $symbol We Are Currently LONG : State $it") }
            .flatMap { state ->
                stateRepository.getTrade(state)
                    .checkpoint("close any open Long trade")
                    // if no trade, get trade is Mono.empty() -> thenReturn always emits 'state'
                    .flatMap { openTrade -> closeAndCompleteOpenTrade(openTrade, state) }
            }
            .doOnSuccess { state -> logger.info { "closed any existing $symbol trade, got state $state" } }


    fun longTakeProfit(symbol: String): Mono<State> =
        stateRepository.updateStateWithEvent(symbol, SignalEvent.LONG_TP)
            .checkpoint("get state")
            .doOnSuccess { logger.info("LONG TAKE PROFIT $symbol : State $it") }
            .flatMap { state ->
                stateRepository.getTrade(state)
                    .checkpoint("partial take profit $symbol trade")
                    // if no trade, get trade is Mono.empty() -> thenReturn always emits 'state'
                    .flatMap { openTrade -> partialTakeProfit(openTrade, state) }
            }
            .doOnSuccess { _ -> logger.info { "partial TP on $symbol trade" } }

    fun shortTakeProfit(symbol: String): Mono<State> =
        stateRepository.updateStateWithEvent(symbol, SignalEvent.SHORT_TP)
            .checkpoint("get state")
            .doOnSuccess { logger.info("SHORT TAKE PROFIT $symbol : State $it") }


    fun bullish(symbol: String): Mono<ServerResponse> {
        logger.info("BULLISH $symbol")
        return stateRepository.updateStateWithEvent(symbol, SignalEvent.BULLISH)
            .flatMap { ServerResponse.ok().build() }
    }

    fun bearish(symbol: String): Mono<ServerResponse> {
        logger.info("BEARISH $symbol")
        return stateRepository.updateStateWithEvent(symbol, SignalEvent.BEARISH)
            .flatMap { ServerResponse.ok().build() }
    }

    fun test(): Mono<ServerResponse> {
        return ServerResponse.ok().build()
    }

    fun testOpen(): Mono<ServerResponse> {
        return bybitTradeClient.placeOrder("ETHUSDT",
            uk.co.avsoftware.trading.client.bybit.model.OrderSide.BUY,
            quantity = 150.0
        )
            .doOnSuccess{ logger.info { "RESULT: $it" }}
            .flatMap { ServerResponse.ok().build() }
    }

    fun testClose(): Mono<ServerResponse> {
        return ServerResponse.ok().build()
    }

    private fun closeAndCompleteOpenTrade(orderResponse: OrderResponse, state: State): Mono<State> {
        return Mono.just(reversalTrade(orderResponse, state.symbol, orderResponse.orderQuantity()))
            // order to fully close position
            .flatMap { newOrderRequest ->
                binanceTradeClient.placeNewOrder(newOrderRequest)
                    .checkpoint("place new order to close position")
                    .doOnSuccess { logger.info { "Closed trade with ${newOrderRequest.side}" } }
                    // save the trade
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
                    // update state entity
                    .flatMap {
                        stateRepository.updateState(
                            state.copy(
                                direction = Direction.NONE,
                                open_position = null,
                                remaining_position = 0.0
                            )
                        )
                    }
            }
    }

    private fun partialTakeProfit(orderResponse: OrderResponse, state: State): Mono<State> {
        return Mono.just(state.remaining_position / TAKE_PROFIT_DIVISOR)
            .flatMap { qty -> binanceTradeClient.placeNewOrder(reversalTrade(orderResponse, state.symbol, qty ))
                    .checkpoint("place new order to close position")
                    .doOnSuccess { logger.info { "Closed trade with $qty ${it.side}" } }
                    // save the trade
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
                    // update state entity
                    .flatMap {
                        stateRepository.updateState(
                            state.copy(
                                // only update remaining position, trade stays open
                                remaining_position = state.remaining_position - qty
                            )
                        )
                    }
            }
    }

    private fun reversalTrade(orderResponse: OrderResponse, symbol: String, size: Double): NewOrderRequest {
        return when (orderResponse.side) {
            OrderSide.SELL -> longRequest(
                size,
                symbol
            ) // closing a sell order with a corresponding buy
            else -> shortRequest(
                size,
                symbol
            ) // else close buy order with a corresponding sell
        }
    }

    private fun longRequest(tradeAmount: Double, symbol: String) =
        NewOrderRequest(
            symbol = symbol,
            side = OrderSide.BUY,
            type = OrderType.MARKET,
            quantity = String.format("%.8f", tradeAmount)
        )

    private fun shortRequest(tradeAmount: Double, symbol: String) =
        NewOrderRequest(
            symbol = symbol,
            side = OrderSide.SELL,
            type = OrderType.MARKET,
            quantity = String.format("%.8f", tradeAmount)
        )

    companion object {
        const val TAKE_PROFIT_DIVISOR = 2.0
    }
}