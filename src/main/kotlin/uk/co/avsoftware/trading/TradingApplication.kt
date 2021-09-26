package uk.co.avsoftware.trading

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import uk.co.avsoftware.trading.api.config.BinanceConfigProperties
import javax.annotation.PostConstruct

@SpringBootApplication
class TradingApplication {

    private val logger = KotlinLogging.logger {}

    @Autowired
    lateinit var binanceConfigProperties: BinanceConfigProperties

    @PostConstruct
    fun validateConfig() {

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
