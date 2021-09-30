package uk.co.avsoftware.trading.bot

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Service
class BotHandler( val tradingBot: TradingBot ) {

    private val logger = KotlinLogging.logger {}

    fun longTrigger(): Mono<ServerResponse> = tradingBot.longTrigger()
        .flatMap { ServerResponse.ok().build() }
        .onErrorResume {
            logger.error("ERROR: $it")
            ServerResponse.notFound().build()
        }

    fun shortTrigger(): Mono<ServerResponse> = tradingBot.shortTrigger()
        .flatMap { ServerResponse.ok().build() }
        .onErrorResume {
            logger.error("ERROR: $it")
            ServerResponse.notFound().build()
        }

    fun shortTakeProfit(): Mono<ServerResponse> = tradingBot.shortTakeProfit()
        .flatMap { ServerResponse.ok().build() }
        .onErrorResume {
            logger.error("ERROR: $it")
            ServerResponse.notFound().build()
        }

    fun longTakeProfit(): Mono<ServerResponse> = tradingBot.longTakeProfit()
        .flatMap { ServerResponse.ok().build() }
        .onErrorResume {
            logger.error("ERROR: $it")
            ServerResponse.notFound().build()
        }
}