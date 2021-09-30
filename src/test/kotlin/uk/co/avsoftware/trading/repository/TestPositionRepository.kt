package uk.co.avsoftware.trading.repository

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import uk.co.avsoftware.trading.TestDataHelper
import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.database.model.Position
import uk.co.avsoftware.trading.repository.service.PositionService

class TestPositionRepository {

    @Test
    fun test_add_opening_sell_order(){
        val positionService: PositionService = mockk()
        val openOrder = TestDataHelper.createOrderResponse(OrderSide.SELL, "openOrder")
        val positionOne = Position(exchange = "test-exchange", symbol = "SOLBTC")
        val updatedPosition = Position(exchange="test-exchange", name="", symbol="SOLBTC", direction="SELL", open_time_stamp=openOrder.transactTime?:0, open_order_id="${openOrder.orderId}", open_quantity=listOf(20.0), open_price=listOf(10.0), open_commission=listOf(0.0023), open_commission_currency="BTC", close_time_stamp=0, close_order_id="", close_quantity= emptyList(), close_price= emptyList(), close_commission= emptyList(), close_commission_currency="", open_qty=0.0, open_cost=0.0, open_comm=0.0, close_qty=0.0, close_cost=0.0, close_comm=0.0)
        every { positionService.retrievePosition("doc-one")} returns Mono.just(positionOne)
        every { positionService.retrievePosition("updated-pos-doc")} returns Mono.just(positionOne)
        every { positionService.updatePosition(updatedPosition)} returns Mono.just("updated-pos-doc")
        val sut = PositionRepository(positionService = positionService)

        val result: Mono<Position> = sut.addOpenOrder("doc-one", openOrder)

        StepVerifier
            .create(result)
            .expectNext(updatedPosition)
            .expectComplete()
            .verify()
    }

    @Test
    fun test_add_closing_buy_order(){
        val positionService: PositionService = mockk()
        val openOrder = TestDataHelper.createOrderResponse(OrderSide.BUY, "openOrder")
        val openShortPosition = Position(exchange="test-exchange", name="", symbol="SOLBTC", direction="SELL", open_time_stamp=0, open_order_id="0", open_quantity=listOf(20.0), open_price=listOf(10.0), open_commission=listOf(0.0023), open_commission_currency="BTC", close_time_stamp=0, close_order_id="", close_quantity= emptyList(), close_price= emptyList(), close_commission= emptyList(), close_commission_currency="", open_qty=0.0, open_cost=0.0, open_comm=0.0, close_qty=0.0, close_cost=0.0, close_comm=0.0)
        val closedPosition = Position(exchange="test-exchange", name="", symbol="SOLBTC", direction="BUY", open_time_stamp=0, open_order_id="0", open_quantity=listOf(20.0), open_price=listOf(10.0), open_commission=listOf(0.0023), open_commission_currency="BTC", close_time_stamp=openOrder.transactTime?:0, close_order_id="${openOrder.orderId}", close_quantity=listOf(20.0), close_price=listOf(10.0), close_commission=listOf(0.0023), close_commission_currency="BTC", open_qty=20.0, open_cost=200.0, open_comm=0.0023, close_qty=20.0, close_cost=200.0, close_comm=0.0023)
        every { positionService.retrievePosition("doc-one")} returns Mono.just(openShortPosition)
        every { positionService.retrievePosition("updated-pos-doc")} returns Mono.just(closedPosition)
        every { positionService.updatePosition(openShortPosition)} returns Mono.just("updated-pos-doc")
        val sut = PositionRepository(positionService = positionService)

        val result: Mono<Position> = sut.addCloseOrder("doc-one", openOrder)

        StepVerifier
            .create(result)
            .expectNext(closedPosition)
            .expectComplete()
            .verify()

    }
}