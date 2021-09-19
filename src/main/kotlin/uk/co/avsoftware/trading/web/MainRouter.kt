package uk.co.avsoftware.trading.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RequestPredicates.accept
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import uk.co.avsoftware.trading.client.binance.request.NewOrderRequest
import uk.co.avsoftware.trading.client.binance.request.TradeFeesRequest
import uk.co.avsoftware.trading.client.binance.request.TradeListRequest
import uk.co.avsoftware.trading.web.handler.ApiKeyHandler
import uk.co.avsoftware.trading.web.handler.GreetingHandler
import uk.co.avsoftware.trading.web.handler.SpotTradeHandler
import uk.co.avsoftware.trading.web.handler.WalletHandler

@Configuration(proxyBeanMethods = false)
class MainRouter() {

    @Bean
    fun route(greeting: GreetingHandler,
              wallet: WalletHandler,
              apiKey: ApiKeyHandler,
              spotTrade: SpotTradeHandler
    ): RouterFunction<ServerResponse> =
        RouterFunctions.route(
            GET("/hello").and(accept(MediaType.APPLICATION_JSON))) { greeting.hello() }
            .andRoute(GET("/api/permissions")
                .and(accept(MediaType.APPLICATION_JSON))) { apiKey.getApiKeyPermissions() }
            .andRoute(GET("/wallet/coins")
                .and(accept(MediaType.APPLICATION_JSON))) { wallet.getAllCoinsInfo() }
            .andRoute(GET("/wallet/status")
                .and(accept(MediaType.APPLICATION_JSON))) { wallet.systemStatus() }
            .andRoute(GET("/wallet/dust")
                .and(accept(MediaType.APPLICATION_JSON))) { wallet.getDustLog() }
            .andRoute(GET("/wallet/fees")
                .and(accept(MediaType.APPLICATION_JSON))) { wallet.getTradeFees(TradeFeesRequest.from(it)) }
            .andRoute(GET("/trade/account")
                .and(accept(MediaType.APPLICATION_JSON))) { spotTrade.getAccountInformation() }
            .andRoute(GET("/trade/list")
                .and(accept(MediaType.APPLICATION_JSON))) { spotTrade.getAccountTradeList(
                TradeListRequest.from(it) // fixme
            )}
            .andRoute(GET("/trade/test/order")
                .and(accept(MediaType.APPLICATION_JSON))) { spotTrade.testNewOrder(
                NewOrderRequest.from(it)
            )}
}