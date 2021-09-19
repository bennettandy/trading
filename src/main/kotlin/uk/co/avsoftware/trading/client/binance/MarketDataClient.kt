package uk.co.avsoftware.trading.client.binance

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.request.CurrentPriceRequest
import uk.co.avsoftware.trading.client.binance.request.OrderBookRequest
import uk.co.avsoftware.trading.client.binance.request.SymbolPriceTickerRequest
import uk.co.avsoftware.trading.client.binance.response.*
import uk.co.avsoftware.trading.client.binance.sign.BinanceSigner
import java.io.IOException

@Component
class MarketDataClient(@Qualifier("binanceApiClient") val webClient: WebClient, val binanceSigner: BinanceSigner) {

    fun pingServer(): Mono<Unit> =
            webClient.get().uri("/api/v3/ping")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Unit::class.java)

    fun getServerTime(): Mono<ServerTimeResponse> =
        webClient.get().uri("/api/v3/time")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                { it== HttpStatus.BAD_REQUEST },
                { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
            )
            .bodyToMono(ServerTimeResponse::class.java)

    fun getOrderBookDepth(orderBookRequest: OrderBookRequest): Mono<OrderBookResponse> =
            webClient.get().uri("/api/v3/depth?${orderBookRequest.getQueryString()}")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                    { it == HttpStatus.BAD_REQUEST },
                    { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
                )
                .bodyToMono(OrderBookResponse::class.java)

    fun getRecentTrades(orderBookRequest: OrderBookRequest): Flux<RecentTradeResponse> =
        webClient.get().uri("/api/v3/trades?${orderBookRequest.getQueryString()}")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                { it == HttpStatus.BAD_REQUEST },
                { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
            )
            .bodyToFlux(RecentTradeResponse::class.java)

    fun getCurrentAveragePrice(currentPriceRequest: CurrentPriceRequest): Mono<CurrentAveragePriceResponse> =
        webClient.get().uri("/api/v3/avgPrice?${currentPriceRequest.getQueryString()}")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                { it == HttpStatus.BAD_REQUEST },
                { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
            )
            .bodyToMono(CurrentAveragePriceResponse::class.java)

    fun get24HourPriceChange(currentPriceRequest: CurrentPriceRequest): Flux<PriceChangeResponse> =
        webClient.get().uri("/api/v3/ticker/24hr?${currentPriceRequest.getQueryString()}")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                { it == HttpStatus.BAD_REQUEST },
                { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
            )
            .bodyToFlux(PriceChangeResponse::class.java)

    fun getTickerPrice(symbolRequest: SymbolPriceTickerRequest): Flux<SymbolTickerResponse> =
        webClient.get().uri("/api/v3/ticker/price?${symbolRequest.getQueryString()}")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                { it == HttpStatus.BAD_REQUEST },
                { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
            )
            .bodyToFlux(SymbolTickerResponse::class.java)

    fun getOrderBookTickerPrice(symbolRequest: SymbolPriceTickerRequest): Flux<SymbolOrderBookTickerResponse> =
        webClient.get().uri("/api/v3/ticker/price?${symbolRequest.getQueryString()}")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(
                { it == HttpStatus.BAD_REQUEST },
                { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
            )
            .bodyToFlux(SymbolOrderBookTickerResponse::class.java)
}