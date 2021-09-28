package uk.co.avsoftware.trading.bot

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.SpotTradeClient
import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.client.binance.model.trade.OrderType
import uk.co.avsoftware.trading.client.binance.request.NewOrderRequest
import uk.co.avsoftware.trading.client.binance.response.OrderFill
import uk.co.avsoftware.trading.client.binance.response.OrderResponse
import uk.co.avsoftware.trading.database.model.Position
import uk.co.avsoftware.trading.database.model.State
import uk.co.avsoftware.trading.repository.PositionRepository
import uk.co.avsoftware.trading.repository.StateRepository
import uk.co.avsoftware.trading.repository.TradeRepository

@Component
class TradingBot(
    val tradeClient: SpotTradeClient,
    val positionRepository: PositionRepository,
    val stateRepository: StateRepository,
    val tradeRepository: TradeRepository
) {

    private val logger = KotlinLogging.logger {}

    fun longTrigger(): Mono<ServerResponse> =
        stateRepository.getState(SYMBOL)
            .doOnSuccess { logger.info("LONG TRIGGER : S ${it.short_position}, L ${it.long_position}") }
            .flatMap { state: State ->
                closeExistingShort(state)
                    .flatMap { newState -> placeLong(newState) }
            }
            .flatMap { ServerResponse.ok().build() }
            .onErrorResume {
                logger.error("ERROR: $it")
                ServerResponse.notFound().build()
            }

    fun shortTrigger(): Mono<ServerResponse> =
        stateRepository.getState(SYMBOL)
            .doOnSuccess { logger.info("SHORT TRIGGER : S ${it.short_position}, L ${it.long_position}") }
            .flatMap { state: State ->
                closeExistingLong(state)
                    .flatMap { newState -> placeShort(newState) }
            }
            .flatMap { ServerResponse.ok().build() }
            .onErrorResume {
                logger.error("ERROR: $it")
                ServerResponse.notFound().build()
            }

    fun longTakeProfit(): Mono<ServerResponse> =
        stateRepository.getState(SYMBOL)
            .doOnSuccess { logger.info("LONG TP : S ${it.short_position}, L ${it.long_position}") }
            .flatMap { state: State -> closeExistingLong(state) }
            .flatMap { ServerResponse.ok().build() }
            .onErrorResume {
                logger.error("ERROR: $it")
                ServerResponse.notFound().build()
            }

    fun shortTakeProfit(): Mono<ServerResponse> =
        stateRepository.getState(SYMBOL)
            .doOnSuccess { logger.info("SHORT TP : S ${it.short_position}, L ${it.long_position}") }
            .flatMap { state: State -> closeExistingShort(state) }
            .flatMap { ServerResponse.ok().build() }
            .onErrorResume {
                logger.error("ERROR: $it")
                ServerResponse.notFound().build()
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

    private fun closeExistingShort(state: State): Mono<State> {
        val shortPositionId = state.short_position
        return when (shortPositionId != null) {
            // we have an existing short position, so place new Long Order
            true -> tradeClient.placeNewOrder(longRequest())
                    // save Long order response to db
                .flatMap { orderResponse -> tradeRepository.saveOrderResponse(orderResponse).map { orderResponse } }
                .flatMap { orderResponse: OrderResponse ->
                    // Long Order Response -> set on current short position
                    positionRepository.addCloseOrder( documentId = shortPositionId, orderResponse = orderResponse)
                            // remove short position from state
                        .flatMap { stateRepository.updateState(state.copy(short_position = null)) }
                }
                .doOnSuccess {  logger.info("Close Short Success") }
            false -> Mono.just(state)
        }
    }

    private fun closeExistingLong(state: State): Mono<State> {
        val longPositionId = state.long_position
        return when (longPositionId != null) {
            // we have an existing long position, so place new Short Order
            true -> tradeClient.placeNewOrder(shortRequest())
                // save Short order response to db
                .flatMap { orderResponse -> tradeRepository.saveOrderResponse(orderResponse).map { orderResponse } }
                .flatMap { orderResponse: OrderResponse ->
                    // Short Order Response -> set on current long position
                    positionRepository.addCloseOrder( documentId = longPositionId, orderResponse = orderResponse)
                        // remove long position from state
                        .flatMap { stateRepository.updateState(state.copy(long_position = null)) }
                }
                .doOnSuccess {  logger.info("Close Long Success") }
            false -> Mono.just(state)
        }
    }

    private fun placeLong(state: State): Mono<State> {
        val longPositionId = state.long_position
        return if (longPositionId == null) {
            // we have no long position, so place a long order
            tradeClient.placeNewOrder(longRequest())
                // save Long order response to db for records
                .flatMap { orderResponse -> tradeRepository.saveOrderResponse(orderResponse).map { orderResponse } }
                .flatMap { orderResponse: OrderResponse ->
                    // Create a new Long Position
                    val newPosition = Position(exchange = "binance", symbol = "SOLBTC")
                    positionRepository.createPosition(newPosition)
                        // Open the Position with the current Long Order
                        .flatMap { documentId ->
                            positionRepository.addOpenOrder(documentId = documentId, orderResponse = orderResponse)
                                .flatMap { // add document ID of Long position to state
                                    stateRepository.updateState(state.copy(long_position = documentId))
                                }
                        }
                }
                .doOnSuccess { logger.info("Open Long Success") }
        } else Mono.just(state) // already Long, do nothing
    }

    private fun placeShort(state: State): Mono<State> {
        val shortPositionId = state.short_position
        return if (shortPositionId == null) {
            // we have no short position, so place a short order
            tradeClient.placeNewOrder(shortRequest())
                // save Short order response to db for records
                .flatMap { orderResponse -> tradeRepository.saveOrderResponse(orderResponse).map { orderResponse } }
                .flatMap { orderResponse: OrderResponse ->
                    // Create a new Short Position
                    val newPosition = Position(exchange = "binance", symbol = "SOLBTC")
                    positionRepository.createPosition(newPosition)
                        // Open the Position with the current Short Order
                        .flatMap { documentId ->
                            positionRepository.addOpenOrder(documentId = documentId, orderResponse = orderResponse)
                                .flatMap { // add document ID of Short position to state
                                    stateRepository.updateState(state.copy(short_position = documentId))
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
        const val TRADE_AMOUNT = "30.0"
        const val SYMBOL = "SOLBTC"
    }

    private fun testOrderResponse(): OrderResponse {
        return OrderResponse(
            fills = listOf(
                OrderFill(
                    price = 10.0,
                    qty = 20.0,
                    commission = 0.0023,
                    commissionAsset = "BTC"
                )
            ),
            symbol = "SOLBTC"
        )
    }
}