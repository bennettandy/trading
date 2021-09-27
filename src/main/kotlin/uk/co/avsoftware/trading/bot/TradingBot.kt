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
import uk.co.avsoftware.trading.database.model.ServiceError
import uk.co.avsoftware.trading.database.model.State
import uk.co.avsoftware.trading.repository.ConfigurationRepository
import uk.co.avsoftware.trading.repository.PositionRepository
import uk.co.avsoftware.trading.repository.StateRepository
import java.util.*

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
            .map { position ->
                position.copy(
                    direction = "SELL",
                    open_commission = position.open_commission.plus("22"),
                    open_price = position.open_price.plus("1.020"),
                    open_quantity = position.open_quantity.plus("22")
                )
            }
            .flatMap { positionRepository.updatePosition(it) }

            .flatMap { position ->
                ServerResponse.ok().body(fromValue(position))
            }
            .onErrorResume { ServerResponse.badRequest().body(fromValue(ServiceError.from(it))) }
    }

    fun reset(): Mono<ServerResponse> {
        return positionRepository.getPosition()

            // update position
            .map { position ->
                position.copy(
                    direction = "SELL",
                    status = "CLOSED",
                    open_commission = emptyList(),
                    open_price = emptyList(),
                    open_quantity = emptyList(),
                    close_commission = emptyList(),
                    close_price = emptyList(),
                    close_quantity = emptyList()
                )
            }
            .flatMap { positionRepository.updatePosition(it) }

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
        return tradeClient.placeNewOrder(longRequest())
            .doOnSuccess { logger.info("Open Long Success") }
            .flatMap { stateRepository.updateState(state.copy(isLong = true)) }
    }

    private fun placeShort(state: State): Mono<State> {
        return tradeClient.placeNewOrder(shortRequest())
            .doOnSuccess { logger.info("Open Short Success") }
            .flatMap { stateRepository.updateState(state.copy(isShort = true)) }
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
        const val TRADE_AMOUNT = "42.0"
    }
}