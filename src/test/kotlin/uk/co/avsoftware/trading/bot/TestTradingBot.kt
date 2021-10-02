package uk.co.avsoftware.trading.bot

import com.google.cloud.firestore.DocumentReference
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import uk.co.avsoftware.trading.TestDataHelper
import uk.co.avsoftware.trading.client.binance.TradeClient
import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.client.binance.response.OrderResponse
import uk.co.avsoftware.trading.database.model.State
import uk.co.avsoftware.trading.repository.CompletedTradeRepository
import uk.co.avsoftware.trading.repository.StateRepository
import uk.co.avsoftware.trading.repository.TradeRepository

class TestTradingBot {

    private val completedTradeRepository: CompletedTradeRepository = mockk()
    private val stateRepository: StateRepository = mockk()
    private val tradeRepository: TradeRepository = mockk()
    private val tradeClient: TradeClient = mockk()


    @Test
    fun testTradingBotLongTriggerWithOpenShortPosition(){

        val orderDocumentReferenceA: DocumentReference = mockk()
        val orderDocumentReferenceB: DocumentReference = mockk()

        val openSellResponse: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.SELL, "shortA")
        val buyOrderResponseA: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.BUY, "orderA")
        val buyOrderResponseB: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.BUY, "orderB")

        val initialShortState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            open_position = orderDocumentReferenceA
        )

        val finalLongState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            open_position = orderDocumentReferenceB
        )

        val bot = TradingBot(
            tradeClient = tradeClient,
            completedTradeRepository = completedTradeRepository,
            stateRepository = stateRepository,
            tradeRepository = tradeRepository
        )

        // close short and complete trade
        every { tradeClient.placeNewOrder(any())} returns Mono.just(buyOrderResponseA) andThen Mono.just(buyOrderResponseB)
        every { stateRepository.getState("SOLBTC") } returns Mono.just(initialShortState)
        every { stateRepository.getTrade(initialShortState) } returns Mono.just(openSellResponse)
        every { tradeRepository.saveOrderResponse(buyOrderResponseA)}
        every { tradeRepository.saveOrderResponse(buyOrderResponseA)} returns Mono.just(orderDocumentReferenceA)
        every { completedTradeRepository.createCompletedTrade(openSellResponse, buyOrderResponseA)} returns Mono.just("doc-id-string")

        // open new long
        every { tradeRepository.saveOrderResponse(buyOrderResponseB)} returns Mono.just(orderDocumentReferenceB)
        every { stateRepository.updateState(finalLongState)} returns Mono.just(finalLongState)

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
        // save short closing long trade
        verify(exactly = 1) { tradeRepository.saveOrderResponse(buyOrderResponseA) }
        // save new long trade
        verify(exactly = 1) { tradeRepository.saveOrderResponse(buyOrderResponseB) }
        // completed trade repository, close short position
        verify(exactly = 1) { completedTradeRepository.createCompletedTrade(openSellResponse, buyOrderResponseA) }
        // finally, update state with new long position
        verify(exactly = 1) { stateRepository.updateState(finalLongState) }
    }

    /*
    @Test
    fun testTradingBotLongTriggerWithNoShortPosition(){

        val buyOrderResponseB: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.BUY, "orderB")

        val initialShortState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "",
            short_position = ""
        )

        val finalLongState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "new-position-doc-id",
            short_position = ""
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

        every { tradeClient.placeNewOrder(any())} returns Mono.just(buyOrderResponseB)
        every { stateRepository.getState("SOLBTC") } returns Mono.just(initialShortState)
        every { tradeRepository.saveOrderResponse(buyOrderResponseB)} returns Mono.just("document-id")
        every { positionRepository.createPosition(Position(exchange = "binance", symbol = "SOLBTC")) } returns Mono.just("new-position-doc-id")
        every { positionRepository.addOpenOrder( documentId = "new-position-doc-id", orderResponse = buyOrderResponseB) } returns Mono.just(position)
        every { stateRepository.updateState(State(exchange = "binance", symbol="SOLBTC", long_position="new-position-doc-id", short_position="")) } returns Mono.just(finalLongState)

        val botSrc: Mono<State> = bot.longTrigger()

        StepVerifier
            .create(botSrc)
            .expectNext(finalLongState)
            .expectComplete()
            .verify()

        // initial get state
        verify(exactly = 1) { stateRepository.getState("SOLBTC") }
        // create the new long position
        verify(exactly = 1) { positionRepository.createPosition(any()) }
        // position repository, open long position
        verify(exactly = 1) { positionRepository.addOpenOrder("new-position-doc-id", buyOrderResponseB) }
        // finally, update state with new long position
        verify(exactly = 1) { stateRepository.updateState(finalLongState) }
    }

    @Test
    fun testTradingBotShortTriggerWithOpenLongPosition(){

        val buyOrderResponseA: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.SELL, "orderA")
        val buyOrderResponseB: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.SELL, "orderB")

        val initialLongState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "long_pos_123",
            short_position = ""
        )

        val intermediateIdleState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "",
            short_position = ""
        )

        val finalShortState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "",
            short_position = "new-short-position-doc"
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
        every { stateRepository.getState("SOLBTC") } returns Mono.just(initialLongState)
        every { tradeRepository.saveOrderResponse(buyOrderResponseA)} returns Mono.just("document-id")
        every { positionRepository.addCloseOrder( documentId = "long_pos_123", orderResponse = buyOrderResponseA) } returns Mono.just(position)
        every { stateRepository.updateState(State(exchange = "binance", symbol="SOLBTC", long_position="", short_position="")) } returns Mono.just(intermediateIdleState)
        every { positionRepository.createPosition(Position(exchange = "binance", symbol = "SOLBTC")) } returns Mono.just("new-short-position-doc")
        every { tradeRepository.saveOrderResponse(buyOrderResponseB)} returns Mono.just("document-id")
        every { positionRepository.addOpenOrder( documentId = "new-short-position-doc", orderResponse = buyOrderResponseB) } returns Mono.just(position)
        every { stateRepository.updateState(State(exchange = "binance", symbol="SOLBTC", long_position="", short_position="new-short-position-doc")) } returns Mono.just(finalShortState)

        val botSrc: Mono<State> = bot.shortTrigger()

        StepVerifier
            .create(botSrc)
            .expectNext(finalShortState)
            .expectComplete()
            .verify()

        // initial get state
        verify(exactly = 1) { stateRepository.getState("SOLBTC") }
        // closing shot and opening long - 2x long trades
        verify(exactly = 2) { tradeClient.placeNewOrder(any()) }
        // save short closing long trade
        verify(exactly = 1) { tradeRepository.saveOrderResponse(buyOrderResponseA) }
        // save new long trade
        verify(exactly = 1) { tradeRepository.saveOrderResponse(buyOrderResponseB) }
        // position repository, close short position
        verify(exactly = 1) { positionRepository.addCloseOrder(any(), any()) }
        // create new empty position
        verify(exactly = 1) { positionRepository.createPosition(any()) }
        // position repository, open long position
        verify(exactly = 1) { positionRepository.addOpenOrder("new-short-position-doc", buyOrderResponseB) }
        // finally, update state with new long position
        verify(exactly = 1) { stateRepository.updateState(finalShortState) }
    }

    @Test
    fun testTradingBotShortTriggerWithNoLongPosition(){

        val buyOrderResponseB: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.SELL, "orderB")

        val initialLongState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "",
            short_position = ""
        )

        val finalShortState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "",
            short_position = "new-short-position-doc"
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

        every { tradeClient.placeNewOrder(any())} returns Mono.just(buyOrderResponseB)
        every { stateRepository.getState("SOLBTC") } returns Mono.just(initialLongState)
        every { positionRepository.createPosition(Position(exchange = "binance", symbol = "SOLBTC")) } returns Mono.just("new-short-position-doc")
        every { tradeRepository.saveOrderResponse(buyOrderResponseB)} returns Mono.just("document-id")
        every { positionRepository.addOpenOrder( documentId = "new-short-position-doc", orderResponse = buyOrderResponseB) } returns Mono.just(position)
        every { stateRepository.updateState(State(exchange = "binance", symbol="SOLBTC", long_position="", short_position="new-short-position-doc")) } returns Mono.just(finalShortState)

        val botSrc: Mono<State> = bot.shortTrigger()

        StepVerifier
            .create(botSrc)
            .expectNext(finalShortState)
            .expectComplete()
            .verify()

        // initial get state
        verify(exactly = 1) { stateRepository.getState("SOLBTC") }
        // create new empty position
        verify(exactly = 1) { positionRepository.createPosition(any()) }
        // position repository, open long position
        verify(exactly = 1) { positionRepository.addOpenOrder("new-short-position-doc", buyOrderResponseB) }
        // finally, update state with new long position
        verify(exactly = 1) { stateRepository.updateState(finalShortState) }
    }

    @Test
    fun testTradingBotShortTP(){

        val buyOrderResponseA: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.BUY, "orderA")

        val initialShortState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "",
            short_position = "short_pos_123"
        )

        val finalState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "",
            short_position = ""
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

        every { tradeClient.placeNewOrder(any())} returns Mono.just(buyOrderResponseA)
        every { stateRepository.getState("SOLBTC") } returns Mono.just(initialShortState)
        every { tradeRepository.saveOrderResponse(buyOrderResponseA)} returns Mono.just("document-id")
        every { positionRepository.addCloseOrder( documentId = "short_pos_123", orderResponse = buyOrderResponseA) } returns Mono.just(position)
        every { stateRepository.updateState(State(exchange = "binance", symbol="SOLBTC", long_position="", short_position="")) } returns Mono.just(finalState)

        val botSrc: Mono<State> = bot.shortTakeProfit()

        StepVerifier
            .create(botSrc)
            .expectNext(finalState)
            .expectComplete()
            .verify()

        // initial get state
        verify(exactly = 1) { stateRepository.getState("SOLBTC") }
        // closing shot and opening long - 2x long trades
        verify(exactly = 1) { tradeClient.placeNewOrder(any()) }
        // save short closing long trade
        verify(exactly = 1) { tradeRepository.saveOrderResponse(buyOrderResponseA) }
        // finally, update state with new long position
        verify(exactly = 1) { stateRepository.updateState(finalState) }
    }

    @Test
    fun testTradingBotLongTP(){

        val sellOrderResponseA: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.SELL, "orderA")

        val initialShortState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "long_pos_123",
            short_position = ""
        )

        val finalState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "",
            short_position = ""
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

        every { tradeClient.placeNewOrder(any())} returns Mono.just(sellOrderResponseA)
        every { stateRepository.getState("SOLBTC") } returns Mono.just(initialShortState)
        every { tradeRepository.saveOrderResponse(sellOrderResponseA)} returns Mono.just("document-id")
        every { positionRepository.addCloseOrder( documentId = "long_pos_123", orderResponse = sellOrderResponseA) } returns Mono.just(position)
        every { stateRepository.updateState(State(exchange = "binance", symbol="SOLBTC", long_position="", short_position="")) } returns Mono.just(finalState)

        val botSrc: Mono<State> = bot.longTakeProfit()

        StepVerifier
            .create(botSrc)
            .expectNext(finalState)
            .expectComplete()
            .verify()

        // initial get state
        verify(exactly = 1) { stateRepository.getState("SOLBTC") }
        // closing shot and opening long - 2x long trades
        verify(exactly = 1) { tradeClient.placeNewOrder(any()) }
        // save short closing long trade
        verify(exactly = 1) { tradeRepository.saveOrderResponse(sellOrderResponseA) }
        // finally, update state with new long position
        verify(exactly = 1) { stateRepository.updateState(finalState) }
    }

    @Test
    fun testTradingBotLongTP_noLong(){

        val sellOrderResponseA: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.SELL, "orderA")

        val initialShortState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "",
            short_position = ""
        )

        val finalState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "",
            short_position = ""
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

        every { tradeClient.placeNewOrder(any())} returns Mono.just(sellOrderResponseA)
        every { stateRepository.getState("SOLBTC") } returns Mono.just(initialShortState)

        val botSrc: Mono<State> = bot.longTakeProfit()

        StepVerifier
            .create(botSrc)
            .expectNext(finalState)
            .expectComplete()
            .verify()

        // initial get state
        verify(exactly = 1) { stateRepository.getState("SOLBTC") }
    }

    @Test
    fun testTradingBotShortTP_noShort(){

        val sellOrderResponseA: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.SELL, "orderA")

        val initialShortState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "",
            short_position = ""
        )

        val finalState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "",
            short_position = ""
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

        every { tradeClient.placeNewOrder(any())} returns Mono.just(sellOrderResponseA)
        every { stateRepository.getState("SOLBTC") } returns Mono.just(initialShortState)

        val botSrc: Mono<State> = bot.shortTakeProfit()

        StepVerifier
            .create(botSrc)
            .expectNext(finalState)
            .expectComplete()
            .verify()

        // initial get state
        verify(exactly = 1) { stateRepository.getState("SOLBTC") }
    }

    @Test
    fun testTradingBotLong_alreadyLong(){

        val sellOrderResponseA: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.SELL, "orderA")

        val initialShortState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "existing-long-123",
            short_position = ""
        )

        val finalState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "existing-long-123",
            short_position = ""
        )

        val bot = TradingBot(
            tradeClient = tradeClient,
            positionRepository = positionRepository,
            stateRepository = stateRepository,
            tradeRepository = tradeRepository
        )

        every { tradeClient.placeNewOrder(any())} returns Mono.just(sellOrderResponseA)
        every { stateRepository.getState("SOLBTC") } returns Mono.just(initialShortState)

        val botSrc: Mono<State> = bot.longTrigger()

        StepVerifier
            .create(botSrc)
            .expectNext(finalState)
            .expectComplete()
            .verify()

        // initial get state
        verify(exactly = 1) { stateRepository.getState("SOLBTC") }
    }

    @Test
    fun testTradingBotShort_alreadyShort(){

        val sellOrderResponseA: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.SELL, "orderA")

        val initialShortState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "",
            short_position = "existing-short-123"
        )

        val finalState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = "",
            short_position = "existing-short-123"
        )

        val bot = TradingBot(
            tradeClient = tradeClient,
            positionRepository = positionRepository,
            stateRepository = stateRepository,
            tradeRepository = tradeRepository
        )

        every { tradeClient.placeNewOrder(any())} returns Mono.just(sellOrderResponseA)
        every { stateRepository.getState("SOLBTC") } returns Mono.just(initialShortState)

        val botSrc: Mono<State> = bot.shortTrigger()

        StepVerifier
            .create(botSrc)
            .expectNext(finalState)
            .expectComplete()
            .verify()

        // initial get state
        verify(exactly = 1) { stateRepository.getState("SOLBTC") }
    }


     */
}