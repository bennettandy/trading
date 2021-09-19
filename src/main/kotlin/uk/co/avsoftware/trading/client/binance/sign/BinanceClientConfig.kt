package uk.co.avsoftware.trading.client.binance.sign

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import uk.co.avsoftware.trading.api.config.BinanceConfigProperties
import java.time.Clock

@Configuration
class BinanceClientConfig {

    @Bean
    fun provideClock() = Clock.systemDefaultZone()

    @Bean
    @Qualifier("binanceApiClient")
    fun binanceApiClient(builder: WebClient.Builder, binanceConfigProperties: BinanceConfigProperties): WebClient =
        builder.baseUrl(binanceConfigProperties.uri).build()

}