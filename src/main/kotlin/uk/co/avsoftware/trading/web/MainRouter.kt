package uk.co.avsoftware.trading.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RequestPredicates.POST
import org.springframework.web.reactive.function.server.RequestPredicates.accept
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import uk.co.avsoftware.trading.bot.TradingBot

@Configuration(proxyBeanMethods = false)
class MainRouter() {

    @Bean
    fun route(tradingBot: TradingBot): RouterFunction<ServerResponse> =
        RouterFunctions.route(POST("/bot/long")
                .and(accept(MediaType.APPLICATION_JSON))) { tradingBot.longTrigger() }
            .andRoute(POST("/bot/short")
                .and(accept(MediaType.APPLICATION_JSON))) { tradingBot.shortTrigger() }
            .andRoute(POST("/bot/short/tp")
                .and(accept(MediaType.APPLICATION_JSON))) { tradingBot.shortTakeProfit() }
            .andRoute(POST("/bot/long/tp")
                .and(accept(MediaType.APPLICATION_JSON))) { tradingBot.longTakeProfit() }
            .andRoute(POST("/bot/bullish")
                .and(accept(MediaType.APPLICATION_JSON))) { tradingBot.bullish() }
            .andRoute(POST("/bot/bearish")
                .and(accept(MediaType.APPLICATION_JSON))) { tradingBot.bearish() }
}