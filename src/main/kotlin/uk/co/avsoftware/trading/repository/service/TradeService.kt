package uk.co.avsoftware.trading.repository.service

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.WriteResult
import com.google.firebase.cloud.FirestoreClient
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.co.avsoftware.trading.client.binance.model.trade.OrderResponse

@Service
class TradeService() {
    companion object {
        const val COL_NAME = "trades"
        private val logger = KotlinLogging.logger {}
    }

    val dbFirestore: Firestore by lazy { FirestoreClient.getFirestore() }

    fun saveOrderResponse(orderResult: OrderResponse): Mono<DocumentReference> {
        val collection = dbFirestore.collection(COL_NAME)
        val documentId = orderResult.orderId.toString()
        val collectionsApiFuture: ApiFuture<WriteResult> = collection.document(documentId).set(orderResult)
        return Mono.fromSupplier { collectionsApiFuture.get() }
            .map { result: WriteResult  -> result.updateTime.toString()}
            .doOnSuccess { updatedTime -> logger.info { "Saved order result - updated time: $updatedTime" } }
                // emit document reference to saved orderResult
            .flatMap { Mono.fromSupplier { collection.document(documentId) } }
    }

    fun loadOrderResponse(documentReference: DocumentReference): Mono<OrderResponse> {
        val future: ApiFuture<DocumentSnapshot> = documentReference.get()
        return Mono.fromSupplier { future.get() }
            .checkpoint("retrieve OrderResponse")
            .doOnSuccess { logger.info { "Got OrderResponse $it" } }
            .doOnError { logger.info { "Failed to get OrderResponse ${it.message}" } }
            .map {  documentSnapshot -> documentSnapshot.toObject(OrderResponse::class.java) }
    }

}