package uk.co.avsoftware.trading

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TradingApplication

fun main(args: Array<String>) {
	runApplication<TradingApplication>(*args)

	//Read environmental variables:
	//Read environmental variables:
	val env = System.getenv()
	println("BINANCE_KEY: " + env["BINANCE_KEY"])
}
