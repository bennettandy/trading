package uk.co.avsoftware.trading.bot

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromValue
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.SpotTradeClient
import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.client.binance.model.trade.OrderType
import uk.co.avsoftware.trading.client.binance.request.NewOrderRequest
import uk.co.avsoftware.trading.client.binance.response.OrderFill
import uk.co.avsoftware.trading.client.binance.response.OrderResponse
import uk.co.avsoftware.trading.database.model.ServiceError
import uk.co.avsoftware.trading.database.model.State
import uk.co.avsoftware.trading.repository.PositionRepository
import uk.co.avsoftware.trading.repository.StateRepository

@Component
class TradingBot(
    val tradeClient: SpotTradeClient,
    val positionRepository: PositionRepository,
    val stateRepository: StateRepository
) {

    private val logger = KotlinLogging.logger {}

    @Value("\${sm://projects/1042444923718/secrets/binance-api-key}")
    lateinit var key: String

    fun longTrigger(): Mono<ServerResponse> =
        stateRepository.getState()
            .doOnSuccess { logger.info("LONG TRIGGER : S ${it.isShort}, L ${it.isLong}") }
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
        stateRepository.getState()
            .doOnSuccess { logger.info("SHORT TRIGGER : S ${it.isShort}, L ${it.isLong}") }
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
        stateRepository.getState()
            .doOnSuccess { logger.info("LONG TP : S ${it.isShort}, L ${it.isLong}") }
            .flatMap { state: State -> closeExistingLong(state) }
            .flatMap { ServerResponse.ok().build() }
            .onErrorResume {
                logger.error("ERROR: $it")
                ServerResponse.notFound().build()
            }

    fun shortTakeProfit(): Mono<ServerResponse> =
        stateRepository.getState()
            .doOnSuccess { logger.info("SHORT TP : S ${it.isShort}, L ${it.isLong}") }
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
        return positionRepository.getPosition()

            // update position
            .flatMap { positionRepository.addCloseOrder( orderResponse =  testOrderResponse()) }

            .flatMap { position ->
                ServerResponse.ok().body(fromValue(position))
            }
            .onErrorResume { ServerResponse.badRequest().body(fromValue(ServiceError.from(it))) }
    }

    fun reset(): Mono<ServerResponse> {
        return positionRepository.getPosition()

            // update position
            // update position
            .flatMap { positionRepository.addOpenOrder( orderResponse =  testOrderResponse()) }

            .flatMap { position ->
                ServerResponse.ok().body(fromValue(position))
            }
            .onErrorResume { ServerResponse.badRequest().body(fromValue(ServiceError.from(it))) }
    }

    fun clear(): Mono<ServerResponse> {
        return positionRepository.getPosition()

            // update position
            .map { it.copy(
                open_quantity = emptyList(),
                open_price = emptyList(),
                open_commission = emptyList(),
                close_quantity = emptyList(),
                close_price = emptyList(),
                close_commission = emptyList(),
                status = "CLOSED"
            ) }
            // update position
            .flatMap { positionRepository.updatePosition( it) }

            .flatMap { position ->
                ServerResponse.ok().body(fromValue(position))
            }
            .onErrorResume { ServerResponse.badRequest().body(fromValue(ServiceError.from(it))) }
    }

    private fun closeExistingShort(state: State): Mono<State> {
        return when (state.isShort) {
            true -> tradeClient.placeNewOrder(longRequest())
                .doOnSuccess { logger.info("Close Short Success") }
                .flatMap { stateRepository.updateState(state.copy(isShort = false)) }
            false -> Mono.just(state)
        }
    }

    private fun closeExistingLong(state: State): Mono<State> {
        return when (state.isLong) {
            true -> tradeClient.placeNewOrder(shortRequest())
                .doOnSuccess { logger.info("Close Long Success") }
                .flatMap { stateRepository.updateState(state.copy(isLong = false)) }
            false -> Mono.just(state)
        }
    }

    private fun placeLong(state: State): Mono<State> {
        if (!state.isLong) {
            return tradeClient.placeNewOrder(longRequest())
                .doOnSuccess { logger.info("Open Long Success") }
                .flatMap { stateRepository.updateState(state.copy(isLong = true)) }
        }
        else return Mono.just(state)
    }

    private fun placeShort(state: State): Mono<State> {
        if (!state.isShort) {
            return tradeClient.placeNewOrder(shortRequest())
                .doOnSuccess { logger.info("Open Short Success") }
                .flatMap { stateRepository.updateState(state.copy(isShort = true)) }
        }
        else return Mono.just(state)
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