package uk.co.avsoftware.trading.client.bybit.config

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import uk.co.avsoftware.trading.client.binance.config.BinanceConfigProperties
import javax.annotation.PostConstruct

@Configuration
class BybitClientConfig(private val bybitConfigProperties: BinanceConfigProperties) {

    private val logger = KotlinLogging.logger {}

    @Bean
    @Qualifier("bybitApiClient")
    fun bybitApiClient(builder: WebClient.Builder): WebClient =
        builder.baseUrl(bybitConfigProperties.uri).build()

    @PostConstruct
    fun validateConfig() {
        bybitConfigProperties.apply {
            if (key.isEmpty()) {
                logger.error("Missing Bybit Api Key")
            } else logger.info("Obtained Bybit API KEY ${key.length} chars")

            if (secret.isEmpty()) {
                logger.error("Missing Bybit Api Secret")
            } else logger.info("Obtained Bybit API SECRET ${secret.length} chars")
        }
    }
}