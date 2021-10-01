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
import uk.co.avsoftware.trading.database.model.Position
import uk.co.avsoftware.trading.database.model.State
import uk.co.avsoftware.trading.repository.PositionRepository
import uk.co.avsoftware.trading.repository.StateRepository
import uk.co.avsoftware.trading.repository.TradeRepository

@Component
class TradingBot(
    val tradeClient: TradeClient,
    val positionRepository: PositionRepository,
    val stateRepository: StateRepository,
    val tradeRepository: TradeRepository
) {

    private val logger = KotlinLogging.logger {}

    fun longTrigger(): Mono<State> =
        stateRepository.getState(SYMBOL)
            .doOnSuccess { logger.info("LONG TRIGGER : S ${it.short_position}, L ${it.long_position}") }
            .flatMap { state: State ->
                closeAnyExistingShort(state)
                    .checkpoint("close any existing short")
                    .doOnSuccess { newState -> logger.info { "New state $newState" } }
                    .flatMap { newState -> placeLongIfNoPosition(newState)
                        .checkpoint("place long if no position")
                    }
            }


    fun shortTrigger(): Mono<State> =
        stateRepository.getState(SYMBOL)
            .doOnSuccess { logger.info("SHORT TRIGGER : S ${it.short_position}, L ${it.long_position}") }
            .flatMap { state: State ->
                closeAnyExistingLong(state)
                    .checkpoint("close any existing long")
                    .flatMap { newState -> placeShortIfNoPosition(newState)
                        .checkpoint("place short if no position")
                    }
            }

    fun longTakeProfit(): Mono<State> =
        stateRepository.getState(SYMBOL)
            .doOnSuccess { logger.info("LONG TP : S ${it.short_position}, L ${it.long_position}") }
            .flatMap { state: State -> closeAnyExistingLong(state)
                .checkpoint("close any existing Long")
            }

    fun shortTakeProfit(): Mono<State> =
        stateRepository.getState(SYMBOL)
            .doOnSuccess { logger.info("SHORT TP : S ${it.short_position}, L ${it.long_position}") }
            .flatMap { state: State -> closeAnyExistingShort(state)
                .checkpoint("close any existing Short")
            }


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

    private fun closeAnyExistingShort(state: State): Mono<State> {
        val shortPositionId = state.short_position
        return when (shortPositionId != null) {
            // we have an existing short position, so place new Long Order
            true -> tradeClient.placeNewOrder(longRequest())
                .checkpoint("place new long order")
                .doOnSuccess { logger.info { "order response $it" } }
                // save Long order response to db
                .flatMap { orderResponse ->
                    tradeRepository.saveOrderResponse(orderResponse)
                        .checkpoint("saved order response")
                        .flatMap { _ ->
                            // Long Order Response -> set on current short position
                            positionRepository.addCloseOrder(
                                documentId = shortPositionId,
                                orderResponse = orderResponse
                            ).checkpoint("add close order to position")
                                // remove short position from state
                                .flatMap { stateRepository.updateState(state.copy(short_position = null))
                                    .checkpoint("remove short position from current state")
                                }
                        }
                }
                .doOnSuccess { logger.info("Close Short Success") }
                .checkpoint("closed short")
            false -> Mono.just(state)
        }
    }

    private fun closeAnyExistingLong(state: State): Mono<State> {
        val longPositionId = state.long_position
        return when (longPositionId != null) {
            // we have an existing long position, so place new Short Order
            true -> tradeClient.placeNewOrder(shortRequest())
                .checkpoint("place new short order")
                // save Short order response to db
                .flatMap { shortOrderResponse ->
                    tradeRepository.saveOrderResponse(shortOrderResponse)
                        .checkpoint("saved order response")
                        .flatMap { _ ->
                            // Short Order Response -> set on current long position
                            positionRepository.addCloseOrder(
                                documentId = longPositionId,
                                orderResponse = shortOrderResponse
                            ).checkpoint("add close order to position")
                                // remove long position from state
                                .flatMap { stateRepository.updateState(state.copy(long_position = null))
                                    .checkpoint("remove short position from current state")
                                }
                        }
                }
                .doOnSuccess { logger.info("Close Long Success") }
                .checkpoint("closed long")
            false -> Mono.just(state)
        }
    }

    private fun placeLongIfNoPosition(state: State): Mono<State> {
        val longPositionId = state.long_position
        return if (longPositionId == null) {
            // we have no long position, so place a long order
            tradeClient.placeNewOrder(longRequest())
                .checkpoint("placed new Long order")
                // save Long order response to db for records
                .flatMap { orderResponse ->
                    tradeRepository.saveOrderResponse(orderResponse)
                        .checkpoint("saved order response")
                        .flatMap { _ ->
                            // Create a new Long Position
                            val newPosition = Position(exchange = "binance", symbol = "SOLBTC")
                            positionRepository.createPosition(newPosition)
                                .checkpoint("create position")
                                // Open the Position with the current Long Order
                                .flatMap { positionDocId ->
                                    positionRepository.addOpenOrder(
                                        documentId = positionDocId,
                                        orderResponse = orderResponse
                                    )
                                        .checkpoint("added open order to position")
                                        .doOnSuccess { logger.info { "Added open order to position" } }
                                        .flatMap { // add document ID of Long position to state
                                            logger.info { "Add position doc id $positionDocId to state long_position" }
                                            stateRepository.updateState(state.copy(long_position = positionDocId))
                                                .checkpoint("update state")
                                                .doOnSuccess { logger.info { "Updated State: $it" } }
                                        }
                                }
                        }
                }
                .doOnSuccess { logger.info("Open Long Success") }
        } else Mono.just(state) // already Long, do nothing
    }

    private fun placeShortIfNoPosition(state: State): Mono<State> {
        val shortPositionId = state.short_position
        return if (shortPositionId == null) {
            // we have no short position, so place a short order
            tradeClient.placeNewOrder(shortRequest())
                // save Short order response to db for records
                .flatMap { orderResponse ->
                    tradeRepository.saveOrderResponse(orderResponse)
                        .flatMap { _ ->
                            // Create a new Short Position
                            val newPosition = Position(exchange = "binance", symbol = "SOLBTC")
                            positionRepository.createPosition(newPosition)
                                .doOnSuccess { logger.info { "Created new position id: $it" } }
                                // Open the Position with the current Short Order
                                .flatMap { positionDocId ->
                                    positionRepository.addOpenOrder(
                                        documentId = positionDocId,
                                        orderResponse = orderResponse
                                    )
                                        .doOnSuccess { logger.info { "Added open order to position" } }
                                        .flatMap { // add document ID of Short position to state
                                            logger.info { "Add position doc id $positionDocId to state short_position" }
                                            stateRepository.updateState(state.copy(short_position = positionDocId))
                                                .doOnSuccess { logger.info { "Updated State: $it" } }
                                        }

                                }
                        }
                }
                .doOnSuccess { logger.info("Open Short Success") }
        } else Mono.just(state) // already Long, do nothing
    }

    private fun longRequest() =
        NewOrderRequest(
            symbol = "SOLBTC",
            side = OrderSide.BUY,
            type = OrderType.MARKET,
            quantity = TRADE_AMOUNT
        )

    private fun shortRequest() =
        NewOrderRequest(
            symbol = "SOLBTC",
            side = OrderSide.SELL,
            type = OrderType.MARKET,
            quantity = TRADE_AMOUNT
        )

    companion object {
        const val TRADE_AMOUNT = "1.0"
        const val SYMBOL = "SOLBTC"
    }

}