package uk.co.avsoftware.trading.client.binance.model

enum class OrderType {
    MARKET, // Market order will be placed without a price. The order will be executed at the best price available at that time in the order book.
    LIMIT, // Limit orders will be placed at a specific price. If the price isn't available in the order book for that asset the order will be added in the order book for someone to fill.
    STOP_LOSS, // Stop loss order. Will execute a market order when the price drops below a price to sell and therefore limit the loss
    STOP_LOSS_LIMIT, // Stop loss order. Will execute a limit order when the price drops below a price to sell and therefore limit the loss
    TAKE_PROFIT, // Take profit order. Will execute a market order when the price rises above a price to sell and therefore take a profit
    TAKE_PROFIT_LIMIT, // Take profit order. Will execute a limit order when the price rises above a price to sell and therefore take a profit
    LIMIT_MAKER // Same as a limit order, however it will fail if the order would immediately match, therefore preventing taker orders
}