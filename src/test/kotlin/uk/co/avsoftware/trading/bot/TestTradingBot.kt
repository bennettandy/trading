package uk.co.avsoftware.trading.bot

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import uk.co.avsoftware.trading.TestDataHelper
import uk.co.avsoftware.trading.client.binance.TradeClient
import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.client.binance.response.OrderResponse
import uk.co.avsoftware.trading.database.model.Position
import uk.co.avsoftware.trading.database.model.State
import uk.co.avsoftware.trading.repository.PositionRepository
import uk.co.avsoftware.trading.repository.StateRepository
import uk.co.avsoftware.trading.repository.TradeRepository

class TestTradingBot {

    val positionRepository: PositionRepository = mockk()
    val stateRepository: StateRepository = mockk()
    val tradeRepository: TradeRepository = mockk()
    val tradeClient: TradeClient = mockk()

    @Test
    fun testTradingBotLongTriggerWithOpenShortPosition(){

        val buyOrderResponseA: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.BUY)
        val buyOrderResponseB: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.BUY)

        val initialShortState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = null,
            short_position = "short_pos_123"
        )

        val intermediateIdleState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = null,
            short_position = null
        )

        val finalLongState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "new-position-doc-id",
            short_position = null
        )

        val bot = TradingBot(
            tradeClient = tradeClient,
            positionRepository = positionRepository,
            stateRepository = stateRepository,
            tradeRepository = tradeRepository
        )

        val position = Position(
            exchange = "binance",
            symbol = "SOLBTC",
        )

        every { tradeClient.placeNewOrder(any())} returns Mono.just(buyOrderResponseA) andThen Mono.just(buyOrderResponseB)
        every { stateRepository.getState("SOLBTC") } returns Mono.just(initialShortState)
        every { tradeRepository.saveOrderResponse(buyOrderResponseA)} returns Mono.just("document-id")
        every { positionRepository.addCloseOrder( documentId = "short_pos_123", orderResponse = buyOrderResponseA) } returns Mono.just(position)
        every { stateRepository.updateState(State(exchange = "binance", symbol="SOLBTC", long_position=null, short_position=null)) } returns Mono.just(intermediateIdleState)
        every { positionRepository.createPosition(Position(exchange = "binance", symbol = "SOLBTC")) } returns Mono.just("new-position-doc-id")
        every { positionRepository.addOpenOrder( documentId = "new-position-doc-id", orderResponse = buyOrderResponseB) } returns Mono.just(position)
        every { stateRepository.updateState(State(exchange = "binance", symbol="SOLBTC", long_position="new-position-doc-id", short_position=null)) } returns Mono.just(finalLongState)

        val botSrc: Mono<State> = bot.longTrigger()

        StepVerifier
            .create(botSrc)
            .expectNext(finalLongState)
            .expectComplete()
            .verify()

        // initial get state
        verify(exactly = 1) { stateRepository.getState("SOLBTC") }
        // closing shot and opening long - 2x long trades
        verify(exactly = 2) { tradeClient.placeNewOrder(any()) }
        // save short closing long and new long open trade
        verify(exactly = 2) { tradeRepository.saveOrderResponse(any()) }
        // position repository, close short position
        verify(exactly = 1) { positionRepository.addCloseOrder(any(), any()) }
        // create new empty position
        verify(exactly = 1) { positionRepository.createPosition(any()) }
        // position repository, open long position
        verify(exactly = 1) { positionRepository.addOpenOrder("new-position-doc-id", buyOrderResponseB) }
        // finally, update state with new long position
        verify(exactly = 1) { stateRepository.updateState(finalLongState) }
    }


}