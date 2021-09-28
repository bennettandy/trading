package uk.co.avsoftware.trading.bot

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.SpotTradeClient
import uk.co.avsoftware.trading.database.model.State
import uk.co.avsoftware.trading.repository.PositionRepository
import uk.co.avsoftware.trading.repository.StateRepository
import uk.co.avsoftware.trading.repository.TradeRepository

class TestTradingBot {

    val tradeClient: SpotTradeClient = mockk()
    val positionRepository: PositionRepository = mockk()
    val stateRepository: StateRepository = mockk()
    val tradeRepository: TradeRepository = mockk()

    @Test
    fun testTradingBotLongTrigger(){

        val state: State = State (
            exchange = "binance",
            symbol = "SOLBTC",
            long_position = null,
            short_position = null
                )

        val bot = TradingBot(
            tradeClient = tradeClient,
            positionRepository = positionRepository,
            stateRepository = stateRepository,
            tradeRepository = tradeRepository
        )

        every { stateRepository.getState("SOLBTC") } returns Mono.just(state)

        bot.longTrigger()
    }
}