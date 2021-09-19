package uk.co.avsoftware.trading.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RequestPredicates.accept
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import uk.co.avsoftware.trading.client.binance.parameters.TradeFeesRequest
import uk.co.avsoftware.trading.client.binance.parameters.TradeListRequest
import uk.co.avsoftware.trading.web.handler.ApiKeyHandler
import uk.co.avsoftware.trading.web.handler.GreetingHandler
import uk.co.avsoftware.trading.web.handler.SpotTradeHandler
import uk.co.avsoftware.trading.web.handler.WalletHandler

@Configuration(proxyBeanMethods = false)
class MainRouter() {

    @Bean
    fun route(greetingHandler: GreetingHandler,
              walletHandler: WalletHandler,
              apiKeyHandler: ApiKeyHandler,
              spotTradeHandler: SpotTradeHandler
    ): RouterFunction<ServerResponse> =
        RouterFunctions.route(
            GET("/hello").and(accept(MediaType.APPLICATION_JSON))) { greetingHandler.hello() }
            .andRoute(GET("/api/permissions")
                .and(accept(MediaType.APPLICATION_JSON))) { apiKeyHandler.getApiKeyPermissions() }
            .andRoute(GET("/wallet/coins")
                .and(accept(MediaType.APPLICATION_JSON))) { walletHandler.getAllCoinsInfo() }
            .andRoute(GET("/wallet/status")
                .and(accept(MediaType.APPLICATION_JSON))) { walletHandler.systemStatus() }
            .andRoute(GET("/wallet/dust")
                .and(accept(MediaType.APPLICATION_JSON))) { walletHandler.getDustLog() }
            .andRoute(GET("/wallet/fees")
                .and(accept(MediaType.APPLICATION_JSON))) { walletHandler.getTradeFees(TradeFeesRequest.from(it)) }
            .andRoute(GET("/trade/account")
                .and(accept(MediaType.APPLICATION_JSON))) { spotTradeHandler.getAccountInformation() }
            .andRoute(GET("/trade/list")
                .and(accept(MediaType.APPLICATION_JSON))) { spotTradeHandler.getAccountTradeList(
                TradeListRequest.from(it)
            )}
}