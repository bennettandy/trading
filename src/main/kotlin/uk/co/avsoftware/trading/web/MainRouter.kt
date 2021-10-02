package uk.co.avsoftware.trading.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RequestPredicates.*
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import uk.co.avsoftware.trading.bot.BotHandler
import uk.co.avsoftware.trading.bot.TradingBot

@Configuration(proxyBeanMethods = false)
class MainRouter() {

    @Bean
    fun route(tradingBot: TradingBot, botHandler: BotHandler): RouterFunction<ServerResponse> =
        RouterFunctions.route(POST("/bot/long")
                .and(accept(MediaType.APPLICATION_JSON))) { botHandler.longTrigger() }
            .andRoute(POST("/bot/short")
                .and(accept(MediaType.APPLICATION_JSON))) { botHandler.shortTrigger() }
            .andRoute(POST("/bot/short/tp")
                .and(accept(MediaType.APPLICATION_JSON))) { botHandler.shortTakeProfit() }
            .andRoute(POST("/bot/long/tp")
                .and(accept(MediaType.APPLICATION_JSON))) { botHandler.longTakeProfit() }
            .andRoute(POST("/bot/bullish")
                .and(accept(MediaType.APPLICATION_JSON))) { botHandler.bullish() }
            .andRoute(POST("/bot/bearish")
                .and(accept(MediaType.APPLICATION_JSON))) { botHandler.bearish() }

                // fixme: comment
            .andRoute(GET("/test/open")) { tradingBot.testOpen() }
            .andRoute(GET("/test/close")) { tradingBot.testClose() }
            .andRoute(GET("/test")) { tradingBot.test() }


}