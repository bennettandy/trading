package uk.co.avsoftware.trading

import mu.KotlinLogging
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import uk.co.avsoftware.trading.api.config.BinanceConfigProperties
import uk.co.avsoftware.trading.client.binance.sign.BinanceClientConfig
import javax.annotation.PostConstruct

@SpringBootApplication
class TradingApplication {

    private val logger = KotlinLogging.logger {}

//	@Value("\${sm//:binance-api-key}")
//	val apiKey: String? = null

    @Autowired
    lateinit var binanceConfigProperties: BinanceConfigProperties

    @PostConstruct
    fun validateConfig() {
//		logger.info("API KEY $apiKey")

		with (binanceConfigProperties) {
			if (key.isEmpty()) {
				logger.error("Missing Binance Api Key")
			} else logger.info("Obtained Binance API KEY ${key.length} chars")

			if (secret.isEmpty()) {
				logger.error("Missing Binance Api Secret")
			} else logger.info("Obtained Binance API SECRET ${secret.length} chars")
		}
    }

}


fun main(args: Array<String>) {
    runApplication<TradingApplication>(*args)
}
