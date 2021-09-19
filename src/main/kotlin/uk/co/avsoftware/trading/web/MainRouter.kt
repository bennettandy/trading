package uk.co.avsoftware.trading.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RequestPredicates.accept
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import uk.co.avsoftware.trading.client.binance.request.*
import uk.co.avsoftware.trading.web.handler.ApiKeyHandler
import uk.co.avsoftware.trading.web.handler.MarketDataHandler
import uk.co.avsoftware.trading.web.handler.SpotTradeHandler
import uk.co.avsoftware.trading.web.handler.WalletHandler

@Configuration(proxyBeanMethods = false)
class MainRouter() {

    @Bean
    fun route( wallet: WalletHandler,
              apiKey: ApiKeyHandler,
              spotTrade: SpotTradeHandler,
               marketData: MarketDataHandler
    ): RouterFunction<ServerResponse> =
        RouterFunctions.route(
            GET("/api/permissions")
                .and(accept(MediaType.APPLICATION_JSON))) { apiKey.getApiKeyPermissions() }
            .andRoute(GET("/wallet/coins")
                .and(accept(MediaType.APPLICATION_JSON))) { wallet.getAllCoinsInfo() }
            .andRoute(GET("/wallet/status")
                .and(accept(MediaType.APPLICATION_JSON))) { wallet.systemStatus() }
            .andRoute(GET("/wallet/dust")
                .and(accept(MediaType.APPLICATION_JSON))) { wallet.getDustLog() }
            .andRoute(GET("/wallet/assetDetail")
                .and(accept(MediaType.APPLICATION_JSON))) { wallet.getAssetDetail(AssetDetailRequest.from(it)) }
            .andRoute(GET("/wallet/fees")
                .and(accept(MediaType.APPLICATION_JSON))) { wallet.getTradeFees(TradeFeesRequest.from(it)) }
            .andRoute(GET("/trade/account")
                .and(accept(MediaType.APPLICATION_JSON))) { spotTrade.getAccountInformation() }
            .andRoute(GET("/trade/list")
                .and(accept(MediaType.APPLICATION_JSON))) { spotTrade.getAccountTradeList(TradeListRequest.from(it))}
            .andRoute(GET("/trade/test/order")
                .and(accept(MediaType.APPLICATION_JSON))) { spotTrade.testNewOrder(
                NewOrderRequest.from(it)
            )}
            .andRoute(GET("/market/ping")
                .and(accept(MediaType.APPLICATION_JSON))) { marketData.pingServer()}
            .andRoute(GET("/market/time")
                .and(accept(MediaType.APPLICATION_JSON))) { marketData.getServerTime()}
            .andRoute(GET("/market/depth")
                .and(accept(MediaType.APPLICATION_JSON))) { marketData.getOrderBookDepth(OrderBookRequest.from(it))}
            .andRoute(GET("/market/trades")
                .and(accept(MediaType.APPLICATION_JSON))) { marketData.getRecentTrades(OrderBookRequest.from(it))}
            .andRoute(GET("/market/avgPrice")
                .and(accept(MediaType.APPLICATION_JSON))) { marketData.getCurrentAveragePrice(CurrentPriceRequest.from(it))}
            .andRoute(GET("/market/ticker/24hr")
                .and(accept(MediaType.APPLICATION_JSON))) { marketData.get24HourPriceChange(CurrentPriceRequest.from(it))}
            .andRoute(GET("/market/ticker/price")
                .and(accept(MediaType.APPLICATION_JSON))) { marketData.getTickerPrice(SymbolPriceTickerRequest.from(it))}
            .andRoute(GET("/market/ticker/bookTicker") // fixme: null responses
                .and(accept(MediaType.APPLICATION_JSON))) { marketData.getOrderBookTickerPrice(SymbolPriceTickerRequest.from(it))}
}