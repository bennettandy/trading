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
import uk.co.avsoftware.trading.client.bybit.BybitTradeClient
import uk.co.avsoftware.trading.database.model.*
import uk.co.avsoftware.trading.repository.OpenTradeRepository
import uk.co.avsoftware.trading.repository.StateRepository
import uk.co.avsoftware.trading.repository.service.OpenTradeService
import java.time.Instant

@Component
class TradingBot(
    val binanceTradeClient: BinanceTradeClient,
    val bybitTradeClient: BybitTradeClient,
    val stateRepository: StateRepository,
    val openTradeRepository: OpenTradeRepository,
    val openTradeService: OpenTradeService,
) {

    private val logger = KotlinLogging.logger {}

    fun longTrigger(symbol: String): Mono<State> =
        stateRepository.updateStateWithEvent(symbol, SignalEvent.LONG)
            .flatMap { state ->
                if (state.direction != Direction.LONG) {
                    logger.info { "passed filter $symbol NOT already long $state" }
                    binanceTradeClient.placeNewOrder(longRequest(state.position_size, state.symbol))
                        .doOnSuccess { logger.info { "save $symbol long order response ${state.position_size}" } }
                        .flatMap { orderResponse ->
                            openTradeRepository.createNewOpenTrade(orderResponse, state, Direction.LONG)
                                .flatMap { openTradeDocReference ->
                                    stateRepository.updateState(
                                        state.copy(
                                            open_position = openTradeDocReference,
                                            // fully open position
                                            remaining_position = state.position_size,
                                            direction = Direction.LONG // set direction
                                        )
                                    )
                                }
                        }
                } else {
                    logger.info { "Already Long $symbol already long $state" }
                    Mono.just(state)
                }
            }


    fun shortTrigger(symbol: String): Mono<State> =
        stateRepository.updateStateWithEvent(symbol, SignalEvent.SHORT)
            .flatMap { state ->
                if (state.direction == Direction.LONG) {
                    logger.info { "passed filter $symbol We are currently LONG $state" }
                    // close remaining position
                    binanceTradeClient.placeNewOrder(shortRequest(state.remaining_position, state.symbol))
                        .flatMap { closingOrder: OrderResponse ->
                            openTradeRepository.addClosingOrder(closingOrder, state, direction = Direction.SHORT)
                                .flatMap { openTradeRepository.closeOrder(state) }
                                .flatMap {
                                    stateRepository.updateState(
                                        state.copy(
                                            open_position = null,
                                            remaining_position = 0.0,
                                            direction = Direction.NONE
                                        )
                                    )
                                }
                        }
                } else {
                    logger.info { "Already Long $symbol already long $state" }
                    Mono.just(state)
                }
            }

    fun longTakeProfit(symbol: String): Mono<State> =
        stateRepository.updateStateWithEvent(symbol, SignalEvent.SHORT)
            .flatMap { state ->
                if (state.direction == Direction.LONG) {
                    logger.info { "passed filter $symbol is LONG $state" }
                    // close portion of remaining position
                    val tradeSize = state.remaining_position / TAKE_PROFIT_DIVISOR
                    logger.info { "Selling $tradeSize $symbol" }
                    binanceTradeClient.placeNewOrder(shortRequest(tradeSize, state.symbol))
                        .flatMap { closingOrder ->
                            openTradeRepository.addClosingOrder(closingOrder, state, direction = Direction.SHORT)
                                // Take Profit doesn't close the order here
                                .flatMap {
                                    stateRepository.updateState(
                                        state.copy(
                                            remaining_position = state.remaining_position - tradeSize,
                                        )
                                    )
                                }
                        }
                } else {
                    logger.info { "Ignoring $symbol Not currently long $state" }
                    Mono.just(state)
                }
            }

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
        return openTradeService.saveNewOpenTrade(
            OpenTrade(
                exchange = "binance",
                symbol = "SOLBTC",
                direction = Direction.LONG,
                open_fills = listOf(
                    TradeFill(
                        timestamp = Instant.now().toEpochMilli(),
                        symbol = "SOL",
                        price = 120.0,
                        qty = 2.0,
                        commission = 0.0002,
                        commissionAsset = "BNB"
                    ),
                    TradeFill(
                        timestamp = Instant.now().toEpochMilli(),
                        symbol = "SOL",
                        price = 121.0,
                        qty = 5.0,
                        commission = 0.0003,
                        commissionAsset = "BNB"
                    )
                )
            )
        )
            .flatMap { ServerResponse.ok().build() }
    }

    fun testOpen(): Mono<ServerResponse> {
        return ServerResponse.ok().build()
    }

    fun testClose(): Mono<ServerResponse> {
        return ServerResponse.ok().build()
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