package uk.co.avsoftware.trading.client.binance.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "api.bybit")
data class BinanceConfigProperties(
    var description: String = "",
    var uri: String = "",
    var key: String = "",
    var secret: String = ""
)