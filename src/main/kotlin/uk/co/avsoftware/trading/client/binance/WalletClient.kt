package uk.co.avsoftware.trading.client.binance

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.model.*
import uk.co.avsoftware.trading.client.binance.model.trade.TradeFee
import uk.co.avsoftware.trading.client.binance.request.AssetDetailRequest
import uk.co.avsoftware.trading.client.binance.request.BinanceRequest
import uk.co.avsoftware.trading.client.binance.request.FundingAssetRequest
import uk.co.avsoftware.trading.client.binance.request.TradeFeesRequest
import uk.co.avsoftware.trading.client.binance.response.AssetDetail
import uk.co.avsoftware.trading.client.binance.response.BinanceError
import uk.co.avsoftware.trading.client.binance.response.FundingAsset
import uk.co.avsoftware.trading.client.binance.sign.BinanceSigner
import java.io.IOException

@Component
class WalletClient(@Qualifier("binanceApiClient") val webClient: WebClient, val binanceSigner: BinanceSigner) {

    fun getSystemStatus(): Mono<SystemStatus> =
        webClient.get().uri("/sapi/v1/system/status").accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(SystemStatus::class.java)

    fun getAllCoinsInfo(): Flux<CoinInfo> =
        with (binanceSigner){
            webClient.get().uri("/sapi/v1/capital/config/getall?${signQueryString(BinanceRequest().baseQueryString())}")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-MBX-APIKEY", getApiKey() )
                .retrieve()
                .onStatus(
                    { it==HttpStatus.BAD_REQUEST },
                    { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
                )
                .onStatus({ it.is5xxServerError }, { Mono.error( RuntimeException("Server is not responding"))})
                .bodyToFlux(CoinInfo::class.java)
        }

    fun getDustLog(): Mono<DustLog> =
        with (binanceSigner){
            webClient.get().uri("/sapi/v1/asset/dribblet?${signQueryString(BinanceRequest().baseQueryString())}")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-MBX-APIKEY", getApiKey() )
                .retrieve()
                .onStatus(
                    { it==HttpStatus.BAD_REQUEST },
                    { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
                )
                .onStatus({ it.is5xxServerError }, { Mono.error( RuntimeException("Server is not responding"))})
                .bodyToMono(DustLog::class.java)
        }

    fun getTradeFees(request: TradeFeesRequest): Flux<TradeFee> =
        with (binanceSigner){
            webClient.get()
                .uri("/sapi/v1/asset/tradeFee?${signQueryString(request.getQueryString())}")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-MBX-APIKEY", getApiKey() )
                .retrieve()
                .onStatus(
                    { it==HttpStatus.BAD_REQUEST },
                    { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
                )
                .onStatus({ it.is5xxServerError }, { Mono.error( RuntimeException("Server is not responding"))})
                .bodyToFlux(TradeFee::class.java)
        }

    fun getAssetDetail(assetDetailRequest: AssetDetailRequest): Mono<Map<String,AssetDetail>> =
        with (binanceSigner){
            webClient.get().uri("/sapi/v1/asset/assetDetail?${signQueryString(assetDetailRequest.getQueryString())}")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-MBX-APIKEY", getApiKey() )
                .retrieve()
                .onStatus(
                    { it== HttpStatus.BAD_REQUEST },
                    { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
                )
                .onStatus({ it.is5xxServerError }, { Mono.error( RuntimeException("Server is not responding"))})
                .bodyToMono(object : ParameterizedTypeReference<Map<String,AssetDetail>>(){})
        }

    fun getFundingAsset(fundingAssetRequest: FundingAssetRequest): Flux<FundingAsset> =
        with (binanceSigner){
            webClient.post().uri("/sapi/v1/asset/get-funding-asset?${signQueryString(fundingAssetRequest.getQueryString())}")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-MBX-APIKEY", getApiKey() )
                .retrieve()
                .onStatus(
                    { it== HttpStatus.BAD_REQUEST },
                    { response -> response.bodyToMono(BinanceError::class.java).map { error -> IOException(error) } }
                )
                .onStatus({ it.is5xxServerError }, { Mono.error( RuntimeException("Server is not responding"))})
                .bodyToFlux(FundingAsset::class.java)
                .doOnNext {  it.asset }
        }
}