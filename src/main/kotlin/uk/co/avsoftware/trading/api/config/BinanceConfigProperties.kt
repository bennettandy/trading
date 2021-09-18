package uk.co.avsoftware.trading.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "api.binance")
data class BinanceConfigProperties(
    var description: String = "",
    var uri: String = "",
    var key: String = "",
    var secret: String = ""
)