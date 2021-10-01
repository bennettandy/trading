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
                    .flatMap { newState -> placeLongIfNoPosition(newState) }
            }


    fun shortTrigger(): Mono<State> =
        stateRepository.getState(SYMBOL)
            .doOnSuccess { logger.info("SHORT TRIGGER : S ${it.short_position}, L ${it.long_position}") }
            .flatMap { state: State ->
                closeAnyExistingLong(state)
                    .flatMap { newState -> placeShortIfNoPosition(newState) }
            }

    fun longTakeProfit(): Mono<State> =
        stateRepository.getState(SYMBOL)
            .doOnSuccess { logger.info("LONG TP : S ${it.short_position}, L ${it.long_position}") }
            .flatMap { state: State -> closeAnyExistingLong(state) }

    fun shortTakeProfit(): Mono<State> =
        stateRepository.getState(SYMBOL)
            .doOnSuccess { logger.info("SHORT TP : S ${it.short_position}, L ${it.long_position}") }
            .flatMap { state: State -> closeAnyExistingShort(state) }


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
                .doOnSuccess { logger.info { "order response $it" } }
                // save Long order response to db
                .flatMap { orderResponse ->
                    tradeRepository.saveOrderResponse(orderResponse)
                        .flatMap { _ ->
                            // Long Order Response -> set on current short position
                            positionRepository.addCloseOrder(
                                documentId = shortPositionId,
                                orderResponse = orderResponse
                            )
                                // remove short position from state
                                .flatMap { stateRepository.updateState(state.copy(short_position = null)) }
                        }
                }
                .doOnSuccess { logger.info("Close Short Success") }
            false -> Mono.just(state)
        }
    }

    private fun closeAnyExistingLong(state: State): Mono<State> {
        val longPositionId = state.long_position
        return when (longPositionId != null) {
            // we have an existing long position, so place new Short Order
            true -> tradeClient.placeNewOrder(shortRequest())
                // save Short order response to db
                .flatMap { shortOrderResponse ->
                    tradeRepository.saveOrderResponse(shortOrderResponse)
                        .flatMap { _ ->
                            // Short Order Response -> set on current long position
                            positionRepository.addCloseOrder(
                                documentId = longPositionId,
                                orderResponse = shortOrderResponse
                            )
                                // remove long position from state
                                .flatMap { stateRepository.updateState(state.copy(long_position = null)) }
                        }
                }
                .doOnSuccess { logger.info("Close Long Success") }
            false -> Mono.just(state)
        }
    }

    private fun placeLongIfNoPosition(state: State): Mono<State> {
        val longPositionId = state.long_position
        return if (longPositionId == null) {
            // we have no long position, so place a long order
            tradeClient.placeNewOrder(longRequest())
                // save Long order response to db for records
                .flatMap { orderResponse ->
                    tradeRepository.saveOrderResponse(orderResponse)
                        .flatMap { _ ->
                            // Create a new Long Position
                            val newPosition = Position(exchange = "binance", symbol = "SOLBTC")
                            positionRepository.createPosition(newPosition)
                                // Open the Position with the current Long Order
                                .flatMap { documentId ->
                                    positionRepository.addOpenOrder(
                                        documentId = documentId,
                                        orderResponse = orderResponse
                                    )
                                        .flatMap { // add document ID of Long position to state
                                            stateRepository.updateState(state.copy(long_position = documentId))
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
                                // Open the Position with the current Short Order
                                .flatMap { documentId ->
                                    positionRepository.addOpenOrder(
                                        documentId = documentId,
                                        orderResponse = orderResponse
                                    )
                                        .flatMap { // add document ID of Short position to state
                                            stateRepository.updateState(state.copy(short_position = documentId))
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