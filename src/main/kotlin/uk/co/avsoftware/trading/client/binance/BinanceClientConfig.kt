package uk.co.avsoftware.trading.client.binance

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import uk.co.avsoftware.trading.api.config.BinanceConfigProperties

@Configuration
class BinanceClientConfig {

    @Bean
    @Qualifier("binanceApiClient")
    fun binanceApiClient(builder: WebClient.Builder, binanceConfigProperties: BinanceConfigProperties): WebClient =
        builder.baseUrl(binanceConfigProperties.uri).build()

}