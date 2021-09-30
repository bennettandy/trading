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
    fun test_add_open_order(){

        val positionService: PositionService = mockk()
        val openOrder = TestDataHelper.createOrderResponse(OrderSide.SELL)
        val positionOne = Position(symbol = "SOLBTC")
        val updatedPosition = Position(exchange="", name="", symbol="SOLBTC", direction="", open_time_stamp=0, open_order_id="", open_quantity=listOf(20.0), open_price=listOf(10.0), open_commission=listOf(0.0023), open_commission_currency="", close_time_stamp=0, close_order_id="", close_quantity= emptyList(), close_price= emptyList(), close_commission= emptyList(), close_commission_currency="", open_qty=0.0, open_cost=0.0, open_comm=0.0, close_qty=0.0, close_cost=0.0, close_comm=0.0)
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
}