package uk.co.avsoftware.trading.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RequestPredicates.accept
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import uk.co.avsoftware.trading.api.config.BinanceConfigProperties

@Configuration(proxyBeanMethods = false)
class GreetingRouter(var binanceConfigProperties: BinanceConfigProperties) {


    @Bean
    fun route(greetingHandler: GreetingHandler, walletHandler: WalletHandler): RouterFunction<ServerResponse> =
        RouterFunctions.route(
            GET("/hello").and(accept(MediaType.APPLICATION_JSON)),
            HandlerFunction { _ -> greetingHandler.hello() })
            .andRoute(GET("/wallet/status").and(accept(MediaType.APPLICATION_JSON)),
                HandlerFunction { walletHandler.systemStatus() })
}