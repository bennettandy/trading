package uk.co.avsoftware.trading.bot

import com.google.cloud.firestore.DocumentReference
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Ignore
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import uk.co.avsoftware.trading.TestDataHelper
import uk.co.avsoftware.trading.client.binance.BinanceTradeClient
import uk.co.avsoftware.trading.client.binance.model.OrderSide
import uk.co.avsoftware.trading.client.binance.model.OrderResponse
import uk.co.avsoftware.trading.client.bybit.BybitTradeClient
import uk.co.avsoftware.trading.database.model.Direction
import uk.co.avsoftware.trading.database.model.SignalEvent
import uk.co.avsoftware.trading.database.model.State
import uk.co.avsoftware.trading.repository.OpenTradeRepository
import uk.co.avsoftware.trading.repository.StateRepository
import uk.co.avsoftware.trading.repository.service.OpenTradeService

class TestTradingBot {

    private val stateRepository: StateRepository = mockk()
    private val openTradeRepository: OpenTradeRepository = mockk()
    private val tradeClient: BinanceTradeClient = mockk()
    private val bybitClient: BybitTradeClient = mockk()
    private val openTradeService: OpenTradeService = mockk()

    @Test
    fun testSimpleLongStrategy(){

        val orderDocumentReference: DocumentReference = mockk()
        val orderDocumentReferenceA: DocumentReference = mockk()
        val orderDocumentReferenceB: DocumentReference = mockk()

        val openSellResponse: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.SELL, "shortA")
        val buyOrderResponseA: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.BUY, "orderA")
        val buyOrderResponseB: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.BUY, "orderB")

        val initialClosedState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            open_position = null,
            position_size = 5.0,
            remaining_position = 0.0,
            direction = Direction.NONE,
            timestamp = 0L,
            last_event = SignalEvent.NONE
        )

        val eventUpdatedState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            open_position = null,
            position_size = 5.0,
            remaining_position = 0.0,
            direction = Direction.NONE,
            timestamp = 0L,
            last_event = SignalEvent.LONG
        )

        val finalLongState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            open_position = orderDocumentReferenceB,
            position_size = 5.0,
            remaining_position = 5.0,
            direction = Direction.LONG,
            timestamp = 0L,
            last_event = SignalEvent.LONG
        )

        val bot = TradingBot(
            binanceTradeClient = tradeClient,
            bybitTradeClient = bybitClient,
            stateRepository = stateRepository,
            openTradeRepository = openTradeRepository,
            openTradeService = openTradeService
        )

        // update state with event
        every { stateRepository.updateStateWithEvent("SOLBTC", SignalEvent.LONG)} returns Mono.just(eventUpdatedState)

        // close short and complete trade orders
        every { tradeClient.placeNewOrder(any())} returns Mono.just(buyOrderResponseA) andThen Mono.just(buyOrderResponseB)

        // initial state
        every { stateRepository.getState("SOLBTC") } returns Mono.just(initialClosedState)

        // open new long
        every { stateRepository.updateState(finalLongState)} returns Mono.just(finalLongState)

        val botSrc: Mono<State> = bot.longTrigger("SOLBTC")

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
        // save new long trade
        // completed trade repository, close short position
        // finally, update state with new long position
        verify(exactly = 1) { stateRepository.updateState(finalLongState) }
    }

    @Test
    @Ignore
    fun testTradingBotLongTriggerWithOpenShortPosition(){

        val orderDocumentReference: DocumentReference = mockk()
        val orderDocumentReferenceA: DocumentReference = mockk()
        val orderDocumentReferenceB: DocumentReference = mockk()

        val openSellResponse: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.SELL, "shortA")
        val buyOrderResponseA: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.BUY, "orderA")
        val buyOrderResponseB: OrderResponse = TestDataHelper.createOrderResponse(OrderSide.BUY, "orderB")

        val initialShortState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            open_position = orderDocumentReference,
            position_size = 1.0,
            remaining_position = 1.0,
            direction = Direction.SHORT,
            timestamp = 0L
        )

        val finalLongState = State (
            exchange = "binance",
            symbol = "SOLBTC",
            open_position = orderDocumentReferenceB,
            position_size = 1.0,
            remaining_position = 1.0,
            direction = Direction.LONG,
            timestamp = 0L
        )

        val bot = TradingBot(
            binanceTradeClient = tradeClient,
            bybitTradeClient = bybitClient,
            stateRepository = stateRepository,
            openTradeRepository = openTradeRepository,
            openTradeService = openTradeService
        )

        // close short and complete trade orders
        every { tradeClient.placeNewOrder(any())} returns Mono.just(buyOrderResponseA) andThen Mono.just(buyOrderResponseB)

        // initial state
        every { stateRepository.getState("SOLBTC") } returns Mono.just(initialShortState)
//        every { stateRepository.getTrade(initialShortState) } returns Mono.just(openSellResponse)

        // open new long
        every { stateRepository.updateState(finalLongState)} returns Mono.just(finalLongState)

        val botSrc: Mono<State> = bot.longTrigger("SOLBTC")

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
        // save new long trade
        // completed trade repository, close short position
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