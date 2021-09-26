package uk.co.avsoftware.trading.repository.service

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.WriteResult
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.response.OrderResponse

@Service
class TradeService {
    companion object {
        const val COL_NAME = "trades"
    }

    // todo: temporary while I build trade object
    fun saveOrderResponse(orderResult: OrderResponse): Mono<String> {
        val dbFirestore = FirestoreClient.getFirestore()

        val collectionsApiFuture: ApiFuture<WriteResult> = dbFirestore.collection(COL_NAME)
            .document(orderResult.orderId.toString()).set(orderResult)

        return Mono.fromSupplier { collectionsApiFuture.get() }
            .map { result -> result.updateTime.toString()}
    }


}