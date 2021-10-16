package uk.co.avsoftware.trading.client.binance.model

enum class TimeInForce {
    GTC, // Good 'till cancel orders will stay active until they are filled or canceled
    IOC, // Immediate or Cancel orders have to be at least partially filled upon placing or will be automatically canceled
    FOK // Fill or kill orders have to be entirely filled upon placing or will be automatically canceled
}