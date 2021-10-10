package uk.co.avsoftware.trading.database.model

import com.google.cloud.firestore.DocumentReference

data class State(
    val enabled: Boolean? = null,
    var exchange: String = "",
    var symbol: String = "",
    // open position
    var open_position: DocumentReference? = null,
    var position_size: Double = 1.0,
    var direction: String = ""
)