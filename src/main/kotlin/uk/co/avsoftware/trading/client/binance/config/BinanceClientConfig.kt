package uk.co.avsoftware.trading.client.binance.config

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import java.time.Clock
import javax.annotation.PostConstruct

@Configuration
class BinanceClientConfig( private val binanceConfigProperties: BinanceConfigProperties) {

    private val logger = KotlinLogging.logger {}

    @Bean
    fun provideClock() = Clock.systemDefaultZone()

    @Bean
    @Qualifier("binanceApiClient")
    fun binanceApiClient(builder: WebClient.Builder, binanceConfigProperties: BinanceConfigProperties): WebClient =
        builder.baseUrl(binanceConfigProperties.uri).build()

    @PostConstruct
    fun validateConfig() {
        binanceConfigProperties.apply {
            if (key.isEmpty()) {
                logger.error("Missing Binance Api Key")
            } else logger.info("Obtained Binance API KEY ${key.length} chars")

            if (secret.isEmpty()) {
                logger.error("Missing Binance Api Secret")
            } else logger.info("Obtained Binance API SECRET ${secret.length} chars")
        }
    }
}