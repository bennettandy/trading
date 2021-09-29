package uk.co.avsoftware.trading.bot

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import org.junit.rules.TestRule
import uk.co.avsoftware.trading.client.binance.SpotTradeClient
import uk.co.avsoftware.trading.client.binance.TradeClient
import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.client.binance.request.NewOrderRequest
import uk.co.avsoftware.trading.client.binance.response.OrderFill
import uk.co.avsoftware.trading.client.binance.response.OrderResponse
import uk.co.avsoftware.trading.database.model.State
import uk.co.avsoftware.trading.repository.PositionRepository
import uk.co.avsoftware.trading.repository.StateRepository
import uk.co.avsoftware.trading.repository.TradeRepository
import java.time.Duration

class TestTradingBot {

//    @get:Rule
//    var rule: TestRule = InstantTask

    val buyOrderResponse: OrderResponse = testOrderResponse(OrderSide.BUY)

    val tradeClient: TradeClient = object : TradeClient {
        override fun placeNewOrder(newOrderRequest: NewOrderRequest): Mono<OrderResponse> {
            return Mono.just(buyOrderResponse)
        }

    }
    val positionRepository: PositionRepository = mockk()
    val stateRepository: StateRepository = mockk()
    val tradeRepository: TradeRepository = mockk()

    @Test
    fun testTradingBotLongTrigger(){

        val state = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = null,
            short_position = "short_pos_123"
                )

        val bot = TradingBot(
            tradeClient = tradeClient,
            positionRepository = positionRepository,
            stateRepository = stateRepository,
            tradeRepository = tradeRepository
        )


        every { stateRepository.getState("SOLBTC") } returns Mono.just(state)

        var serverResponse: ServerResponse?
           // runBlocking {
                serverResponse = bot.longTrigger().block() //(Duration.ofSeconds(10))
           // }
        assertThat(serverResponse).isNotNull
        assertThat(serverResponse?.rawStatusCode()).isNotEqualTo(404)

        verify(exactly = 1) { stateRepository.getState("SOLBTC") }
    }

    private fun testOrderResponse(orderSide: OrderSide): OrderResponse {
        return OrderResponse(
            fills = listOf(
                OrderFill(
                    price = 10.0,
                    qty = 20.0,
                    commission = 0.0023,
                    commissionAsset = "BTC"
                )
            ),
            symbol = "SOLBTC",
            side = orderSide
        )
    }
}