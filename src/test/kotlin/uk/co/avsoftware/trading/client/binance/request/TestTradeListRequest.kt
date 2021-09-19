package uk.co.avsoftware.trading.client.binance.request

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.server.ServerRequest
import uk.co.avsoftware.trading.client.binance.model.trade.NewOrderResponseType
import uk.co.avsoftware.trading.client.binance.model.trade.OrderSide
import uk.co.avsoftware.trading.client.binance.model.trade.OrderType
import uk.co.avsoftware.trading.client.binance.model.trade.TimeInForce
import java.util.*

class TestTradeListRequest {

    @Test
    fun testTradeListRequest(){

        val serverRequest: ServerRequest = mockk()
        every { serverRequest.queryParam("symbol") } returns Optional.of("ETHBTC")
        every { serverRequest.queryParam("side") } returns Optional.of(OrderSide.BUY.name)
        every { serverRequest.queryParam("type") } returns Optional.of(OrderType.LIMIT.name)
        every { serverRequest.queryParam("timeInForce") } returns Optional.of(TimeInForce.GTC.name)
        every { serverRequest.queryParam("quantity") } returns Optional.of("0.001")
        every { serverRequest.queryParam("quoteOrderQty") } returns Optional.of("0.001")
        every { serverRequest.queryParam("price") } returns Optional.of("0.001")
        every { serverRequest.queryParam("newClientOrderId") } returns Optional.of("order-123")
        every { serverRequest.queryParam("stopPrice") } returns Optional.of("0.001")
        every { serverRequest.queryParam("icebergQty") } returns Optional.of("0.001")
        every { serverRequest.queryParam("newOrderRespType") } returns Optional.of(NewOrderResponseType.ACK.name)

        val request = TradeListRequest.from(serverRequest)

        val queryString = request.getQueryString()

        assertThat(queryString).isNotNull

        assertThat(queryString).endsWith("&symbol=ETHBTC&side=BUY&type=LIMIT&timeInForce=GTC&quantity=0.001&quoteOrderQty=0.001&price=0.001&newClientOrderId=order-123&stopPrice=0.001&icebergQty=0.001&newOrderRespType=ACK")
    }
}