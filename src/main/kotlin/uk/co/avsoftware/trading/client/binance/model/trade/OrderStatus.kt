package uk.co.avsoftware.trading.client.binance.model.trade

enum class OrderStatus {
    NEW, // Order is new
    PARTIALLY_FILLED, // Order is partly filled, still has quantity left to fill
    FILLED, // The order has been filled and completed
    CANCELED, // The order has been canceled
    PENDING_CANCEL, // The order is in the process of being canceled
    REJECTED, // The order has been rejected
    EXPIRED // The order has expired
}