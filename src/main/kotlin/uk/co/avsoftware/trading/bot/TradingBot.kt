package uk.co.avsoftware.trading.bot

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.TradeClient
import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.client.binance.model.trade.OrderType
import uk.co.avsoftware.trading.client.binance.request.NewOrderRequest
import uk.co.avsoftware.trading.client.binance.response.OrderResponse
import uk.co.avsoftware.trading.client.binance.response.orderQuantity
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

    fun longTrigger(): Mono<State> =
        stateRepository.getState(SYMBOL)
            .checkpoint("get state")
            .doOnSuccess { logger.info("LONG TRIGGER : State $it") }
            .flatMap { state ->
                stateRepository.getTrade(state)
                    .checkpoint("close any open trade")
                    // if no trade, get trade is Mono.empty() -> thenReturn always emits 'state'
                    .flatMap { openTrade -> closeAndCompleteOpenTrade(openTrade) }
                    .thenReturn(state)
            }

            .doOnSuccess { state -> logger.info { "closed any existing trade, got state $state" } }
            .checkpoint("place new long trade")
            .flatMap { state ->
                tradeClient.placeNewOrder(longRequest(state.position_size))
                    .doOnSuccess { logger.info { "save long order response" } }
                    .checkpoint("save long order response")
                    .flatMap { orderResponse ->
                        tradeRepository.saveOrderResponse(orderResponse)
                            .doOnSuccess { logger.info { "Saved new long order response ref: $it" } }
                            .checkpoint("update state with new open long order")
                            .flatMap { orderDocReference -> stateRepository.updateState(state.copy(open_position = orderDocReference)) }
                    }
            }


    fun shortTrigger(): Mono<State> =
        stateRepository.getState(SYMBOL)
            .checkpoint("get state")
            .doOnSuccess { logger.info("SHORT TRIGGER : State $it") }
            .flatMap { state ->
                stateRepository.getTrade(state)
                    .checkpoint("close any open trade")
                    // if no trade, get trade is Mono.empty() -> thenReturn always emits 'state'
                    .flatMap { openTrade -> closeAndCompleteOpenTrade(openTrade) }
                            .thenReturn(state)
            }
            .doOnSuccess { state -> logger.info { "closed any existing trade, got state $state" } }
            .checkpoint("place new short trade")
            .flatMap { state ->
                tradeClient.placeNewOrder(shortRequest(state.position_size))
                    .doOnSuccess { logger.info { "save short order response" } }
                    .checkpoint("save short order response")
                    .flatMap { orderResponse ->
                        tradeRepository.saveOrderResponse(orderResponse)
                            .doOnSuccess { logger.info { "Saved new short order response ref: $it" } }
                            .checkpoint("update state with new open short order")
                            .flatMap { orderDocReference -> stateRepository.updateState(state.copy(open_position = orderDocReference)) }
                    }
            }


    fun longTakeProfit(): Mono<State> =
        stateRepository.getState(SYMBOL)
            .checkpoint("get state")
            .doOnSuccess { logger.info("LONG TAKE PROFIT : State $it") }
            .flatMap { state ->
                stateRepository.getTrade(state)
                    .checkpoint("close any open trade")
                    // if no trade, get trade is Mono.empty() -> thenReturn always emits 'state'
                    .flatMap { openTrade ->
                        closeAndCompleteOpenTrade(openTrade)
                            .flatMap { stateRepository.updateState(state.copy(open_position = null)) }
                            .doOnSuccess { logger.info { "Closed Open Trade: $it" } }
                            .thenReturn(state)
                    }
            }
            .doOnSuccess { state -> logger.info { "closed any existing trade" } }

    // fixme: identical to longTakeProfit
    fun shortTakeProfit(): Mono<State> =
        stateRepository.getState(SYMBOL)
            .checkpoint("get state")
            .doOnSuccess { logger.info("SHORT TAKE PROFIT : State $it") }
            .flatMap { state ->
                stateRepository.getTrade(state)
                    .checkpoint("close any open trade")
                    // if no trade, get trade is Mono.empty() -> thenReturn always emits 'state'
                    .flatMap { openTrade ->
                        closeAndCompleteOpenTrade(openTrade)
                            .flatMap { stateRepository.updateState(state.copy(open_position = null)) }
                            .doOnSuccess { logger.info { "Closed Open Trade: $it" } }
                            .thenReturn(state)
                    }
            }
            .doOnSuccess { state -> logger.info { "closed any existing trade" } }


    fun bullish(): Mono<ServerResponse> {
        logger.info("BULLISH")
        return ServerResponse.ok().build()
    }

    fun bearish(): Mono<ServerResponse> {
        logger.info("BEARISH")
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

    private fun closeAndCompleteOpenTrade(orderResponse: OrderResponse): Mono<String> {
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
                            completedTradeRepository.createCompletedTrade(orderResponse, closeOrderResponse)
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

    companion object {
        const val SYMBOL = "SOLBTC"
    }

}