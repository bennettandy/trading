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
import uk.co.avsoftware.trading.repository.StateRepository
import java.util.*

@Component
class TradingBot(
    val tradeClient: SpotTradeClient,
    val configurationRepository: ConfigurationRepository,
    val stateRepository: StateRepository
) {

    private val logger = KotlinLogging.logger {}

    @Value("\${sm://projects/1042444923718/secrets/binance-api-key}")
    lateinit var key: String

    fun longTrigger(): Mono<ServerResponse> =
        stateRepository.getState()
            .doOnSuccess { logger.info("LONG TRIGGER : S ${it.isShort}, L ${it.isLong}") }
            .flatMap { state: State -> closeExistingShort(state)
                .flatMap { newState -> placeLong( newState ) } }
            .flatMap { ServerResponse.ok().build() }
            .onErrorResume {
                    logger.error("ERROR: $it")
                    ServerResponse.notFound().build()
                }

    fun shortTrigger(): Mono<ServerResponse> =
        stateRepository.getState()
            .doOnSuccess { logger.info("SHORT TRIGGER : S ${it.isShort}, L ${it.isLong}") }
            .flatMap { state: State -> closeExistingLong(state)
                .flatMap { newState -> placeShort( newState ) } }
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
        return configurationRepository.getConfiguration()

            .doOnSuccess { logger.info { "Obtained Current Configuration $it" } }
            .doOnError { logger.warn { "Failed to obtain current configuration ${it.message}" } }

            .doOnSuccess { logger.info { "config $it"} }
            .map { config -> config.copy(version = config.version.inc(), field = Date().toString()) }

            .doOnSuccess { logger.info { "updated $it"} }
            .flatMap { updatedConfig -> configurationRepository.updateConfiguration(updatedConfig) }

//            .flatMap { stateRepository.getState().flatMap {
//                state -> stateRepository.updateState(state.copy(isLong = !state.isLong, isShort = !state.isShort))
//            } }

            .flatMap { configuration ->
                ServerResponse.ok().body(fromValue(configuration))
            }
            .onErrorResume { ServerResponse.badRequest().body(fromValue(ServiceError.from(it))) }

    }

    private fun closeExistingShort(state: State): Mono<State> {
        return when (state.isShort){
            true -> tradeClient.placeNewOrder(longRequest())
                .doOnSuccess{ logger.info("Close Short Success") }
                .flatMap { stateRepository.updateState(state.copy(isShort = false)) }
            false -> Mono.empty()
        }
    }

    private fun closeExistingLong(state: State): Mono<State> {
        return when (state.isLong){
            true -> tradeClient.placeNewOrder(shortRequest())
                .doOnSuccess{ logger.info("Close Long Success") }
                .flatMap { stateRepository.updateState(state.copy(isLong = false)) }
            false -> Mono.empty()
        }
    }

    private fun placeLong(state: State): Mono<State> {
        return tradeClient.placeNewOrder(longRequest())
                .doOnSuccess{ logger.info("Open Long Success") }
                .flatMap { stateRepository.updateState(state.copy(isLong = true)) }
    }

    private fun placeShort(state: State): Mono<State> {
        return tradeClient.placeNewOrder(shortRequest())
            .doOnSuccess{ logger.info("Open Short Success") }
            .flatMap { stateRepository.updateState(state.copy(isShort = true)) }
    }

    private fun longRequest() =
        NewOrderRequest(
            symbol = "SOLBTC",
            side = OrderSide.BUY,
            type = OrderType.MARKET,
            quantity = "10.0"
        )

    private fun shortRequest() =
        NewOrderRequest(
            symbol = "SOLBTC",
            side = OrderSide.SELL,
            type = OrderType.MARKET,
            quantity = "10.0"
        )
}