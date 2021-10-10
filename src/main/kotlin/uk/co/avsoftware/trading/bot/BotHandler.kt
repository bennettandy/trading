package uk.co.avsoftware.trading.bot

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Service
class BotHandler( val tradingBot: TradingBot ) {

    private val logger = KotlinLogging.logger {}

    fun longTrigger(symbol: String): Mono<ServerResponse> = tradingBot.longTrigger(symbol)
        .checkpoint("Long Trigger")
        .flatMap { ServerResponse.ok().build() }
        .onErrorResume {
            logger.error("ERROR: $it", it)
            ServerResponse.notFound().build()
        }

    fun shortTrigger(symbol: String): Mono<ServerResponse> = tradingBot.shortTrigger(symbol)
        .checkpoint("Short Trigger")
        .flatMap { ServerResponse.ok().build() }
        .onErrorResume {
            logger.error("ERROR: $it", it)
            ServerResponse.notFound().build()
        }

    fun shortTakeProfit(symbol: String): Mono<ServerResponse> = tradingBot.shortTakeProfit(symbol)
        .checkpoint("Short Take Profit")
        .flatMap { ServerResponse.ok().build() }
        .onErrorResume {
            logger.error("ERROR: $it", it)
            ServerResponse.notFound().build()
        }

    fun longTakeProfit(symbol: String): Mono<ServerResponse> = tradingBot.longTakeProfit(symbol)
        .checkpoint("Long Take Profit")
        .flatMap { ServerResponse.ok().build() }
        .onErrorResume {
            logger.error("ERROR: $it", it)
            ServerResponse.notFound().build()
        }

    fun bullish(symbol: String): Mono<ServerResponse> = tradingBot.bullish(symbol)
    fun bearish(symbol: String): Mono<ServerResponse> = tradingBot.bearish(symbol)
}