package uk.co.avsoftware.trading

import mu.KotlinLogging
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.DependsOn
import uk.co.avsoftware.trading.api.config.BinanceConfigProperties
import uk.co.avsoftware.trading.client.binance.sign.BinanceClientConfig
import uk.co.avsoftware.trading.database.model.Person
import uk.co.avsoftware.trading.database.service.PersonService
import java.util.function.Consumer
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
