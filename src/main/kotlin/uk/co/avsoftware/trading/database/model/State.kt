package uk.co.avsoftware.trading.database.model

import com.google.cloud.firestore.DocumentReference
import uk.co.avsoftware.trading.client.binance.model.trade.BotDirection

data class State(
    val enabled: Boolean? = null,
    var exchange: String = "",
    var symbol: String = "",
    // open position
    var open_position: DocumentReference? = null,
    var position_size: Double = 1.0,
    var open_qty: Double = 0.0,
    var direction: BotDirection = BotDirection.IDLE
)