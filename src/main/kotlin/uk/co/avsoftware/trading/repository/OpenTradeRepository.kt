package uk.co.avsoftware.trading.repository

import com.google.cloud.firestore.DocumentReference
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.model.OrderFill
import uk.co.avsoftware.trading.client.binance.model.OrderResponse
import uk.co.avsoftware.trading.database.model.*
import uk.co.avsoftware.trading.repository.service.OpenTradeService
import java.time.Instant

@Service
class OpenTradeRepository(val openTradeService: OpenTradeService) {

    private val logger = KotlinLogging.logger {}

    fun createNewOpenTrade(openingOrder: OrderResponse, state: State, direction: Direction): Mono<DocumentReference> {
        return openTradeService.saveNewOpenTrade( OpenTrade(
            exchange = state.exchange,
            symbol = state.symbol,
            direction = direction,
            completed = false,
            open_fills = openingOrder.fills?.map { mapOrderFills(it, state) } ?: emptyList(),
            open_commission = commissionString(openingOrder.fills)
        ))
    }

    fun addClosingOrder(closingOrder: OrderResponse, state: State, direction: Direction): Mono<State>
    = state.open_position?.let { documentReference ->
        openTradeService.retrieveOpenTrade(documentReference)
            .flatMap { openTrade: OpenTrade -> openTradeService.updateOpenTrade(
                openTrade.copy(
                    closing_fills = addClosingFills(openTrade.closing_fills, closingOrder, state),
                    close_commission = commissionString(closingOrder.fills)
                ),
                documentReference.id
            )  }
            .thenReturn(state)
    } ?: Mono.just(state)

    fun closeOrder(state: State): Mono<State>
    = state.open_position?.let { documentReference ->
        openTradeService.retrieveOpenTrade(documentReference)
            .flatMap { openTrade: OpenTrade -> openTradeService.updateOpenTrade(
                openTrade.copy(
                    completed = true,
                    profit = calculateProfit(openTrade)
                ),
                documentReference.id
            )  }
                // open trade updated and profits calculated
            .thenReturn(state)
    } ?: Mono.just(state)

    private fun calculateProfit(openTrade: OpenTrade): Double {
        val openCost: Double = openTrade.open_fills.sumOf { it.qty * it.price }
        val closeCost: Double = openTrade.closing_fills.sumOf { it.qty * it.price }
        logger.info { "Open cost: $openCost, Close cost: $closeCost, profit = ${openCost-closeCost}"}
        return openCost - closeCost
    }

    private fun commissionString(tradeFills: List<OrderFill>?): String =
        tradeFills?.let {
            "${tradeFills.sumOf { it.commission }} ${tradeFills.first().commissionAsset}"
        } ?: "n/a"

    private fun addClosingFills(tradeFills: List<TradeFill>, closingOrder: OrderResponse, state: State): List<TradeFill> =
        (closingOrder.fills?.map { mapOrderFills(it, state) } ?: emptyList()).plus(tradeFills)

    private fun mapOrderFills(orderFill: OrderFill, state: State): TradeFill =
        TradeFill(
            timestamp = Instant.now().toEpochMilli(),
            symbol = state.symbol,
            price = orderFill.price,
            qty = orderFill.qty,
            commission = orderFill.commission,
            commissionAsset = orderFill.commissionAsset
        )

    fun addOpenOrderToCompletedTrade(completedTrade: CompletedTrade, orderResponse: OrderResponse) {
        completedTrade.apply {
            with(orderResponse) {
                open_commission = fills?.map { orderFill -> orderFill.commission } ?: emptyList()
                open_quantity = fills?.map { orderFill -> orderFill.qty } ?: emptyList()
                open_price = fills?.map { orderFill -> orderFill.price } ?: emptyList()
                direction = orderResponse.side?.name ?: "missing"
                open_commission_currency = fills?.first()?.commissionAsset ?: "unknown"
                open_order_id = orderResponse.orderId.toString()
                open_time_stamp = orderResponse.transactTime ?: -1
            }
            logger.info { "Added open order to CompletedTrade: $this" }
        }
    }

    fun addCloseOrderToCompletedTrade(completedTrade: CompletedTrade, orderResponse: OrderResponse) {
        completedTrade.apply {
            with(orderResponse) {
                close_commission = fills?.map { orderFill -> orderFill.commission } ?: emptyList()
                close_quantity = fills?.map { orderFill -> orderFill.qty } ?: emptyList()
                close_price = fills?.map { orderFill -> orderFill.price } ?: emptyList()
                close_commission_currency = fills?.first()?.commissionAsset ?: "unknown"
                close_order_id = orderResponse.orderId.toString()
                close_time_stamp = orderResponse.transactTime ?: -1
            }
            logger.info { "Added close order to CompletedTrade: $this" }
        }
    }

    private fun calculateProfits(completedTrade: CompletedTrade, state: State) {
        completedTrade.apply {
            open_qty = completedTrade.open_quantity.sum()
            open_cost = completedTrade.open_quantity
                .mapIndexed { index, d -> d * completedTrade.open_price[index] }.sum()
            open_comm = completedTrade.open_commission.sum()
            close_qty = completedTrade.close_quantity.sum()
            close_cost = completedTrade.close_quantity
                .mapIndexed { index, d -> d * completedTrade.close_price[index] }.sum()
            close_comm = completedTrade.close_commission.sum()
            price_delta = when (direction) {
                "SELL" -> open_cost - close_cost // sell and buy back cheaper
                "BUY" -> close_cost - open_cost // buy cheap and sell higher
                else -> 0.0
            }

            val percentage = price_delta / open_cost

            // populate exchange details
            exchange = state.exchange
            symbol = state.symbol
            name = "$symbol [$exchange] - $ : ${percentage}%"

            logger.info { "Calculated Totals: Position: $this" }
        }
    }
}