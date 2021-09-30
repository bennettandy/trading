package uk.co.avsoftware.trading.bot

import io.mockk.every
import io.mockk.mockk
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
    fun testTradingBotLongTrigger(){

        val buyOrderResponse: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.BUY)

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

        every { tradeClient.placeNewOrder(any())} returns Mono.just(buyOrderResponse)
        every { stateRepository.getState("SOLBTC") } returns Mono.just(initialShortState)
        every { tradeRepository.saveOrderResponse(buyOrderResponse)} returns Mono.just("document-id")
        every { positionRepository.addCloseOrder( documentId = "short_pos_123", orderResponse = buyOrderResponse) } returns Mono.just(position)
        every { stateRepository.updateState(State(exchange = "binance", symbol="SOLBTC", long_position=null, short_position=null)) } returns Mono.just(intermediateIdleState)
        every { positionRepository.createPosition(Position(exchange = "binance", symbol = "SOLBTC")) } returns Mono.just("new-position-doc-id")
        every { positionRepository.addOpenOrder( documentId = "new-position-doc-id", orderResponse = buyOrderResponse) } returns Mono.just(position)
        every { stateRepository.updateState(State(exchange = "binance", symbol="SOLBTC", long_position="new-position-doc-id", short_position=null)) } returns Mono.just(finalLongState)

        val botSrc: Mono<State> = bot.longTrigger()

        StepVerifier
            .create(botSrc)
            .expectNext(finalLongState)
            .expectComplete()
            .verify()

       // verify(exactly = 1) { stateRepository.getState("SOLBTC") }
    }


}