package uk.co.avsoftware.trading.database.model

data class Profits(
    val openingPosition: Position,
    val closingPosition: Position,

    val profit: Double
)
